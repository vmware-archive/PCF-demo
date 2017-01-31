package com.pivotal.example.xd;

import java.util.Map;

import org.springframework.cloud.service.UriBasedServiceInfo;

public class UserProvidedServiceInfo extends UriBasedServiceInfo {
    
    Map<String, Object> credentials = null;
      
        public UserProvidedServiceInfo(String id, String uri, Map<String, Object> credentials) {
                  super(id, uri);
                          this.credentials = credentials;
                              }
            
            public Map<String, Object> getCredentials() {
                    return this.credentials;
                        }
}
