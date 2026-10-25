package com.codex.filestorage.dto;

public record FileUploadRequestDto
(
    String fileName,
    Long fileSize,
    String mimeType,
    String checkSumSha256
) {
    
}
