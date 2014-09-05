package com.pivotal.example.xd;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

@Service
public class RabbitClient {

	static Logger logger = Logger.getLogger(RabbitClient.class);
	
	private static final String QUEUE_NAME="ORDERS_QUEUE";

	private @Autowired(required=false) ConnectionFactory factory;
	private Connection senderConn;
	private Connection receiverConn;
	
	@PostConstruct
	public void init() {
		if (factory != null) {
			senderConn = factory.createConnection();
			receiverConn = factory.createConnection();
		}
	}
	
	public synchronized void post(Order order) throws IOException{
		if (senderConn==null || !senderConn.isOpen()){
			senderConn = factory.createConnection();
		}
		Channel channel = senderConn.createChannel(false);
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    channel.basicPublish("", QUEUE_NAME, null, order.toBytes());
	    channel.close();		
	}
	
	public synchronized QueueingConsumer consumeOrders() throws IOException{
		if (receiverConn==null || !receiverConn.isOpen()){
			receiverConn = factory.createConnection();
		}
		Channel channel = receiverConn.createChannel(false);
	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(QUEUE_NAME, true, consumer);
	    return consumer;
	}
	
	public boolean isBound(){
		return factory!=null;
	}
}
