package com.pivotal.example.xd.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pivotal.example.xd.BootstrapDataPopulator;
import com.pivotal.example.xd.Customer;

@Controller
public class CustomerController {
	@Autowired @Qualifier("hawqDataSource") DataSource hawqDataSource;

	static Logger logger = Logger.getLogger(CustomerController.class);

	@RequestMapping("/getCustomer")
	public @ResponseBody Customer getCustomer(@RequestParam(value="lName", required=false, defaultValue="Test")String last_name) {
		//get Customer from HAWQ table
		Customer cust = null;
		try {
			Connection conn = null;
			conn = hawqDataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(BootstrapDataPopulator.SELECT_CUSTOMER_LNAME);
			pstmt.setString(1, last_name);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()){
				logger.warn("Row Num: " + rs.getRow());
				logger.warn(Integer.parseInt(rs.getString("customer_id")) +  "\t" + rs.getString("first_name") + "\t" + rs.getString("last_name") + "\t" + rs.getString("gender"));
				cust = new Customer(rs.getString("first_name"), rs.getString("last_name"), rs.getString("gender"), Integer.parseInt(rs.getString("customer_id")));	
			}
			
		} catch (Exception e){
			logger.error("Cannot get data from HAWQ table", e);
		}
		
		//return new Customer(first_name, last_name,  gender, id);
		return cust;
	}
	
	@RequestMapping("/getAllCustomers")
	public @ResponseBody String getAllCustomers() {
		//get all Customers from HAWQ table
		String cust = "";
		try {
			Connection conn = hawqDataSource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(BootstrapDataPopulator.SELECT_CUSTOMERS);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()){
				logger.warn("Row Num: " + rs.getRow());
				logger.warn(Integer.parseInt(rs.getString("customer_id")) +  "\t" + rs.getString("first_name") + "\t" + rs.getString("last_name") + "\t" + rs.getString("gender"));
				cust += new Customer(rs.getString("first_name"), rs.getString("last_name"), rs.getString("gender"), Integer.parseInt(rs.getString("customer_id"))).toString();
			}
		} catch (Exception e){
			logger.error("Cannot get data from HAWQ table", e);
		}
		
		return cust;
	}
}
