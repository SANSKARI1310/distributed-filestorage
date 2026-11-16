package com.example.demo.fair;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TenantFairSchedulerTest {

    @Test
    void shouldScheduleTenantsAccordingToWeights() {

        TenantQueueManager queueManager = new TenantQueueManager();

        TenantWeightResolver weightResolver = new TenantWeightResolver();

        TenantFairScheduler scheduler = new TenantFairScheduler(queueManager, weightResolver);
       
        // Tenant A -> weight 5
        for (int i = 0; i < 10; i++) {
            queueManager.enqueue(
                    new QueuedTenantRequest("TENANT_A", Instant.now())
            );
        }

        // Tenant B -> weight 3
        for (int i = 0; i < 10; i++) {
            queueManager.enqueue(
                    new QueuedTenantRequest("TENANT_B", Instant.now())
            );
        }

        // Tenant C -> weight 1
        for (int i = 0; i < 10; i++) {
            queueManager.enqueue(
                    new QueuedTenantRequest("TENANT_C", Instant.now())
            );
        }

        // Run one DWRR scheduling round
        scheduler.scheduleRound();

        assertEquals(5, queueManager.getQueueSize("TENANT_A"));
        assertEquals(7, queueManager.getQueueSize("TENANT_B"));
        assertEquals(9, queueManager.getQueueSize("TENANT_C"));
    }
    @Test
    void shouldContinueSchedulingAcrossMultipleRounds() {

        TenantQueueManager queueManager = new TenantQueueManager();
        TenantWeightResolver weightResolver = new TenantWeightResolver();

        TenantFairScheduler scheduler =
                new TenantFairScheduler(queueManager, weightResolver);

        // Give each tenant enough requests for multiple rounds
        for (int i = 0; i < 20; i++) {

            queueManager.enqueue(
                    new QueuedTenantRequest("TENANT_A", Instant.now())
            );

            queueManager.enqueue(
                    new QueuedTenantRequest("TENANT_B", Instant.now())
            );

            queueManager.enqueue(
                    new QueuedTenantRequest("TENANT_C", Instant.now())
            );
        }

        // Round 1
        scheduler.scheduleRound();

        assertEquals(15, queueManager.getQueueSize("TENANT_A"));
        assertEquals(17, queueManager.getQueueSize("TENANT_B"));
        assertEquals(19, queueManager.getQueueSize("TENANT_C"));

        // Round 2
        scheduler.scheduleRound();

        assertEquals(10, queueManager.getQueueSize("TENANT_A"));
        assertEquals(14, queueManager.getQueueSize("TENANT_B"));
        assertEquals(18, queueManager.getQueueSize("TENANT_C"));
        }
    @Test
    void shouldHandleTenantQueueBecomingEmpty() {

        TenantQueueManager queueManager = new TenantQueueManager();
        TenantWeightResolver weightResolver = new TenantWeightResolver();

        TenantFairScheduler scheduler =
                new TenantFairScheduler(queueManager, weightResolver);

        // A only has 2 requests, despite weight 5
        for (int i = 0; i < 2; i++) {
            queueManager.enqueue(
                    new QueuedTenantRequest("TENANT_A", Instant.now())
            );
        }

        // B and C have enough requests
        for (int i = 0; i < 10; i++) {

            queueManager.enqueue(
                    new QueuedTenantRequest("TENANT_B", Instant.now())
            );

            queueManager.enqueue(
                    new QueuedTenantRequest("TENANT_C", Instant.now())
            );
        }

        scheduler.scheduleRound();

        assertEquals(0, queueManager.getQueueSize("TENANT_A"));
        assertEquals(7, queueManager.getQueueSize("TENANT_B"));
        assertEquals(9, queueManager.getQueueSize("TENANT_C"));
    }

    @Test
    void shouldAdmitScheduledRequest() {

        TenantQueueManager queueManager = new TenantQueueManager();
        TenantWeightResolver weightResolver = new TenantWeightResolver();

        TenantFairScheduler scheduler =
                new TenantFairScheduler(queueManager, weightResolver);

        QueuedTenantRequest request =
                new QueuedTenantRequest("TENANT_C", Instant.now());

        queueManager.enqueue(request);

        StepVerifier.create(request.waitForAdmission())
                .then(() -> scheduler.scheduleRound())
                .verifyComplete();
    }
}