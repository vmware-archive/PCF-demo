PCF Demo
=========

This is the refactoring for the PCF-Demo app built with two different micro-services: 

- The producer (generating orders) - written in Groovy with SpringBoot 
- The map (consuming orders and presenting on the heat map) - written in Java with Spring Web MVC.

The apps/micro-services are connected through a RabbitMQ instance of any name and all the information exchanged is JSON based.
The map will be active as the PCF-demo-producer micro-service is started. To freeze the map, just stop the service.

For convenience, a manifest which will push both micro-services at the same time is provided, assuming a RabbitMQ service called "myrabbit" is created.

A script for pushing to PWS is also provided, which will create a space called "pcf-demo", create the service required and push both micro-services to it.

Instructions for deployment on PCF
- cf api [your cf api url]
- cf login 
- cf create-service p-rabbitmq standard myrabbit (for PWS: "cf create-service cloudamqp lemur myrabbit")
- cf push


Remember:  free RabbitMQ service on PWS (a.k.a. "CloudAMQP" plan "lemur") is limited to 3 connections max. For that, you can demo two instances of the map (still able to show self-healing and load-balancing) and one instance of the producer.

For deploying this demo through Jenkins, see instructions here: https://github.com/Pivotal-Field-Engineering/PCF-demo/blob/micro-services/Deploy-microservices-CD.adoc

Enjoy!!
