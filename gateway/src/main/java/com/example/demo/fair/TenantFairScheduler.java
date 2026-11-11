package com.example.demo.fair;


import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;   
import java.util.Comparator;   

@Component
public class TenantFairScheduler {
    
    private static final int BASE_QUANTUM = 1;
    private static final int REQUEST_COST = 1;
    private int deficit ;
    private final TenantQueueManager queueManager;
    private final TenantSchedulingState tenantSchedule;
    private final TenantWeightResolver tenantWeight;

    private final Map<String, TenantSchedulingState> tenantStates =
            new ConcurrentHashMap<>();

    public TenantFairScheduler(TenantQueueManager queueManager , TenantSchedulingState tenantSchedule , TenantWeightResolver tenantWeight) {
        this.queueManager = queueManager;
        this.tenantSchedule=tenantSchedule;
        this.tenantWeight = tenantWeight;
        this.deficit=0;
    }

    @PostConstruct
    public void start() {

        Thread schedulerThread = new Thread(() -> {

            while (!Thread.currentThread().isInterrupted()) {

                scheduleRound();

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        schedulerThread.setDaemon(true);
        schedulerThread.setName("tenant-dwrr-scheduler");

        schedulerThread.start();
    }

    public void scheduleRound() {

        List<String> activeTenants = queueManager.getActiveTenants()
                .stream()
                .filter(queueManager::hasRequests)
                .sorted(Comparator.naturalOrder())
                .toList();

        for (String tenantId : activeTenants) {

            TenantSchedulingState state =
                    tenantStates.computeIfAbsent(
                            tenantId,
                            this::createTenantState
                    );

            state.addQuantum(BASE_QUANTUM);

            while (queueManager.hasRequests(tenantId)
                    && state.canServe(REQUEST_COST)) {

                QueuedTenantRequest request =
                        queueManager.poll(tenantId);

                if (request == null) {
                    break;
                }

                state.consume(REQUEST_COST);

                request.admit();
            }
        }
    }

    private TenantSchedulingState createTenantState(String tenantId) {

        int weight = tenantWeight.getWeight(tenantId);
        tenantSchedule.addQuantum(BASE_QUANTUM);
        return new TenantSchedulingState(weight,deficit);
    }

}