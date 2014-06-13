package com.pivotal.example.xd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.log4j.Logger;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pivotal.example.xd.controller.OrderController;
import com.pivotal.gemfirexd.internal.jdbc.ClientConnectionPoolDataSource;

@Service 
public class BootstrapDataPopulator implements InitializingBean {

	static Logger logger = Logger.getLogger(OrderController.class);
	
	private DataSource ds;
	String gemXDURI = null;
	String gemXDUser = null;
	String gemXDPass = null;
	String nameNode= null;
	String dir = null;
		
	
	private static final String CREATE_DISK_STORE_DDL="" +
			" CREATE HDFSSTORE streamingstore " +
			" NameNode '_NAMENODE_' " +
			" HomeDir '_DIR_' " + 
			" BatchSize 10 "+
			" QueuePersistent true "+
			" MaxWriteOnlyFileSize 200;";
			
	private static final String CREATE_TABLE_DDL="" +
			" CREATE TABLE ORDERS " +
			" (ORDER_ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
			"  STATE VARCHAR(2) NOT NULL, " +
			"  VALUE INT NOT NULL ) " +
			" PARTITION BY PRIMARY KEY " +
			" EXPIRE ENTRY WITH TIMETOLIVE 300 ACTION DESTROY "+
			" HDFSSTORE (streamingstore) WRITEONLY;" ; 
	
	
	
	
    public BootstrapDataPopulator(){

    	CloudEnvironment env = new CloudEnvironment();
    	Map phdProps = env.getServiceDataByName("phd1");
    	Iterator it = phdProps.keySet().iterator();
    	while (it.hasNext()){
    		String key = it.next().toString();
    		
    		logger.warn("phd1 - "+key+" : "+phdProps.get(key)+" "+phdProps.get(key).getClass());
    	}
    	
    	try{

    		Map phdCred = (Map)env.getServiceDataByName("phd1").get("credentials");
    		Map gemXDCred = (Map)phdCred.get("gemfirexd");
    		String uri = (String)gemXDCred.get("uri");
    		
    		StringTokenizer tokenizer = new StringTokenizer(uri, ";");
    		gemXDURI = tokenizer.nextToken();
    		gemXDUser = tokenizer.nextToken().substring(5);  // remove the "user=" string
    		gemXDPass = tokenizer.nextToken().substring(9);  //  remove the "password=" string
    		
    		
    		Map hdfsCred = (Map)phdCred.get("hdfs");
    		Map hdfsConfig = (Map)hdfsCred.get("configuration");
    		nameNode=(String)hdfsConfig.get("fs.defaultFS");
    		dir=(String)hdfsCred.get("directory");
    		
    		
    		logger.warn("GemXD URI: "+gemXDURI);
    		logger.warn("GemXD User: "+gemXDUser);
    		logger.warn("GemXD Password: "+gemXDPass);
    		
    		logger.warn("HDFS Config: "+hdfsConfig);
    		logger.warn("Namenode: "+nameNode);
    		logger.warn("dir: "+dir);
    		
    		
    		
    		
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	

    	
  /*  	
    	try{
    		Cloud cloud = new CloudFactory().getCloud();
    		Properties props = cloud.getCloudProperties();
    		Enumeration propEnum = props.keys();
    		while (propEnum.hasMoreElements()){
    			Object key = propEnum.nextElement();    			
    			logger.warn(key+" : "+props.get(key));
    		}
    		
	    	Iterator<ServiceInfo> services = cloud.getServiceInfos().iterator();
	    	while (services.hasNext()){
	    		ServiceInfo svc = services.next();
	    		if (svc instanceof PostgresqlServiceInfo){
	    			PostgresqlServiceInfo hawq = (PostgresqlServiceInfo)svc;	    				    			
	    			
	    		}
	    	}
    	}
    	catch(CloudException ce){
    		// means its not being deployed on Cloud
    		logger.warn(ce.getMessage());
    	}
    	*/
    }
    
	private DataSource getDataSource() throws Exception{
   		Properties props = new Properties();
		props.setProperty("driverClassName","com.pivotal.gemfirexd.internal.jdbc.ClientConnectionPoolDataSource");
		props.setProperty("url",gemXDURI);	
		props.setProperty("username",gemXDUser);	
		props.setProperty("password",gemXDPass);	
		
		
		return BasicDataSourceFactory.createDataSource(props);
 		
		
	}
	    
    
    @Transactional
    @Override
    public void afterPropertiesSet() throws Exception {
        logger.warn("Bootstrapping data...");
 
        // Create HDFS Disk Store if not existing.
        if (ds==null){
        	ds = getDataSource();
        }
        
        //Connection conn = ds.getConnection(); 
        ClientConnectionPoolDataSource ds = new ClientConnectionPoolDataSource();
        Connection conn = DriverManager.getConnection(gemXDURI, gemXDUser, gemXDPass);
        
        try{
        	String ddl = CREATE_DISK_STORE_DDL.replaceAll("_NAMENODE_", nameNode).replaceAll("_DIR_", dir);
        	logger.warn("EXECUTING DDL: "+ddl);
	        conn.createStatement().executeUpdate(ddl);
	        conn.commit();
	        logger.warn("CREATED DISK STORE");
        }
        catch(Exception e){
        	logger.fatal("Exception trying to create hdfs disk store", e);
        	//e.printStackTrace();
        }
        finally{
        	conn.close();
        }
        // Create DB tables if not existing.

        logger.warn("...Bootstrapping completed");
    }

   
}