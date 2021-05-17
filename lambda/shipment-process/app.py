from chalice import Chalice

app = Chalice(app_name='shipment-process')
app.debug = True

@app.lambda_function()
def index(event, context):
    app.log.debug(event)
