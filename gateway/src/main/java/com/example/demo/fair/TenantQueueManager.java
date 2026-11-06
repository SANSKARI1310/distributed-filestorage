package com.example.demo.filter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Component;
import java.util.Map;


@Component 
public class TenantQueueManager {
    
    private final Map<String , ConcurrentLinkedQueue<TenantRequest>> tenantQueues = new ConcurrentHashMap<>();

    public void enqueue(TenantRequest req)
    {
        tenantQueues.computeIfAbsent(
            req.organizationId(),
            ignored -> new ConcurrentLinkedQueue<>()
        ).offer(req);

    }

    public TenantRequest poll(String organizationId)
    {
        ConcurrentLinkedQueue<TenantRequest> queue = new ConcurrentLinkedQueue<>();
        queue = tenantQueues.get(organizationId);
        if(queue==null)
            return null;
        return queue.poll();
    
    }
    public int getQueueSize()
    {
         ConcurrentLinkedQueue<TenantRequest> queue = new ConcurrentLinkedQueue<>();
        queue = tenantQueues.get(organizationId);
        if(queue==null)
            return 0;
        return queueu.size();
    }
}
