data "aws_vpcs" "vpc" {
  filter {
    name   = "tag:Name"
    values = ["oso-dev"]
  }
}

data "aws_vpc" "vpc" {
  filter {
    name   = "tag:Name"
    values = ["oso-dev"]
  }
}

data "aws_subnet_ids" "private" {
  vpc_id = data.aws_vpc.vpc.id

  tags = {
    Type = "Private*"
  }
}

data "aws_subnet_ids" "public" {
  vpc_id = data.aws_vpc.vpc.id

  tags = {
    Type = "Public*"
  }
}