package com.codex.filestorage.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import io.minio.MinioClient;
import io.minio.MakeBucketArgs;
import io.minio.BucketExistsArgs;


@Slf4j 
@Configuration  
@RequiredArgsConstructor 
public class MinioConfig {
    
    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(minioProperties.getUrl())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        try
        {
            boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
            if(!found)
            {
                log.info("Bucket {} not exists" , minioProperties.getBucketName());
                client.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
            }
        }
        catch(Exception e)
        {
            log.error("Error while creating bucket {}" , e.getMessage());
            throw new RuntimeException("Failed to initialize minIO client");
        }
        return client;
    }
}
