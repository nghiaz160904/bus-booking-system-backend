package com.booking.service_registry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false",     // Disable Config Server fetch
    "spring.cloud.discovery.enabled=false",  // Disable Eureka Discovery
    "eureka.client.enabled=false"            // Disable Eureka Client registration
})
class ServiceRegistryApplicationTests {

	@Test
	void contextLoads() {
	}

}
