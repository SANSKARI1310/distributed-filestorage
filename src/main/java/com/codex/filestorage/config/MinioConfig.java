package com.codex.filestorage.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import io.minio.MinioClient;


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
        return client;
    }
    //removed create bucket here so our backend does not create issue if minio is not available during startup
}
