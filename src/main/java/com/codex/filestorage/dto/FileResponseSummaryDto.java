package com.codex.filestorage.dto;

public record FileResponseSummaryDto(
    UUID fileId,
    String fileName,
    String fileType,
    long fileSize,
    String mimeType,
    
) {
    

}
