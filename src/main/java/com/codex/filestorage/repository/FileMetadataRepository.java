package com.codex.filestorage.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codex.filestorage.entity.FileMetaData;
import java.util.UUID;
import java.util.Optional;

@Repository 
public interface FileMetadataRepository extends JpaRepository<FileMetaData, UUID> {
    
    Optional<FileMetaData> findByIdAndOrganizationIdAndIsDeletedFalse(UUID id, UUID organizationId);
    Optional<FileMetaData> findByStorageKeyAndOrganizationId(UUID organizationId, String storageKey);
}