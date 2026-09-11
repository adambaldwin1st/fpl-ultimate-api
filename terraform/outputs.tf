output "gha_deploy_role_arn" {
  description = "Set this as the repo secret AWS_DEPLOY_ROLE_ARN in GitHub (Settings > Secrets and variables > Actions > Secrets)."
  value       = aws_iam_role.gha_deploy.arn
}

output "prod_url" {
  value = "https://${var.prod_subdomain}"
}

output "stage_url" {
  value = "https://${var.stage_subdomain}"
}

output "prod_lambda_function_name" {
  value = aws_lambda_function.prod.function_name
}

output "stage_lambda_function_name" {
  value = aws_lambda_function.stage.function_name
}
