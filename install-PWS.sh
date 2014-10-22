cf create-space pcf-demo
cf target -s pcf-demo
cf create-service cloudamqp lemur rabbit
cf push
