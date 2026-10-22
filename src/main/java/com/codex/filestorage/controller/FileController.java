package com.codex.filestorage.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.codex.filestorage.dto.FileUploadRequestDto;
import com.codex.filestorage.dto.FileUploadResponseDto;
import com.codex.filestorage.dto.FileResponseSummaryDto;
import com.codex.filestorage.service.FileService;

@RestController 
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor 
public class FileController {
    
    private final FileService fileService;
    @Value("${server.port:8081}")
    private String port;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponseDto> initiateUpload(
        @RequestHeader("X-Organization-Id") UUID organizationId,
        @RequestHeader ("X-User-Id") UUID userId,
        @RequestBody FileUploadRequestDto fileUploadRequestDto
    )
    {
        FileUploadResponseDto response = fileService.initiateUpload(organizationId, userId, fileUploadRequestDto);
        return ResponseEntity.ok()
            .header("X-Handled-By:" ,"node -" + port)
            .body(response);
    }

    @GetMapping("{fileId}/download")
    public ResponseEntity<Map<String, String>> getDownloadUrl(
        @RequestHeader ("X-Organization-Id") UUID organizationId,
        @RequestHeader ("X-User-Id") UUID userId,
        @PathVariable UUID fileId
    )
    {
        String downloadUrl = fileService.getDownloadUrl(organizationId, userId ,fileId);
        return ResponseEntity.ok().header("X-Handled-By:" ,"node -" + port)
        .body(Map.of("downloadUrl", downloadUrl));
    }

    @GetMapping ("/listFiles")
    public ResponseEntity<Page<FileResponseSummaryDto>> listFiles(
        @RequestHeader ("X-Organization-Id") UUID organizationId,
        @RequestHeader ("X-User-Id") UUID userId,
        @PageableDefault(size = 20) Pageable pageable
    )
    {
        return ResponseEntity.ok().header("X-Handled-By:" ,"node -" + port)
            .body(fileService.getFiles(organizationId, userId, pageable));
    }
    @DeleteMapping("{fileId}")
    public ResponseEntity<Void> deleteFile(
        @RequestHeader ("X-Organization-Id") UUID organizationId,
        @RequestHeader ("X-User-Id") UUID userId,
        @PathVariable UUID fileId
    )
    {
        fileService.deleteFile(organizationId, userId , fileId);
        return ResponseEntity.noContent()
                .header("X-Handled-By", "node-" + port)
                .build();
    }


}
