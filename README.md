PCF Demo
=========

Push the application initially with no service bound.
Notice it will alert no RabbitMQ service is bound.. the link "Stream Data" will not work either.

Now, bind a RabbitMQ service. Re-push the app.
Click "Stream Data" and see the fun start. Click on a state to detail orders going throw it.

Additional fun: click "Kill App" and watch the application crashing.. it will show as "crashed" when you visualize events (cf events <app_name>). Health manager will automatically restart the app for you. => makes a good demo, too.
