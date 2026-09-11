# GitHub Actions OIDC — lets workflows in this repo assume an AWS role via
# short-lived tokens instead of long-lived access keys stored as secrets.
#
# NOTE: an AWS account can only have ONE GitHub OIDC provider
# (token.actions.githubusercontent.com). If this errors with
# "EntityAlreadyExists", another project already created one — remove this
# resource block and instead import the existing one:
#   terraform import aws_iam_openid_connect_provider.github <existing-arn>

resource "aws_iam_openid_connect_provider" "github" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  # AWS no longer actually validates this against GitHub's cert chain for
  # well-known OIDC providers, but the field still must be a well-formed
  # 40-char SHA1 hex thumbprint. Fetched live from the token endpoint's cert:
  #   echo | openssl s_client -servername token.actions.githubusercontent.com \
  #     -connect token.actions.githubusercontent.com:443 2>/dev/null | \
  #     openssl x509 -fingerprint -sha1 -noout
  thumbprint_list = ["06d927fecd0a84aeba28aad1d808139470fe95c3"]
}

data "aws_iam_policy_document" "gha_assume" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    # Any branch/ref/tag within this specific repo may assume the role —
    # needed since the stage workflow lets you pick an arbitrary branch to
    # deploy. Tightened to a single repo, not account-wide.
    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.github_repo}:*"]
    }
  }
}

resource "aws_iam_role" "gha_deploy" {
  name               = "fpl-ultimate-gha-deploy"
  assume_role_policy = data.aws_iam_policy_document.gha_assume.json
}

data "aws_iam_policy_document" "gha_deploy_permissions" {
  statement {
    actions = [
      "lambda:UpdateFunctionCode",
      "lambda:GetFunction",
      "lambda:GetFunctionConfiguration",
    ]
    resources = [
      aws_lambda_function.prod.arn,
      aws_lambda_function.stage.arn,
    ]
  }
}

resource "aws_iam_role_policy" "gha_deploy" {
  name   = "lambda-code-deploy"
  role   = aws_iam_role.gha_deploy.id
  policy = data.aws_iam_policy_document.gha_deploy_permissions.json
}
