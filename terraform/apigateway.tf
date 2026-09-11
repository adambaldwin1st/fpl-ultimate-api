resource "aws_apigatewayv2_api" "prod" {
  name          = "fpl-ultimate-draft-scraper-prod"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_integration" "prod" {
  api_id                 = aws_apigatewayv2_api.prod.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.prod.invoke_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "prod" {
  api_id    = aws_apigatewayv2_api.prod.id
  route_key = "$default"
  target    = "integrations/${aws_apigatewayv2_integration.prod.id}"
}

resource "aws_apigatewayv2_stage" "prod" {
  api_id      = aws_apigatewayv2_api.prod.id
  name        = "$default"
  auto_deploy = true
}

resource "aws_apigatewayv2_api" "stage" {
  name          = "fpl-ultimate-draft-scraper-stage"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_integration" "stage" {
  api_id                 = aws_apigatewayv2_api.stage.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.stage.invoke_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "stage" {
  api_id    = aws_apigatewayv2_api.stage.id
  route_key = "$default"
  target    = "integrations/${aws_apigatewayv2_integration.stage.id}"
}

resource "aws_apigatewayv2_stage" "stage" {
  api_id      = aws_apigatewayv2_api.stage.id
  name        = "$default"
  auto_deploy = true
}

resource "aws_apigatewayv2_domain_name" "prod" {
  domain_name = var.prod_subdomain

  domain_name_configuration {
    certificate_arn = aws_acm_certificate_validation.api.certificate_arn
    endpoint_type   = "REGIONAL"
    security_policy = "TLS_1_2"
  }
}

resource "aws_apigatewayv2_domain_name" "stage" {
  domain_name = var.stage_subdomain

  domain_name_configuration {
    certificate_arn = aws_acm_certificate_validation.api.certificate_arn
    endpoint_type   = "REGIONAL"
    security_policy = "TLS_1_2"
  }
}

resource "aws_apigatewayv2_api_mapping" "prod" {
  api_id      = aws_apigatewayv2_api.prod.id
  domain_name = aws_apigatewayv2_domain_name.prod.id
  stage       = aws_apigatewayv2_stage.prod.id
}

resource "aws_apigatewayv2_api_mapping" "stage" {
  api_id      = aws_apigatewayv2_api.stage.id
  domain_name = aws_apigatewayv2_domain_name.stage.id
  stage       = aws_apigatewayv2_stage.stage.id
}
