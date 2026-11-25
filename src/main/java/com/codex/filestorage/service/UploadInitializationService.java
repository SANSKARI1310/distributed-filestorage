package com.codex.filestorage.service;

import com.codex.filestorage.dto.FileUploadRequestDto;
import com.codex.filestorage.entity.*;
import com.codex.filestorage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadInitializationService {

    private final MembershipRepository membershipRepository;
    private final StorageUsageRepository storageUsageRepository;
    private final FileMetadataRepository fileMetaDataRepository;
    private final RequestFingerPrintService fingerprintService;

    @Transactional
    public FileMetaData initialize(
            UUID organizationId,
            UUID userId,
            String idempotencyKey,
            FileUploadRequestDto request
    ) {
        // 1. Validate membership
        Membership membership = membershipRepository
                .findByOrganizationId_IdAndUserId_Id(organizationId, userId)
                .orElseThrow(() ->
                        new RuntimeException("User not a member of organization"));

        if (membership.getRole() != UserRole.WRITER
                && membership.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Writer role required");
        }

        Organization org = membership.getOrganizationId();
        User user = membership.getUserId();

        // 2. Generate request fingerprint
        String fingerprint = fingerprintService.generate(request);

        // 3. Check whether this key was already used
        var existing = fileMetaDataRepository
                .findByOrganizationId_IdAndIdempotencyKey(
                        organizationId, idempotencyKey);

        if (existing.isPresent()) {
            FileMetaData metadata = existing.get();

            if (!metadata.getRequestFingerprint().equals(fingerprint)) {
                throw new IllegalStateException(
                        "Idempotency key reused with different request data");
            }

            return metadata;
        }

        // 4. Reserve quota atomically
        long fileSize = request.fileSize();
        long quota = org.getStorageQuotaBytes();

        if (fileSize <= 0 || fileSize > quota) {
            throw new IllegalStateException(
                    "Invalid file size or storage quota exceeded");
        }

        int reserved = storageUsageRepository.reserveQuota(
                organizationId, fileSize, quota);

        if (reserved == 0) {
            throw new IllegalStateException(
                    "Storage quota exceeded or usage record missing");
        }

        // 5. Create metadata
        String storageKey = organizationId + "/" + UUID.randomUUID();

        FileMetaData metadata = FileMetaData.builder()
                .organizationId(org)
                .uploadedById(user)
                .storageKey(storageKey)
                .fileName(request.fileName())
                .fileSize(fileSize)
                .mimeType(request.mimeType())
                .checksumSha256(request.checksumSha256())
                .idempotencyKey(idempotencyKey)
                .requestFingerprint(fingerprint)
                .build();
        
        return fileMetaDataRepository.saveAndFlush(metadata);
    }
}
