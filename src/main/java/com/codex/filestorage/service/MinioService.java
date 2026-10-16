package com.codex.filestorage.service;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.codex.filestorage.config.MinioProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.Http.Method;


@Service
@Slf4j
@RequiredArgsConstructor
public class MinioService {
    
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String generatePresignedUrl(String storageKey) 
        {
            try
            {
                return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(minioProperties.getBucketName())
                    .object(storageKey)
                    .expiry(15,TimeUnit.MINUTES)
                    .build()
                );
            }
            catch (Exception e)
            {
                log.error("Error generating presigned url for key {}", storageKey, e);
                throw new RuntimeException("Could not generate presigned URl",e);
            }
        }

        public String generateDownloadUrl(String storageKey) {
            {
                try
                {
                    return minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(minioProperties.getBucketName())
                        .object(storageKey)
                        .expiry(15, TimeUnit.MINUTES)
                        .build()
                    );
                }
                catch (Exception e)
                {
                    log.error("Error generating download url for key {}", storageKey, e);
                    throw new RuntimeException("Could not generate presigned URl", e);
                }
            }
        }
    
}