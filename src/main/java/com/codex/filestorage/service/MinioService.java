package com.codex.filestorage.service;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.codex.filestorage.config.MinioProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.Http.Method;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import java.util.OptionalLong;

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

    public void deleteFile(String storageKey) {
            try
            {
                minioClient.removeObject(
                    RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(storageKey)
                    .build()
                );
            }
            catch (Exception e)
            {
                log.error("Failed to delete file {}", storageKey, e);
                throw new RuntimeException("Failed to delete file", e);
            }
        }

    public boolean objectExists(String storageKey) {
            try {
                minioClient.statObject(
                    StatObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(storageKey)
                        .build()
                );

                return true;
            } catch (io.minio.errors.ErrorResponseException e) {

                String code = e.errorResponse().code();
                if ("NoSuchKey".equals(e.errorResponse().code())) {
                    return false;
                }

                throw new RuntimeException("Could not verify uploaded object", e);
            } catch (Exception e) {
                throw new RuntimeException("Could not verify uploaded object", e);
           }
         }

    public OptionalLong getObjectSizeIfExists(String storageKey) {
            try {
                StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(storageKey)
                    .build()
                );

            return OptionalLong.of(stat.size());

            } catch (io.minio.errors.ErrorResponseException e) {
                String code = e.errorResponse().code();

                if ("NoSuchKey".equals(code) || "NoSuchObject".equals(code)) {
                    return OptionalLong.empty();
                }

                throw new RuntimeException("Could not retrieve object size", e);

         } catch (Exception e) {
                throw new RuntimeException("Could not retrieve object size", e);
            }
        }

    
}