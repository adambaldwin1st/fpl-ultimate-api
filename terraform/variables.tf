variable "aws_region" {
  description = "AWS region the Lambda + API Gateway infra lives in. Must match the region ACM/API Gateway custom domains are created in (regional endpoints require the cert in the same region)."
  type        = string
  default     = "us-east-2"
}

variable "domain_name" {
  description = "Root domain, already hosted in Route53."
  type        = string
  default     = "fplultimate.com"
}

variable "prod_subdomain" {
  description = "Full hostname routed to the prod Lambda."
  type        = string
  default     = "api.fplultimate.com"
}

variable "stage_subdomain" {
  description = "Full hostname routed to the stage Lambda."
  type        = string
  default     = "stage.api.fplultimate.com"
}

variable "lambda_function_name_prod" {
  type    = string
  default = "fpl-ultimate-draft-scraper"
}

variable "lambda_function_name_stage" {
  type    = string
  default = "fpl-ultimate-draft-scraper-stage"
}

variable "fpl_draft_league_id" {
  description = "Draft league ID used by the prod Lambda."
  type        = string
  sensitive   = true
}

variable "fpl_draft_league_id_stage" {
  description = "Draft league ID used by the stage Lambda. Defaults to the same league as prod if left empty."
  type        = string
  default     = ""
  sensitive   = true
}

variable "github_repo" {
  description = "GitHub repo allowed to assume the deploy role via OIDC, as owner/repo."
  type        = string
  default     = "adambaldwin1st/fpl-ultimate-api"
}

locals {
  stage_league_id = var.fpl_draft_league_id_stage != "" ? var.fpl_draft_league_id_stage : var.fpl_draft_league_id
}
