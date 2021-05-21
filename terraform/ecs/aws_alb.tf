## ALB
resource "aws_alb" "demo_eu_alb" {
  name            = "demo-eu-alb"
  subnets         = data.aws_subnet_ids.public.ids
  security_groups = [aws_security_group.lb_sg.id]
  enable_http2    = "true"
  idle_timeout    = 600
}

resource "aws_alb_listener" "front_end" {
  load_balancer_arn = aws_alb.demo_eu_alb.id
  port              = "80"
  protocol          = "HTTP"

  default_action {
    target_group_arn = aws_alb_target_group.springboot.id
    type             = "forward"
  }
}

resource "aws_alb_target_group" "springboot" {
  name       = "springboot"
  port       = 80
  protocol   = "HTTP"
  vpc_id     = data.aws_vpc.vpc.id
  depends_on = [aws_alb.demo_eu_alb]

  stickiness {
    type            = "lb_cookie"
    cookie_duration = 86400
  }

  health_check {
    path                = "/"
    healthy_threshold   = 2
    unhealthy_threshold = 10
    timeout             = 60
    interval            = 300
    matcher             = "200,301,302"
  }
}
