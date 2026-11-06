package com.example.demo.filter;

import java.time.Instant;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class QueuedTenantRequest {
 
    private final String organizationId;
    private final Instant arrivalTime;

    private final Sinks.One<Void> admissionSignal = Sinks.one();

    public QueuedTenantRequest(String organizationId , Instant arrivalTime){
        this.organizationId =  organizationId;
        this.arrivalTime = arrivalTime;
    }

    public String getOrganizationId()
    {
        return organizationId;
    }
     
    public Instant getArrivalTime()
    {
        return arrivalTime;
    }

    public Mono<Void> waitForAdmission()
    {
        return admissionSignal.asMono();
    }

    public void admit()
    {
        return admissionSignal.tryEmitEmpty();
    }
}
