package com.pivotal.example.xd;

import javax.sql.DataSource;

import org.apache.hadoop.conf.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class CloudConfig extends AbstractCloudConfig {

	@Bean
	public ConnectionFactory rabbitConnectionFactory() {
		return connectionFactory().rabbitConnectionFactory();
	}
	
	@Bean
	public DataSource hawqDataSource() {
		return connectionFactory().dataSource("phd-service/hawq");
	}

	@Bean
	public DataSource gemfirexdDataSource() {
		return connectionFactory().dataSource("phd-service/gemfirexd");
	}
	
	@Bean
	public Configuration hadoopConfiguration() {
		return connectionFactory().service(Configuration.class);
	}

}
