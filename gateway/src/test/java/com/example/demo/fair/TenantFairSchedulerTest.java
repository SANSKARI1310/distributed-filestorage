package com.example.demo.fair;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TenantFairSchedulerTest {

    @Test
    void shouldScheduleTenantsAccordingToWeights() {

        TenantQueueManager queueManager = new TenantQueueManager();

        TenantWeightResolver weightResolver = new TenantWeightResolver();

        TenantFairScheduler scheduler =
                new TenantFairScheduler(queueManager, weightResolver);

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
}