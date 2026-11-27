package com.codex.filestorage.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.codex.filestorage.entity.FileMetaData;
import com.codex.filestorage.entity.UploadStatus;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository 
public interface FileMetadataRepository extends JpaRepository<FileMetaData, UUID> {
    
    Optional<FileMetaData> findByIdAndOrganizationId_IdAndIsDeletedFalse(UUID id, UUID organizationId);
    Optional<FileMetaData> findByStorageKeyAndOrganizationId_Id(UUID organizationId, String storageKey);
    Page<FileMetaData> findAllByOrganizationId_IdAndIsDeletedFalse(UUID organizationId, Pageable pageable);
    Optional<FileMetaData> findByOrganizationId_IdAndIdempotencyKey(UUID organizationId,String idempotencyKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        UPDATE FileMetaData f
        SET f.uploadStatus = :completed
        WHERE f.id = :fileId
        AND f.organizationId.id = :organizationId
        AND f.uploadStatus = :pending
        AND f.isDeleted = false
    """)
    int markUploadCompleted(
        @Param("fileId") UUID fileId,
        @Param("organizationId") UUID organizationId,
        @Param("pending") UploadStatus pending,
        @Param("completed") UploadStatus completed
    );
}
