package com.codex.filestorage.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.Builder;
import java.time.OffsetDateTime;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity 
@Table(name = "file_metadata")
@Builder 
public class FileMetaData {
    
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "organization_id" , nullable = false)
    private Organization organizationId;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "uploaded_by_id" , nullable = false)
    private User uploadedById;

    @Column(name = "storage_key" , nullable = false , unique = true , length = 1024)
    private String storageKey;

    @Column(name = "file_name" , nullable = false , length = 512)
    private String fileName;

    @Column(name = "file_size" , nullable = false)
    private Long fileSize;

    @Column(name = "mime_type" , nullable = false)
    private String mimeType;

    @Column(name = "checksum_sha256" , nullable = false , length =64)
    private String checksumSha256;

    @Column(name = "version" , nullable = false)
    @Builder.Default
    private int version = 1;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "created_at" , updatable = false)
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now().toString());
        updatedAt = OffsetDateTime.now().toString();
    }

    @PreUpdate 
    public void onUpdate() {
        updatedAt = OffsetDateTime.now().toString();
    }
}   
