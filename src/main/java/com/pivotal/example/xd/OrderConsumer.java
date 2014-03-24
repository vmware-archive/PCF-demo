package com.pivotal.example.xd;

import com.pivotal.example.xd.controller.OrderController;
import com.rabbitmq.client.QueueingConsumer;

public class OrderConsumer implements Runnable {

	@Override
	public void run() {

		RabbitClient client = RabbitClient.getInstance();
		try{
			QueueingConsumer consumer = client.consumeOrders();
			while (true){
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      Order order = Order.fromBytes(delivery.getBody());
		      OrderController.registerOrder(order);
			}
			
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}

}
