package com.wefit.relationshipService.repository;

import com.wefit.relationshipService.model.Relationship;
import com.wefit.relationshipService.model.RelationshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, UUID> {

    Optional<Relationship> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    Page<Relationship> findByFollowerIdAndStatus(UUID followerId, RelationshipStatus status, Pageable pageable);

    Page<Relationship> findByFollowingIdAndStatus(UUID followingId, RelationshipStatus status, Pageable pageable);

    long countByFollowingIdAndStatus(UUID followingId, RelationshipStatus status);

    long countByFollowerIdAndStatus(UUID followerId, RelationshipStatus status);
}
