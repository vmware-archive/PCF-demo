package com.pivotal.example.xd;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.pivotal.example.xd.controller.OrderController;
import com.rabbitmq.client.QueueingConsumer;
import java.util.logging.Logger;


public class OrderConsumer implements Runnable {

	JSONParser parser;
    private final Logger LOG = Logger.getLogger(OrderConsumer.class.getName());

	
	public OrderConsumer(){
		
		 parser = new JSONParser();  
		
	}
	@Override
	public void run() {

		RabbitClient client = RabbitClient.getInstance();
		try{
			QueueingConsumer consumer = client.consumeOrders();
			while (true){
		      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		      
		      String message = new String(delivery.getBody());   
		      
		      JSONObject obj = (JSONObject) parser.parse(message);
		      
		      Order order = new Order();
		      order.setAmount(((Number)obj.get("amount")).intValue());
		      order.setState((String)obj.get("state"));
		     
		      OrderController.registerOrder(order);
			}
			
		}
		catch(Exception e){
			LOG.warning(e.getMessage());
			LOG.throwing(this.getClass().getName(),"consume", e);
			throw new RuntimeException(e);
		}
		
	}

}
