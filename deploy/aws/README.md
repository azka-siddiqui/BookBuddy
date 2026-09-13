# AWS Deployment (ECS Fargate)

This directory contains Terraform describing how BookBuddy runs on AWS. It is
provided to demonstrate the target cloud architecture; applying it will create
billable resources, so review and adjust before running `terraform apply`.

## Architecture

```
Internet
   │
   ▼
Application Load Balancer  ──►  gateway (Fargate)  ──►  catalog / social / discovery (Fargate)
                                                             │
                                                             ▼
                                              MongoDB (Atlas or DocumentDB)
```

- **ECR** hosts one image repository per service (built from the Dockerfiles).
- **ECS Fargate** runs each service as a task in a shared cluster; the gateway
  sits behind an **Application Load Balancer**.
- **Service discovery** (AWS Cloud Map) lets the gateway reach the internal
  services by DNS name (e.g. `catalog-service.bookbuddy.local`).
- **MongoDB** is external — MongoDB Atlas (free tier) or Amazon DocumentDB. The
  connection string and JWT secret are injected from **AWS Secrets Manager**.

## Files

| File            | Purpose                                                        |
|-----------------|----------------------------------------------------------------|
| `providers.tf`  | AWS provider and Terraform version constraints                 |
| `variables.tf`  | Region, image tags, Mongo URI and JWT secret inputs            |
| `network.tf`    | VPC, subnets, security groups, Cloud Map namespace             |
| `ecr.tf`        | One image repository per service                               |
| `ecs.tf`        | Cluster, task definitions and services for all components      |
| `alb.tf`        | Public load balancer routing to the gateway                    |

## Deploy outline

```sh
# 1. Build and push images to ECR (repository URLs come from `terraform output`)
docker build -t <ecr>/bookbuddy-gateway -f services/api-gateway/Dockerfile .
docker push <ecr>/bookbuddy-gateway   # repeat per service

# 2. Provision infrastructure
cd deploy/aws
terraform init
terraform apply \
  -var "mongo_uri=mongodb+srv://…" \
  -var "jwt_secret=$(openssl rand -base64 32)"

# 3. The ALB DNS name is printed as an output; point the frontend at it.
```

## Cost note

Fargate, the ALB and DocumentDB are billable. For a zero-cost demo, run the stack
locally with `docker compose up` (see the root README) and use this Terraform as a
reference for the production design.
