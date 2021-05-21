# ECS cluster
resource "aws_ecs_cluster" "demo" {
  name = "demo"
}

#Compute
resource "aws_autoscaling_group" "demo-cluster" {
  name                      = "demo-cluster"
  vpc_zone_identifier       = data.aws_subnet_ids.public.ids
  min_size                  = "2"
  max_size                  = "10"
  desired_capacity          = "2"
  launch_configuration      = aws_launch_configuration.demo-cluster-lc.name
  health_check_grace_period = 120
  default_cooldown          = 30
  termination_policies      = ["OldestInstance"]

  tag {
    key                 = "Name"
    value               = "ECS-demo"
    propagate_at_launch = true
  }
}

resource "aws_autoscaling_policy" "demo-cluster" {
  name                      = "demo-ecs-auto-scaling"
  policy_type               = "TargetTrackingScaling"
  estimated_instance_warmup = "90"
  adjustment_type           = "ChangeInCapacity"
  autoscaling_group_name    = aws_autoscaling_group.demo-cluster.name

  target_tracking_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ASGAverageCPUUtilization"
    }

    target_value = 40
  }
}

resource "aws_launch_configuration" "demo-cluster-lc" {
  name_prefix     = "demo-cluster-lc"
  security_groups = [aws_security_group.instance_sg.id]

  image_id                    = data.aws_ami.latest_ecs.id
  instance_type               = "t3.medium"
  iam_instance_profile        = aws_iam_instance_profile.ecs-ec2-role.id
  user_data                   = data.template_file.ecs-cluster.rendered
  associate_public_ip_address = true

  lifecycle {
    create_before_destroy = true
  }
}