package com.codex.filestorage.dto;

public record FileUploadRequestDto
(
    String fileName,
    long fileSize,
    String mimeType,
    String checkSumSha256
) {
    
}
