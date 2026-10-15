package com.codex.filestorage.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.codex.filestorage.entity.StorageUsage;
import java.util.UUID;

@Repository 
public interface StorageUsageRepository extends JpaRepository<StorageUsage, UUID> {
   
    @Modifying //to avoid race condition
    @Query(
       "UPDATE StorageUsage s SET s.usedBytes = s.usedBytes + :bytes WHERE s.organizationId = :orgId"
    )
    int incrementUsage(@Param("orgId") UUID orgId, @Param("bytes") long bytes);
}
