

This is the monolithic version of this demo.
For the micro-services version, please see the microservices branch: https://github.com/Pivotal-Field-Engineering/PCF-demo/tree/micro-services

PCF Demo
=========

Push the application initially with no service bound.
Notice it will alert no RabbitMQ service is bound.. the link "Stream Data" will not work either.

Now, bind a RabbitMQ service named "myrabbitmq". Re-push the app.
Click "Stream Data" and see the fun start. Click on a state to detail orders going throw it.

Additional fun: click "Kill App" and watch the application crashing.. it will show as "crashed" when you visualize events (cf events <app_name>). Health manager will automatically restart the app for you. => makes a good demo, too.

## Concourse CI Pipeline

Please take a look at the [ci](ci/README.md) folder to see the [Concourse](http://concourse.ci/) CI Pipeline.
