resource "aws_route53_record" "prod" {
  zone_id = data.aws_route53_zone.primary.zone_id
  name    = var.prod_subdomain
  type    = "A"

  alias {
    name                   = aws_apigatewayv2_domain_name.prod.domain_name_configuration[0].target_domain_name
    zone_id                = aws_apigatewayv2_domain_name.prod.domain_name_configuration[0].hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "stage" {
  zone_id = data.aws_route53_zone.primary.zone_id
  name    = var.stage_subdomain
  type    = "A"

  alias {
    name                   = aws_apigatewayv2_domain_name.stage.domain_name_configuration[0].target_domain_name
    zone_id                = aws_apigatewayv2_domain_name.stage.domain_name_configuration[0].hosted_zone_id
    evaluate_target_health = false
  }
}
