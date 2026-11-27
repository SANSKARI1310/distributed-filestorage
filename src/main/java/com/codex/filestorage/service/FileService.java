package com.codex.filestorage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.codex.filestorage.dto.FileResponseSummaryDto;
import com.codex.filestorage.dto.FileUploadRequestDto;
import com.codex.filestorage.dto.FileUploadResponseDto;
import com.codex.filestorage.entity.UserRole;
import com.codex.filestorage.repository.StorageUsageRepository;
import com.codex.filestorage.entity.FileMetaData;
import com.codex.filestorage.entity.Membership;
import com.codex.filestorage.entity.UploadStatus;
import com.codex.filestorage.repository.FileMetadataRepository;
import com.codex.filestorage.repository.MembershipRepository;
import com.codex.filestorage.event.FileEventDelete;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.OptionalLong;
import java.util.UUID;

@Service 
@RequiredArgsConstructor 
public class FileService {
    
    private final MinioService minioService;
    private final StorageUsageRepository storageUsageRepository;
    private final FileMetadataRepository fileMetaDataRepository;
    private final MembershipRepository membershipRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestFingerPrintService requestFingerPrintService;
    private final UploadInitializationService uploadInitService;
    
    private Membership validateMembership(UUID organizationId, UUID userId , UserRole requiredRole) {
        Membership membership = membershipRepository.findByOrganizationId_IdAndUserId_Id(organizationId, userId)
            .orElseThrow(() -> new RuntimeException("User not a member of organization"));
        
        if(requiredRole == UserRole.ADMIN && membership.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Admin role required");
        }

        if(requiredRole == UserRole.WRITER && membership.getRole() != UserRole.WRITER) {
            throw new SecurityException("Writer role required");
        }

        return membership;
    }
    @Transactional 
    public FileUploadResponseDto initiateUpload(
        UUID organizationId,
        UUID userId,
        String idempotencyKey,
        FileUploadRequestDto request    
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        String fingerprint = requestFingerPrintService.generate(request);

        FileMetaData metadata;

        try {
            metadata = uploadInitService.initialize(
                    organizationId, userId, idempotencyKey, request);
        } catch (DataIntegrityViolationException exception) {

            metadata = fileMetaDataRepository
                    .findByOrganizationId_IdAndIdempotencyKey(
                            organizationId, idempotencyKey)
                    .orElseThrow(() -> exception);

            if (!metadata.getRequestFingerprint().equals(fingerprint)) {
                throw new IllegalStateException(
                        "Idempotency key reused with different request data");
            }
        }

        String uploadUrl = minioService.generatePresignedUrl(
                metadata.getStorageKey());

        return new FileUploadResponseDto(
                metadata.getId(),
                uploadUrl,
                metadata.getStorageKey());
    }
    @Transactional(readOnly = true)
    public String getDownloadUrl(UUID organizationId, UUID userId, UUID fileId) {

        validateMembership(organizationId, userId, UserRole.READER);
        FileMetaData fileMetaData = fileMetaDataRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));
        String storageKey = fileMetaData.getStorageKey();
        return minioService.generateDownloadUrl(storageKey);
    }

    @Transactional(readOnly = true)
    public Page<FileResponseSummaryDto> getFiles(UUID organizationId, UUID userId ,Pageable pageable) {

        validateMembership(organizationId, userId, UserRole.READER);
        return fileMetaDataRepository.findAllByOrganizationId_IdAndIsDeletedFalse(organizationId, pageable)
            .map(file -> new FileResponseSummaryDto(
                file.getId(),
                file.getFileName(),
                file.getFileSize(),
                file.getMimeType(),
                file.getChecksumSha256(),
                file.getCreatedAt()
            ));
    }

    @Transactional
    public void deleteFile(UUID organizationId, UUID fileId , UUID userId) {

        validateMembership(organizationId, userId, UserRole.WRITER);
        FileMetaData metadata = fileMetaDataRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
        
        metadata.setIsDeleted(true);
        fileMetaDataRepository.save(metadata);

        storageUsageRepository.decrementUsage(organizationId, metadata.getFileSize());
        
        eventPublisher.publishEvent(new FileEventDelete(metadata.getStorageKey()));
        
    }
    
    public void completeUpload(
        UUID organizationId,
        UUID userId,
        UUID fileId
    ) {
        // 1. Validate membership
        Membership membership = membershipRepository
            .findByOrganizationId_IdAndUserId_Id(organizationId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN, "User is not a member"
            ));

        if (membership.getRole() != UserRole.WRITER
                && membership.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Writer role required"
            );
        }

        // 2. Find metadata within this organization
        FileMetaData metadata = fileMetaDataRepository
            .findByIdAndOrganizationId_IdAndIsDeletedFalse(
                fileId, organizationId
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "File not found"
            ));

        // 3. Idempotent retry: already completed
        if (metadata.getUploadStatus() == UploadStatus.COMPLETED) {
            return;
        }

        if (metadata.getUploadStatus() != UploadStatus.PENDING) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Upload cannot be completed"
            );
        }

        // 4. Check object existence and actual size
        OptionalLong actualSize = minioService.getObjectSizeIfExists(
            metadata.getStorageKey()
        );

        if (actualSize.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Uploaded object does not exist"
            );
        }

        if (actualSize.getAsLong() != metadata.getFileSize()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Uploaded file size does not match"
            );
        }

        // 5. Atomic PENDING -> COMPLETED transition
        int updated = fileMetaDataRepository.markUploadCompleted(
            fileId,
            organizationId,
            UploadStatus.PENDING,
            UploadStatus.COMPLETED
        );

        if (updated == 1) {
            return;
        }

        // 6. Another concurrent request may have completed it.
        FileMetaData latest = fileMetaDataRepository
            .findByIdAndOrganizationId_IdAndIsDeletedFalse(
                fileId, organizationId
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "File not found"
            ));

        if (latest.getUploadStatus() == UploadStatus.COMPLETED) {
            return;
        }

        throw new ResponseStatusException(
            HttpStatus.CONFLICT, "Upload could not be completed"
        );
    }
}
