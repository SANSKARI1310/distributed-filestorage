package com.codex.filestorage.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codex.filestorage.entity.Organization;
import java.util.UUID;
import java.util.Optional;

@Repository 
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    Optional<Organization> findByName(String name);
}
