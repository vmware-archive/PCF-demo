package com.pivotal.example.xd;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pivotal.example.xd.controller.OrderController;

@Service 
public class BootstrapDataPopulator implements InitializingBean {

	static Logger logger = Logger.getLogger(BootstrapDataPopulator.class);
	
	private DataSource ds;
	String gemXDURI = null;
	String gemXDUser = null;
	String gemXDPass = null;
	String nameNode= null;
	String dir = null;
		
	//@Autowired 
	//private ApplicationContext applicationContext;	
	
	public static final String CREATE_DISK_STORE_DDL="" +
			" CREATE HDFSSTORE streamingstore " +
			" NameNode '_NAMENODE_' " +
			" HomeDir '/user/gfxd/' " + 
			" BatchSize 10 "+
			" QueuePersistent true "+
			" MaxWriteOnlyFileSize 200;";
			
	public static final String CREATE_TABLE_DDL="" +
			" CREATE TABLE ORDERS " +
			" (ORDER_ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
			"  STATE VARCHAR(2) NOT NULL, " +
			"  VALUE INT NOT NULL ) " +
			" PARTITION BY PRIMARY KEY " +
			//" EXPIRE ENTRY WITH TIMETOLIVE 300 ACTION DESTROY "+
			" HDFSSTORE (streamingstore) WRITEONLY;" ; 
	
	public static final String INSERT_ORDER="" +
			" INSERT INTO ORDERS (STATE, VALUE) VALUES (?,?);" ; 
	
	public static final String SELECT_ORDER="" +
			" select STATE,sum(VALUE) AS \"SUM\" from orders group by STATE" ; 	
	
	public static final String CREATE_HAWQ_TABLE_DDL="" +
			" CREATE TABLE customers " +
			"( " +
			"customer_id TEXT," +
			"first_name TEXT," +
			"last_name TEXT," +
			"gender TEXT" +
			") " +
			"WITH (appendonly=true, compresstype=quicklz) DISTRIBUTED RANDOMLY;" ;
	
	public static final String INSERT_CUSTOMER="" +
			" INSERT INTO CUSTOMERS (customer_id, first_name, last_name, gender) VALUES (?,?,?,?);" ;
	
    public BootstrapDataPopulator(){


    
    }
    
	private DataSource getDataSource() throws Exception{

		return DataSourceConnManager.getInstance().getGemXDDataSource();
 		
		
	}
	 
	private DataSource getHawqDataSource() throws Exception {
		return DataSourceConnManager.getInstance().getHawqDataSource();
	}
    
    @Transactional
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.warn("Bootstrapping data...");
 
        if (ds==null){
        	ds = getDataSource();        	
        	// make the DS available to other Spring beans.
        	//AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();
        	//factory.autowireBean(ds);
        }
        
        Connection conn = ds.getConnection(); 
        nameNode = DataSourceConnManager.getInstance().getHDFSNameNode();
        // Create HDFS Disk Store if not existing.        
        try{
        	String ddl = CREATE_DISK_STORE_DDL.replaceAll("_NAMENODE_", nameNode);
        	logger.warn("EXECUTING DDL: "+ddl);
	        conn.createStatement().executeUpdate(ddl);
	        logger.warn("CREATED DISK STORE");
        }
        catch(Exception e){
        	logger.fatal("Exception trying to create hdfs disk store. Maybe it already exists?",e);
        }
        
        // check if table already exists
        java.sql.DatabaseMetaData metadata = conn.getMetaData();
       
        ResultSet rs = metadata.getTables(null, null, "ORDERS", null);
        if (rs.next()){
        	logger.warn("ORDERS TABLE ALREADY EXISTS.. SKIPPING CREATION. ");	        
        }
        else{
	        try{
		        String ddl = CREATE_TABLE_DDL;
	        	logger.warn("EXECUTING DDL: "+ddl);
		        conn.createStatement().executeUpdate(ddl);
		        logger.warn("CREATED TABLE");
		        conn.commit();
	        }
	        catch(Exception e){
	        	logger.fatal("Exception trying to create table", e);
	        	//e.printStackTrace();
	        }
        }
    	conn.close();
    	//Creating a HAWQ Table
    	ds = getHawqDataSource();
    	conn = ds.getConnection();
    	try {
    		String ddl = CREATE_HAWQ_TABLE_DDL;
        	logger.warn("EXECUTING DDL: "+ddl);
	        conn.createStatement().executeUpdate(ddl);
	        logger.warn("CREATED TABLE");
	        
    		
    	} catch (Exception e) {
    		logger.error("Error creating HAWQ table", e);
    	}
    		
        logger.warn("...Bootstrapping completed");
    }

   
}