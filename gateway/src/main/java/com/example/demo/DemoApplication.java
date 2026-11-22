package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import com.example.demo.config.LoadBalancerConfig;

@SpringBootApplication
@LoadBalancerClient
(
	name = "core-file-service",
	configuration = LoadBalancerConfig.class
)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
