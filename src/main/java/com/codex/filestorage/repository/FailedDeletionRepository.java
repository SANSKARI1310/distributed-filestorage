package com.codex.filestorage.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codex.filestorage.entity.FailedDeletion;
import java.util.UUID;
import java.util.List;

@Repository 
public interface FailedDeletionRepository extends JpaRepository<FailedDeletion, UUID> {
    
    List<FailedDeletion> findTop50ByRetryCountLessThanOrderByCreatedAtAsc(int retryCount);


}
