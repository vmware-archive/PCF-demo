package com.pivotal.example.xd;

import java.util.Map;

import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

public class UserProvidedServiceInfoCreator extends CloudFoundryServiceInfoCreator<UserProvidedServiceInfo> {

    public UserProvidedServiceInfoCreator() {
          super(new Tags(), "pcfdemo");
            }

      @Override
          public UserProvidedServiceInfo createServiceInfo(Map<String, Object> serviceData) {
                String id = (String) serviceData.get("name");

                        Map<String, Object> credentials = getCredentials(serviceData);
                                String uri = getUriFromCredentials(credentials);
                                        return new UserProvidedServiceInfo(id, uri, credentials);
                                          }


}
