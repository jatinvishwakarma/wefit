package com.wefit.relationshipService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RelationshipServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(RelationshipServiceApplication.class, args);
	}
}
