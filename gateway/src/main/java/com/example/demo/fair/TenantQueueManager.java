
package com.example.demo.fair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class TenantQueueManager {

    private final Map<String, ConcurrentLinkedQueue<QueuedTenantRequest>> tenantQueues =
            new ConcurrentHashMap<>();

    private final Map<String, AtomicInteger> queueDepths =
            new ConcurrentHashMap<>();

    public void enqueue(QueuedTenantRequest req) {

        String tenantId = req.getOrganizationId();

        tenantQueues.computeIfAbsent(
                tenantId,
                ignored -> new ConcurrentLinkedQueue<>()
        ).offer(req);

        queueDepths.computeIfAbsent(
                tenantId,
                ignored -> new AtomicInteger(0)
        ).incrementAndGet();
    }

    public QueuedTenantRequest poll(String organizationId) {

        ConcurrentLinkedQueue<QueuedTenantRequest> queue =
                tenantQueues.get(organizationId);

        if (queue == null) {
            return null;
        }

        QueuedTenantRequest request = queue.poll();

        if (request != null) {
            queueDepths.get(organizationId).decrementAndGet();
        }

        return request;
    }

    public int getQueueSize(String organizationId) {

        AtomicInteger depth = queueDepths.get(organizationId);

        return depth == null ? 0 : depth.get();
    }

    public boolean hasRequests(String organizationId) {

        return getQueueSize(organizationId) > 0;
    }

    public Set<String> getActiveTenants() {
        return tenantQueues.keySet();
    }
}
