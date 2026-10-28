package com.codex.filestorage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.codex.filestorage.dto.FileResponseSummaryDto;
import com.codex.filestorage.dto.FileUploadRequestDto;
import com.codex.filestorage.dto.FileUploadResponseDto;
import com.codex.filestorage.entity.Organization;
import com.codex.filestorage.entity.User;
import com.codex.filestorage.entity.UserRole;
import com.codex.filestorage.repository.UserRepository;
import com.codex.filestorage.repository.OrganizationRepository;
import com.codex.filestorage.repository.StorageUsageRepository;
import com.codex.filestorage.entity.FileMetaData;
import com.codex.filestorage.entity.Membership;
import com.codex.filestorage.repository.FileMetadataRepository;
import com.codex.filestorage.repository.MembershipRepository;
import com.codex.filestorage.event.FileEventDelete;
import org.springframework.context.ApplicationEventPublisher;
import java.util.UUID;

@Service 
@RequiredArgsConstructor 
public class FileService {
    
    private final MinioService minioService;
    private final StorageUsageRepository storageUsageRepository;
    private final FileMetadataRepository fileMetaDataRepository;
    private final MembershipRepository membershipRepository;
    private final ApplicationEventPublisher eventPublisher;
    
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
    public FileUploadResponseDto initiateUpload(UUID organizationId, UUID userId, FileUploadRequestDto request) 
        {
          
            Membership membership = validateMembership(organizationId, userId, UserRole.WRITER);
            Organization org = membership.getOrganizationId();
            User user = membership.getUserId();
            storageUsageRepository.findById(organizationId)
            .ifPresent(usage->
                {
                    if (usage.getUsedBytes() + request.fileSize() > org.getStorageQuotaBytes()) {
                        throw new IllegalStateException("Storage quota exceeded for organization");
                    }

                }
            );
            storageUsageRepository.incrementUsage(organizationId, request.fileSize());

            String storageKey = organizationId + "/" + UUID.randomUUID();
            
            FileMetaData fileMetaData = FileMetaData.builder()
                .organizationId(org)
                .uploadedById(user)
                .storageKey(storageKey)
                .fileName(request.fileName())
                .fileSize(request.fileSize())
                .mimeType(request.mimeType())
                .checksumSha256(request.checksumSha256())
                .build();
            FileMetaData saved=  fileMetaDataRepository.save(fileMetaData);
            UUID fileId = saved.getId();
            String presignedUrl = minioService.generatePresignedUrl(storageKey);
            return new FileUploadResponseDto(fileId , presignedUrl , storageKey);
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
        return fileMetaDataRepository.findAllByOrganizationIdAndIsDeletedFalse(organizationId, pageable)
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
    
}
