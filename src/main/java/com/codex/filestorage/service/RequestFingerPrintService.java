package com.codex.filestorage.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

import com.codex.filestorage.dto.FileUploadRequestDto;

@Service
public class RequestFingerPrintService {
    

public String generate(FileUploadRequestDto request) {

    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        updateField(digest, request.fileName());
        updateField(digest, request.fileSize().toString());
        updateField(digest, request.mimeType());
        updateField(digest, request.checksumSha256());

        return HexFormat.of().formatHex(digest.digest());

    } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException("SHA-256 unavailable", e);
    }
}

private void updateField(MessageDigest digest, String value) {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

    digest.update((byte) (bytes.length >>> 24));
    digest.update((byte) (bytes.length >>> 16));
    digest.update((byte) (bytes.length >>> 8));
    digest.update((byte) bytes.length);

    digest.update(bytes);
}


}
