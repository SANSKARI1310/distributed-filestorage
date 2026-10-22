package com.codex.filestorage.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import java.time.OffsetDateTime;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;



@Entity
@Table(name = "membership", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "organization_id"})
})
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Membership {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne (fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "organization_id" , nullable = false)
    private Organization organizationId;

    @ManyToOne (fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "user_id" , nullable = false)
    private User userId;

    @Enumerated (EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.READER;

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
