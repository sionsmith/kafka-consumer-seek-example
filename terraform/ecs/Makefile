#Makefile
.PHONY: init plan build destroy all

all: init plan build

init:
	rm -rf .terraform/modules/
	terraform init -reconfigure

plan: init
	terraform plan -refresh=true

build: init
	terraform apply -auto-approve

check: init
	terraform plan -detailed-exitcode

destroy: init
	terraform destroy --auto-approve

docs:
	terraform-docs md . > README.md

valid:
	tflint
	terraform fmt -check=true -diff=true