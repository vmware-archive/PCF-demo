package com.pivotal.example.xd.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pivotal.example.xd.HeatMap;
import com.pivotal.example.xd.Order;
import com.pivotal.example.xd.OrderGenerator;
import com.pivotal.example.xd.RabbitClient;
import com.pivotal.example.xd.TimeItem;
import com.pivotal.example.xd.UserProvidedServiceInfo;

/**
 * Handles requests for the application home page.
 */
@Controller
public class OrderController {

	@Autowired
	ServletContext context;

	private static Map<String,Queue<Order>> stateOrdersMap = new HashMap<String, Queue<Order>>();
	private static RabbitClient client ;

	boolean generatingData = false;

	static Logger logger = Logger.getLogger(OrderController.class);

	OrderGenerator generator = new OrderGenerator();
	Thread threadSender = new Thread (generator);

    public OrderController(){

    	client = RabbitClient.getInstance();

    	for (int i=0; i<HeatMap.states.length; i++){
    		stateOrdersMap.put(HeatMap.states[i], new ArrayBlockingQueue<Order>(10));
    	}

    	if(client.getRabbitURI() != null){
    		threadSender.start();
        	client.startMessageListener();
        	client.startOrderProcessing();
    	}


    }

	private int getOrderSum(String state){

		int sum = 0;
		Queue<Order> q  = stateOrdersMap.get(state);
		Iterator<Order> it = q.iterator();
		while (it.hasNext()){
			sum += it.next().getAmount();
		}

		return sum;
	}



	public static synchronized void registerOrder(Order order){
		Queue<Order> orderQueue = stateOrdersMap.get(order.getState());
		if (!orderQueue.offer(order)){
			orderQueue.remove();
			orderQueue.add(order);
		}
	}

	@RequestMapping(value = "/")
	public String home(Model model) throws Exception{
		model.addAttribute("rabbitURI", client.getRabbitURI());

		ObjectMapper mapper = new ObjectMapper();

		//add details about VCAP APPLICATION
		if(System.getenv("VCAP_APPLICATION") != null){
			Map vcapMap = mapper.readValue(System.getenv("VCAP_APPLICATION"), Map.class);
			model.addAttribute("vcap_app", vcapMap);
		}

		//retrieve user-provided config credentials for branding purposes
		Cloud cloud = new CloudFactory().getCloud();
    	Iterator<ServiceInfo> services = cloud.getServiceInfos().iterator();
    	Map<String, Object> credentials = null;
    	while (services.hasNext()){
    		ServiceInfo svc = services.next();
    		if (svc.getId().equals("pcfdemo-config") && svc instanceof UserProvidedServiceInfo) {
    			UserProvidedServiceInfo service = (UserProvidedServiceInfo)svc;
    			credentials = service.getCredentials();
    		}
    	}
    	model.addAttribute("credentials", credentials);

    	/*
    	 * Quick & Dirty solution to get the service credentials from the environment directly and parse the JSON
		if(System.getenv("VCAP_SERVICES") != null){
			// Map of services
			Map vcapServMap = mapper.readValue(System.getenv("VCAP_SERVICES"), Map.class);
			model.addAttribute("credentials", ((Map)((List)vcapServMap.get("user-provided")).get(0)).get("credentials"));
		}
		*/


        return "WEB-INF/views/pcfdemo.jsp";
    }

    @RequestMapping(value="/getData")
    public @ResponseBody double getData(@RequestParam("state") String state){
    	if (!stateOrdersMap.containsKey(state)) return 0;
    	Queue<Order> q = stateOrdersMap.get(state);
    	if (q.size()==0) return 0;
    	Order[] orders = q.toArray(new Order[]{});
    	return orders[orders.length-1].getAmount();

    }

    @RequestMapping(value="/startStream")
    public @ResponseBody String startStream(){
		logger.warn("Rabbit URI "+client.getRabbitURI());
		if (client.getRabbitURI()==null) return "Please bind a RabbitMQ service";

    	if (generatingData) return "Data already being generated";

    	generatingData = true;

    	generator.startGen();
    	return "Started";

    }

    @RequestMapping(value="/stopStream")
    public @ResponseBody String stopStream(){
		logger.warn("Rabbit URI "+client.getRabbitURI());
		if (client.getRabbitURI()==null) return "Please bind a RabbitMQ service";

    	if (!generatingData) return "Not Streaming";
    	generatingData = false;
    	generator.stopGen();

    	return "Stopped";

    }

    @RequestMapping(value="/killApp")
    public @ResponseBody String kill(){
		logger.warn("Killing application instance");
		System.exit(-1);
    	return "Killed";

    }

    @RequestMapping(value="/getHeatMap")
    public @ResponseBody HeatMap getHistograms(){
    	HeatMap heatMap = new HeatMap();
    	for (int i=0; i<HeatMap.states.length; i++){
    		heatMap.addOrderSum(HeatMap.states[i], getOrderSum(HeatMap.states[i]));
    	}

    	heatMap.assignColors();
    	return heatMap;

    }

    @RequestMapping(value="/getTime")
    public @ResponseBody TimeItem getTime(){
			//mostly bad code that takes time
			String str = "";
			for (long i = 0; i < 100000000l; i++) {
				if(i%10000 == 0) {
					str = str + ".";
				}
			}
			logger.info("progress: " + str);
			return new TimeItem(System.currentTimeMillis(), Long.toString(System.currentTimeMillis()));
    }


    @PreDestroy
    public void shutdownThread(){

    	generator.shutdown();
    }


}
