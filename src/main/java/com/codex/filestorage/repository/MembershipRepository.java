package com.codex.filestorage.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codex.filestorage.entity.Membership;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository 
public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    List<Membership> findAllByUserId(UUID userId);
    Optional<Membership> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
