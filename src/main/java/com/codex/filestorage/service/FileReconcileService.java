package com.codex.filestorage.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.codex.filestorage.entity.FailedDeletion;
import com.codex.filestorage.repository.FailedDeletionRepository;
import java.util.List;

@Slf4j 
@RequiredArgsConstructor 
@Service 
public class FileReconcileService {
    
    private static final int MAX_RETRY_COUNT = 5;
    private final FailedDeletionRepository failedDeletionRepository;
    private final MinioService minioService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional 
    public void reconcileFailedDeletions() {

        List<FailedDeletion> failedDeletions = failedDeletionRepository.findTop50ByRetryCountLessThanOrderByCreatedAtAsc(MAX_RETRY_COUNT);

        if(failedDeletions.isEmpty()) {
            return;
        }
        
        log.info("starting reconciliation of {} failed deletions", failedDeletions.size());

        for(FailedDeletion i : failedDeletions) {
            try {
                minioService.deleteFile(i.getStorageKey());
                failedDeletionRepository.delete(i);
                log.info("successfully deleted {}", i.getStorageKey());
            }
            catch(Exception e)
            {
                log.warn("failed to delete {}", i.getStorageKey(), e);
                i.setRetryCount(i.getRetryCount() + 1);
                i.setLastError(e.getMessage());
                failedDeletionRepository.save(i);
            }
        }
    }

}
