# Springboot Service
resource "aws_ecs_service" "springboot" {
  name            = "springboot"
  cluster         = aws_ecs_cluster.demo.id
  task_definition = aws_ecs_task_definition.springboot.arn
  desired_count   = 1
  iam_role        = aws_iam_role.ecs-service-role.arn
  depends_on      = [aws_iam_role_policy_attachment.ecs-service-attach]

  load_balancer {
    target_group_arn = aws_alb_target_group.springboot.id
    container_name   = "springboot"
    container_port   = "8080"
  }

  lifecycle {
    ignore_changes = [task_definition]
  }
}

resource "aws_ecs_task_definition" "springboot" {
  family = "springboot"

  container_definitions = <<EOF
[
  {
    "portMappings": [
      {
        "hostPort": 8080,
        "protocol": "tcp",
        "containerPort": 8080
      }
    ],
    "healthCheck": {
      "command": [
        "CMD-SHELL",
        "curl -f http://localhost/actuator/health || exit 1"
      ],
      "interval": 5
    },
    "cpu": 256,
    "memory": 300,
    "image": "docker.io/sionsmith/kafka-consumer-seek-example:latest",
    "essential": true,
    "name": "springboot",
    "logConfiguration": {
    "logDriver": "awslogs",
      "options": {
        "awslogs-group": "/ecs-demo/springboot",
        "awslogs-region": "eu-west-2",
        "awslogs-stream-prefix": "ecs"
      }
    }
  }
]
EOF

}

resource "aws_cloudwatch_log_group" "springboot" {
  name = "/ecs-demo/springboot"
}