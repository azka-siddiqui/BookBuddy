# ECS cluster running the gateway (public, behind the ALB) and the three internal
# services (reachable via Cloud Map DNS). MongoDB is external (Atlas/DocumentDB).

resource "aws_ecs_cluster" "main" {
  name = "bookbuddy"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

# --- IAM: task execution role (pull images, write logs) ---
data "aws_iam_policy_document" "ecs_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "execution" {
  name               = "bookbuddy-ecs-execution"
  assume_role_policy = data.aws_iam_policy_document.ecs_assume.json
}

resource "aws_iam_role_policy_attachment" "execution" {
  role       = aws_iam_role.execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_cloudwatch_log_group" "services" {
  for_each          = toset(concat(keys(var.services), ["api-gateway"]))
  name              = "/ecs/bookbuddy/${each.value}"
  retention_in_days = 14
}

# --- Cloud Map services for the internal components ---
resource "aws_service_discovery_service" "internal" {
  for_each = var.services

  name = each.key

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.internal.id
    dns_records {
      type = "A"
      ttl  = 10
    }
    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

# --- Task definitions and services for the three internal components ---
resource "aws_ecs_task_definition" "internal" {
  for_each = var.services

  family                   = "bookbuddy-${each.key}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.execution.arn

  container_definitions = jsonencode([
    {
      name      = each.key
      image     = "${aws_ecr_repository.service[each.key].repository_url}:${var.image_tag}"
      essential = true
      portMappings = [{ containerPort = each.value }]
      environment = [
        { name = "MONGO_URI", value = var.mongo_uri },
        { name = "MONGO_DATABASE", value = var.mongo_database },
        { name = "JWT_SECRET", value = var.jwt_secret },
        { name = "SPRING_PROFILES_ACTIVE", value = "dev" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services[each.key].name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "internal" {
  for_each = var.services

  name            = each.key
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.internal[each.key].arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.service.id]
    assign_public_ip = true
  }

  service_registries {
    registry_arn = aws_service_discovery_service.internal[each.key].arn
  }
}

# --- Gateway task definition and service (public via ALB) ---
resource "aws_ecs_task_definition" "gateway" {
  family                   = "bookbuddy-api-gateway"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.execution.arn

  container_definitions = jsonencode([
    {
      name      = "api-gateway"
      image     = "${aws_ecr_repository.service["api-gateway"].repository_url}:${var.image_tag}"
      essential = true
      portMappings = [{ containerPort = 8080 }]
      environment = [
        { name = "CATALOG_URI", value = "http://catalog-service.bookbuddy.local:8081" },
        { name = "SOCIAL_URI", value = "http://social-service.bookbuddy.local:8083" },
        { name = "DISCOVERY_URI", value = "http://discovery-service.bookbuddy.local:8082" },
        { name = "JWT_SECRET", value = var.jwt_secret }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services["api-gateway"].name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "gateway" {
  name            = "api-gateway"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.gateway.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.service.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.gateway.arn
    container_name   = "api-gateway"
    container_port   = 8080
  }

  depends_on = [aws_lb_listener.http]
}
