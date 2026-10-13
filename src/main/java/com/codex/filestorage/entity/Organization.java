package com.codex.filestorage.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.UUID;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity 
@Table (name = "organization")
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor 
public class Organization {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name" , nullable = false , unique = true)
    private String name;

    @Column(name = "active" , nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column (name = "storage_quota_bytes" , nullable = false)
    @Builder.Default
    private Long storageQuotaBytes = 10737418240L;
    
    @Column(name = "created_at" , updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist 
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
