package com.pivotal.example.xd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pivotal.example.xd.controller.OrderController;
import com.rabbitmq.client.QueueingConsumer;

@Component
public class OrderConsumer implements Runnable {

	@Autowired OrderController orderController;
	@Autowired RabbitClient rabbitClient;
	
	@Override
	public void run() {

		try{
			QueueingConsumer consumer = rabbitClient.consumeOrders();
			while (true){
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      Order order = Order.fromBytes(delivery.getBody());
		      orderController.registerOrder(order);
			}
			
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}

}
