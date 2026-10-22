package com.codex.filestorage.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity 
@Table (name = "storage_data")
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StorageUsage {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private UUID organizationId;

    @MapsId 
    @OneToOne (fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "organization_id" , nullable = false)
    private Organization organization;

    @Column(name = "used_bytes" , nullable = false)
    @Builder.Default
    private Long usedBytes =0L;

    

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }


}