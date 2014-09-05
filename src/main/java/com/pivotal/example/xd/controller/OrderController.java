package com.pivotal.example.xd.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pivotal.example.xd.BootstrapDataPopulator;
import com.pivotal.example.xd.HeatMap;
import com.pivotal.example.xd.Order;
import com.pivotal.example.xd.OrderConsumer;
import com.pivotal.example.xd.OrderGenerator;
import com.pivotal.example.xd.RabbitClient;

/**
 * Handles requests for the application home page.
 */
@Controller
public class OrderController {
	static Logger logger = Logger.getLogger(OrderController.class);
	
	@Autowired @Qualifier("gemfirexdDataSource") DataSource gemfirexdDataSource;
	@Autowired private RabbitClient rabbitClient;
	@Autowired OrderGenerator orderGenerator;
	@Autowired OrderConsumer orderConsumer;
	
	private static Map<String,Queue<Order>> stateOrdersMap = new HashMap<String, Queue<Order>>();

	boolean generatingData = false;
	
    public OrderController(){
    	
    	for (int i=0; i<HeatMap.states.length; i++){
    		stateOrdersMap.put(HeatMap.states[i], new ArrayBlockingQueue<Order>(10));
    	}
    }
    
    @PostConstruct
    public void init() {
	    	Thread threadSender = new Thread (orderGenerator);
	    	Thread threadConsumer = new Thread (orderConsumer);

	    	threadSender.start();
	    	threadConsumer.start();
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
    

	
	public synchronized void registerOrder(Order order){
		Queue<Order> orderQueue = stateOrdersMap.get(order.getState());
		if (!orderQueue.offer(order)){
			orderQueue.remove();
			orderQueue.add(order);
		}				
		// Persist to GemXD
		Connection conn = null;
		try {
			conn = gemfirexdDataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(BootstrapDataPopulator.INSERT_ORDER);
			pstmt.setString(1, order.getState());
			pstmt.setInt(2, order.getAmount());
			pstmt.executeUpdate();
			conn.commit();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try{
				if (conn!=null) conn.close();
			}catch(Exception e){}
		}
		
	}
    
	@RequestMapping(value = "/")
	public String home(Model model) {
		model.addAttribute("rabbitBound", rabbitClient.isBound());	
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
		logger.warn("Rabbit bound " + rabbitClient.isBound());
		if (!rabbitClient.isBound()) return "Please bind a RabbitMQ service";
    	
    	if (generatingData) return "Data already being generated";
    	
    	orderGenerator.startGen();
    	return "Started";

    }    	

    @RequestMapping(value="/stopStream")
    public @ResponseBody String stopStream(){
		logger.warn("Rabbit bound " + rabbitClient.isBound());
		if (!rabbitClient.isBound()) return "Please bind a RabbitMQ service";
    	
    	if (!generatingData) return "Not Streaming";
    	generatingData = false;
    	orderGenerator.stopGen();
    	return "Stopped";

    }    	
    
    @RequestMapping(value="/killApp")
    public @ResponseBody String kill(){
		logger.warn("Killing application instance");
		System.exit(-1);    	
    	return "Killed";

    }  
    
    @RequestMapping(value="/getOrders")
    public @ResponseBody String getOrders(){
		logger.warn("getOrders Called");
		try {
			Connection conn = gemfirexdDataSource.getConnection();
			//java.sql.DatabaseMetaData metadata = conn.getMetaData();
			//ResultSet rs = metadata.getTables(null, null, "ORDERS", null);
			PreparedStatement pstmt = conn.prepareStatement(BootstrapDataPopulator.SELECT_ORDER);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()){
				logger.warn("Row Num: " + rs.getRow());
				logger.warn(Integer.parseInt(rs.getString("SUM")) +  "\t" + rs.getString("STATE"));	
			}

		}catch (Exception e) {
			logger.error("Could not get Data from GemXD Table " + e);
		}
    	return "Logged data from Gem table";

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


}
