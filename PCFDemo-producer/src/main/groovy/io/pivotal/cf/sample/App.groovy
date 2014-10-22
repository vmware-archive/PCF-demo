package io.pivotal.cf.sample


@GrabResolver(name='Spring Snapshot', root='http://repo.spring.io/snapshot')

@Grab('org.springframework.cloud:cloudfoundry-connector:0.9.6.BUILD-SNAPSHOT')
//@Grab('org.springframework.cloud:spring-service-connector:0.9.6.BUILD-SNAPSHOT')
@Grab('com.googlecode.json-simple:json-simple:1.1')

import java.io.IOException;
import java.util.Random;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.cloud.Cloud
import org.springframework.cloud.CloudFactory
import org.springframework.web.bind.annotation.RestController


beans {
	cloudFactory(CloudFactory)
	cloud(cloudFactory: "getCloud")
}

@RestController
@EnableAutoConfiguration

class WebApplication implements CommandLineRunner{

	String[] states = [
		"ca", "ny", "ma", "tx", "il", "wa", "fl", "pa", "va", "nj", "or", "oh", "mi", "co", "md", "nc", "ga",
		"mn", "az", "in", "wi", "mo", "tn", "ct", "dc", "ut", "nm", "ks", "ky", "ok", "sc", "la", "nv", "ia",
		"nh", "al", "ar", "me", "hi", "ne", "id", "ri", "vt", "mt", "wv", "de", "ak", "ms", "wy", "sd", "nd",
		"pr", "as"]
	
		@Autowired
		Cloud cloud
			
		
		@Override
		void run(String... args) {
			println "Started..."
			
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					RabbitClient client = RabbitClient.getInstance();
					while (true){
						Random random = new Random()
						String state = states[random.nextInt(states.length)]
						int value = (1+random.nextInt(4))*10
						
						JSONObject obj = new JSONObject();
						obj.put("amount", value);
						obj.put("state", state);
						try {
							client.post(obj);
						} catch (IOException e1) {
							throw new RuntimeException(e1);
						}
					
						try{
						   Thread.sleep(10);
					    }
					    catch(Exception e){ return; }
					}
	
				}
			});
			
			thread.start();
		}
		

}
