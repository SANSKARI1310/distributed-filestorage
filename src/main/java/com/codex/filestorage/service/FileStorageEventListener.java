package com.codex.filestorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.codex.filestorage.event.FileEventDelete;

@Slf4j 
@RequiredArgsConstructor 
@Component
public class FileStorageEventListener {
    
    private final MinioService minioService;

    @Async 
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileDelete(FileEventDelete event) {

        log.info("Triggering delete for {}", event.storageKey());
        try
        {
            minioService.deleteFile(event.storageKey());
            log.info("successfully deleted {}", event.storageKey());
        }
        catch (Exception e)
        {
            log.error("Failed to delete {}", event.storageKey(), e);
        }
    }
}
