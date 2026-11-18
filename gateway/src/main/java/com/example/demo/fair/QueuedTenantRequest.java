package com.example.demo.fair;

import java.time.Instant;

import com.example.demo.metrics.SchedulerMetrics;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class QueuedTenantRequest {
 
    private final String organizationId;
    private final Instant arrivalTime;
    private final long arrivalNanos;
    private final SchedulerMetrics schedulerMetrics;

    private final Sinks.One<Void> admissionSignal = Sinks.one();

    public QueuedTenantRequest(String organizationId , Instant arrivalTime , SchedulerMetrics schedulerMetrics){
        this.organizationId =  organizationId;
        this.arrivalTime = arrivalTime;
        this.arrivalNanos = System.nanoTime();
        this.schedulerMetrics = schedulerMetrics;
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

    public void admit() {

        Sinks.EmitResult result = admissionSignal.tryEmitEmpty();

        if (result.isSuccess()) {

            long waitNanos = System.nanoTime() - arrivalNanos;

            schedulerMetrics.recordQueueWait(
                    organizationId,
                    waitNanos
            );

            schedulerMetrics.recordDispatch(organizationId);
        }
    }
}
