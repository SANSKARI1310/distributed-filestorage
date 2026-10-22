package com.codex.filestorage.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileResponseSummaryDto(
    UUID fileId,
    String fileName,
    long fileSize,
    String mimeType,
    String checksumSha256,
    OffsetDateTime createdAt
) {
    
}
