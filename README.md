PCF Demo
=========

This is the refactoring for the PCF-Demo app built with two different micro-services: 

- The producer (generating orders) - written in Groovy with SpringBoot 
- The map (consuming orders and presenting on the heat map) - written in Java with Spring Web MVC.

The apps/micro-services are connected through a RabbitMQ instance of any name and all the information exchanged is JSON based.
The map will be active as the PCF-demo-producer micro-service is started. To freeze the map, just stop the service.

For convenience, a manifest which will push both micro-services at the same time is provided, assuming a RabbitMQ service called "rabbit" is created.

A script for pushing to PWS is also provided, which will create a space called "pcf-demo", create the service required and push both micro-services to it.



Enjoy!!
