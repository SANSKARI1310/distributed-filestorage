package com.example.demo.filter;

import java.time.Instant;
public record TenantRequest(
    String organizationId ,
    Instant arrivalTime 
) {
    
}

