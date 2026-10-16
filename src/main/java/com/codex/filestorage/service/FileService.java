package com.codex.filestorage.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import com.codex.filestorage.dto.FileUploadRequestDto;
import com.codex.filestorage.dto.FileUploadResponseDto;
import com.codex.filestorage.entity.Organization;
import com.codex.filestorage.entity.User;
import com.codex.filestorage.repository.UserRepository;
import com.codex.filestorage.repository.OrganizationRepository;
import com.codex.filestorage.repository.StorageUsageRepository;
import com.codex.filestorage.entity.FileMetaData;
import com.codex.filestorage.repository.FileMetadataRepository;
import java.util.UUID;

@Service 
@RequiredArgsConstructor 
public class FileService {
    
    private final MinioService minioService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final StorageUsageRepository storageUsageRepository;
    private final FileMetadataRepository fileMetaDataRepository;
    
    public FileUploadResponseDto initiateUpload(UUID organizationId, UUID userId, FileUploadRequestDto request) 
        {
              Organization org = organizationRepository.findById(organizationId)
              .orElseThrow(() -> new RuntimeException("Organization not found"));
              User user = userRepository.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found"));

            storageUsageRepository.findById(organizationId)
            .ifPresent(usage->
                {
                    if (usage.getUsedBytes() + request.fileSize() > org.getStorageQuotaBytes()) {
                        throw new IllegalStateException("Storage quota exceeded for organization");
                    }

                }
            );
            storageUsageRepository.incrementUsage(organizationId, request.fileSize());

            UUID fileId = UUID.randomUUID();
            String storageKey = organizationId.toString() + "/" + fileId.toString();
            
            FileMetaData fileMetaData = FileMetaData.builder()
                .id(fileId)
                .organizationId(org)
                .uploadedById(user)
                .storageKey(storageKey)
                .fileName(request.fileName())
                .fileSize(request.fileSize())
                .mimeType(request.mimeType())
                .checksumSha256(request.checkSumSha256())
                .build();
            fileMetaDataRepository.save(fileMetaData);

            String presignedUrl = minioService.generatePresignedUrl(storageKey);
            return new FileUploadResponseDto(fileId , presignedUrl , storageKey);
        }   

        public String getDownloadUrl(UUID organizationId, UUID fileId) {

            FileMetaData fileMetaData = fileMetaDataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
            String storageKey = fileMetaData.getStorageKey();
            return minioService.generateDownloadUrl(storageKey);
        }


}
