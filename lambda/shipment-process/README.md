## Lambda Architecture

The lambda has been added as sample output for demo'ing the payload ouput. I have used the Chalice framework which only currently supports Python 3.8.act

### Local development environment setup
```bash
Created python 3.8 virtual environment
pip install chalice 

TODO: add commands
```

### Deploy test shipment function 
```shell
export AWS_DEFAULT_REGION=eu-west-2
export AWS_ACCESS_KEY_ID=xxxx
export AWS_SECRET_ACCESS_KEY=xxxx
chalice deploy
```

sample output shoudl look something like this:
```shell
➜  shipment-process git:(main) ✗ chalice deploy
Creating deployment package.
Updating policy for IAM role: shipment-process-dev
Updating lambda function: shipment-process-dev-index
Resources deployed:
  - Lambda ARN: arn:aws:lambda:eu-west-2:xxxx:function:shipment-process-dev-index
```