package io.pivotal.cf.sample;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.RabbitServiceInfo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RabbitClient {

	static Logger logger = Logger.getLogger(RabbitClient.class);
	private static RabbitClient instance;
	
	private static final String QUEUE_NAME="ORDERS_QUEUE";

	private ConnectionFactory factory;
	private Connection receiverConn;
	private String rabbitURI;
	
	private RabbitClient(){
		
    	try{
    		Cloud cloud = new CloudFactory().getCloud();
	    	Iterator<ServiceInfo> services = cloud.getServiceInfos().iterator();
	    	while (services.hasNext()){
	    		ServiceInfo svc = services.next();
	    		if (svc instanceof RabbitServiceInfo){
	    			RabbitServiceInfo rabbitSvc = ((RabbitServiceInfo)svc);	    			
	    			rabbitURI=rabbitSvc.getUri();
	    			
	    			try{
	    				factory = new ConnectionFactory();
	    				factory.setUri(rabbitURI);
	    				receiverConn = factory.newConnection();
	    			}
	    			catch(Exception e){
	    				throw new RuntimeException("Exception connecting to RabbitMQ",e);
	    			}
	    			
	    		}
	    	}
    	}
    	catch(CloudException ce){
    		// means its not being deployed on Cloud
    		logger.warn(ce.getMessage());
    	}
		
		
	}
	
	public static synchronized RabbitClient getInstance(){
		if (instance==null){
			instance = new RabbitClient(); 
		}
		return instance;
	}
	

	
	public synchronized QueueingConsumer consumeOrders() throws IOException{
		if (receiverConn==null || !receiverConn.isOpen()){
			receiverConn = factory.newConnection();
		}
		Channel channel = receiverConn.createChannel();
	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(QUEUE_NAME, true, consumer);
	    return consumer;
	}
	
	public boolean isBound(){
		return (rabbitURI!=null);
	}
	
	public String getRabbitURI(){
		return rabbitURI;
	}
}
