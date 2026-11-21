package com.codex.filestorage.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;

@Component
public class MinioHealth implements HealthIndicator{
    

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioHealth(MinioClient minioClient , MinioProperties minioProperties)
    {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @Override
    public Health health() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build()
            );

            if (!bucketExists) {
                return Health.down()
                    .withDetail("bucket", minioProperties.getBucketName())
                    .withDetail("reason", "Configured bucket does not exist")
                    .build();
            }

            return Health.up()
                .withDetail("bucket", minioProperties.getBucketName())
                .build();

        } catch (Exception exception) {
            return Health.down()
                .withDetail("reason", "MinIO is unavailable")
                .withDetail("error", exception.getClass().getSimpleName())
                .build();
        }
    }
}
