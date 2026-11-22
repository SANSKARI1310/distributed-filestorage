package com.example.demo.config;

import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class LoadBalancerConfig {
    
    @Bean
    public ServiceInstanceListSupplier
        serviceInstanceListSupplier(ConfigurableApplicationContext context)
        {
              return ServiceInstanceListSupplier.builder()
                .withDiscoveryClient()
                .withHealthChecks()
                .build(context);
        }
}
