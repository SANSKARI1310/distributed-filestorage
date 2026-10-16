package com.codex.filestorage.dto;

import java.util.UUID;

public record FileUploadResponseDto( 
    UUID fileId,
    String uploadUrl,
    String storageKey
)
{}
