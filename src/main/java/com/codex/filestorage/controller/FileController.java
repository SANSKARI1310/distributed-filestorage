package com.codex.filestorage.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
import java.util.Map;
import com.codex.filestorage.dto.FileUploadRequestDto;
import com.codex.filestorage.dto.FileUploadResponseDto;
import com.codex.filestorage.service.FileService;

@RestController 
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor 
public class FileController {
    
    private final FileService fileService;


    @RequestMapping("/upload")
    public ResponseEntity<FileUploadResponseDto> initiateUpload(
        @RequestHeader("X-Organization-Id") UUID organizationId,
        @RequestHeader ("X-User-Id") UUID userId,
        @RequestBody FileUploadRequestDto fileUploadRequestDto
    )
    {
        FileUploadResponseDto response = fileService.initiateUpload(organizationId, userId, fileUploadRequestDto);
        return ResponseEntity.ok(response);
    }

    @RequestMapping("{fileId}/download")
    public ResponseEntity<Map<String, String>> getDownloadUrl(
        @RequestHeader ("X-Organization-Id") UUID organizationId,
        @PathVariable UUID fileId
    )
    {
        String downloadUrl = fileService.getDownloadUrl(organizationId, fileId);
        return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
    }

}
