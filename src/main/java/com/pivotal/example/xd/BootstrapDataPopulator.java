package com.pivotal.example.xd;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import org.apache.log4j.Logger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pivotal.example.xd.controller.OrderController;

@Service
public class BootstrapDataPopulator implements InitializingBean {

	static Logger logger = Logger.getLogger(OrderController.class);

    public BootstrapDataPopulator(){
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
    	
    }
    
    @Override
    @Transactional()
    public void afterPropertiesSet() throws Exception {
        logger.info("Bootstrapping data...");

        // Create DB table

        logger.info("...Bootstrapping completed");
    }

   
}