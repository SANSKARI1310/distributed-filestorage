package com.example.demo.fair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;

@Component 
public class TenantQueueManager {
    
    private final Map<String , ConcurrentLinkedQueue<QueuedTenantRequest>> tenantQueues = new ConcurrentHashMap<>();

    public void enqueue(QueuedTenantRequest req)
    {
        tenantQueues.computeIfAbsent(
            req.getOrganizationId(),
            ignored -> new ConcurrentLinkedQueue<>()
        ).offer(req);
        
    }

    public QueuedTenantRequest poll(String organizationId)
    {
        ConcurrentLinkedQueue<QueuedTenantRequest> queue = new ConcurrentLinkedQueue<>();
        queue = tenantQueues.get(organizationId);
        if(queue==null)
            return null;
        return queue.poll();
    
    }
    public int getQueueSize(String organizationId)
    {
         ConcurrentLinkedQueue<QueuedTenantRequest> queue = new ConcurrentLinkedQueue<>();
        queue = tenantQueues.get(organizationId);
        if(queue==null)
            return 0;
        return queue.size();
    }

    public boolean hasRequests(String organizationId) {

        ConcurrentLinkedQueue<QueuedTenantRequest> queue =
                tenantQueues.get(organizationId);

        return queue != null && !queue.isEmpty();
    }
    
    public Set<String> getActiveTenants()
    {
        return tenantQueues.keySet();

    }
     
}
