package com.codex.filestorage.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.OffsetDateTime;


@Entity 
@Table (name = "user")
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name" , nullable = false , unique = true)
    private String name;

    @Column(name = "email" , nullable = false , unique = true)
    private String email;

    @Column(name = "password_hash" , nullable = false)
    private String passwordHash;

    @Column(name = "active" , nullable = false)
    @Builder.Default
    private Boolean active = true;

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
