# One image repository per service (three internal services + the gateway).
locals {
  ecr_repositories = concat(keys(var.services), ["api-gateway"])
}

resource "aws_ecr_repository" "service" {
  for_each = toset(local.ecr_repositories)

  name                 = "bookbuddy-${each.value}"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

output "ecr_repository_urls" {
  description = "Push images to these repositories before deploying"
  value       = { for name, repo in aws_ecr_repository.service : name => repo.repository_url }
}
