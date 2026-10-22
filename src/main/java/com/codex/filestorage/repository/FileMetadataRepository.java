package com.codex.filestorage.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codex.filestorage.entity.FileMetaData;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository 
public interface FileMetadataRepository extends JpaRepository<FileMetaData, UUID> {
    
    Optional<FileMetaData> findByIdAndOrganizationIdAndIsDeletedFalse(UUID id, UUID organizationId);
    Optional<FileMetaData> findByStorageKeyAndOrganizationId(UUID organizationId, String storageKey);
    Page<FileMetaData> findAllByOrganizationIdAndIsDeletedFalse(UUID organizationId, Pageable pageable);
}