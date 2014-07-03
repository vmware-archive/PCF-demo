package com.pivotal.example.xd;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.log4j.Logger;
import org.cloudfoundry.runtime.env.CloudEnvironment;

public class DataSourceConnManager {

	private static DataSourceConnManager instance;
	private static final String PHD_SERVICE_NAME="phd1";

	static Logger logger = Logger.getLogger(DataSourceConnManager.class);

	private DataSource gemXD;
	private DataSource hawq;
	
	private String nameNode;



	public static synchronized DataSourceConnManager getInstance(){
		if (instance==null){
			instance = new DataSourceConnManager();
		}
		return instance;
	}
	
	private DataSourceConnManager(){
		
    	CloudEnvironment env = new CloudEnvironment();
    	Map phdProps = env.getServiceDataByName(PHD_SERVICE_NAME);
    	Iterator it = phdProps.keySet().iterator();
    	while (it.hasNext()){
    		String key = it.next().toString();
    		logger.warn("phd1 - "+key+" : "+phdProps.get(key)+" "+phdProps.get(key).getClass());
    	}
    	
    	try{

    		Map phdCred = (Map)env.getServiceDataByName(PHD_SERVICE_NAME).get("credentials");
    		Map gemXDCred = (Map)phdCred.get("gemfirexd");
    		String uri = (String)gemXDCred.get("uri");
    		
    		StringTokenizer tokenizer = new StringTokenizer(uri, ";");
    		String gemXDURI = tokenizer.nextToken();
    		String gemXDUser = tokenizer.nextToken().substring(5);  // remove the "user=" string
    		String gemXDPass = tokenizer.nextToken().substring(9);  //  remove the "password=" string
    		
    		
    		
    		Map hdfsCred = (Map)phdCred.get("hdfs");
    		Map hdfsConfig = (Map)hdfsCred.get("configuration");
    		nameNode=(String)hdfsConfig.get("fs.defaultFS");
    		
    		
    		Map hawqCred = (Map)phdCred.get("hawq");
    		uri = (String)hawqCred.get("uri");    		
    		tokenizer = new StringTokenizer(uri, "?");
    		
    		String hawqURI = tokenizer.nextToken();
    		String userPass = tokenizer.nextToken();

    		String hawqUser = userPass.substring(0,userPass.indexOf("&")).substring(5);
    		String hawqPass = userPass.substring(userPass.indexOf("&")+1).substring(9); 
    		
    		
    		
    		logger.warn("GemXD URI: "+gemXDURI);
    		logger.warn("GemXD User: "+gemXDUser);
    		logger.warn("GemXD Password: "+gemXDPass);
    		
    		logger.warn("HAWQ URI: "+hawqURI);
    		logger.warn("HAWQ User: "+hawqUser);
    		logger.warn("HAWQ Password: "+hawqPass);
    		
    		logger.warn("HDFS Namenode: "+nameNode);
    		
    		// GEMXD
    		
       		Properties props = new Properties();
    		props.setProperty("driverClassName","com.pivotal.gemfirexd.internal.jdbc.ClientConnectionPoolDataSource");
    		props.setProperty("url",gemXDURI);	
    		props.setProperty("username",gemXDUser);	
    		props.setProperty("password",gemXDPass);	
    		
			gemXD = BasicDataSourceFactory.createDataSource(props);
			
			// HAWQ
			props = new Properties();
    		props.setProperty("driverClassName","org.postgresql.Driver");
    		props.setProperty("url",hawqURI);	
    		props.setProperty("username",hawqUser);	
    		props.setProperty("password",hawqPass);	    		
    		
    		hawq = BasicDataSourceFactory.createDataSource(props);
    		
    	}
    	catch(Exception e){
    		logger.fatal("Exception ",e);
    		e.printStackTrace();
    	}
    	
		
	}

	public String getHDFSNameNode(){
		return nameNode;
	}
	    
	public DataSource getGemXDDataSource(){
		return gemXD;
	}
    
	public DataSource getHawqDataSource(){
		return hawq;
	}
	
}
