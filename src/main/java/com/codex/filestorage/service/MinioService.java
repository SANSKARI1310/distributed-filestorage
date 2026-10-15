package com.codex.filestorage.service;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.minio.MinioClient;


@Service
@Slf4j
@RequiredArgsConstructor
public class MinioService {
    
    private MinioClient minioClient;
    
}
