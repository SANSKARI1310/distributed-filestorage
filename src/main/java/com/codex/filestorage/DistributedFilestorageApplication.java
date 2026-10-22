package com.codex.filestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync 
public class DistributedFilestorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributedFilestorageApplication.class, args);
	}

}
