variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name (e.g. dev, prod)"
  type        = string
  default     = "dev"
}

variable "image_tag" {
  description = "Container image tag to deploy for every service"
  type        = string
  default     = "latest"
}

variable "mongo_uri" {
  description = "MongoDB connection string (Atlas or DocumentDB)"
  type        = string
  sensitive   = true
}

variable "mongo_database" {
  description = "MongoDB database name"
  type        = string
  default     = "bookbuddy"
}

variable "jwt_secret" {
  description = "Base64-encoded HS256 JWT signing secret, shared by gateway and social-service"
  type        = string
  sensitive   = true
}

variable "desired_count" {
  description = "Number of tasks per service"
  type        = number
  default     = 1
}

variable "services" {
  description = "Internal services and the ports they listen on"
  type        = map(number)
  default = {
    "catalog-service"   = 8081
    "discovery-service" = 8082
    "social-service"    = 8083
  }
}
