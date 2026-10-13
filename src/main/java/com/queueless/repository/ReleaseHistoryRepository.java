package com.queueless.repository;

import com.queueless.model.ReleaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for ReleaseHistory entities.
 */
@Repository
public interface ReleaseHistoryRepository extends JpaRepository<ReleaseHistory, Long> {

    Optional<ReleaseHistory> findByDeploymentId(String deploymentId);

    Optional<ReleaseHistory> findFirstByIsCurrentTrueOrderByDeployedAtDesc();

    Optional<ReleaseHistory> findFirstByOrderByDeployedAtDesc();

    List<ReleaseHistory> findAllByOrderByDeployedAtDesc();

    List<ReleaseHistory> findByDeploymentStatusIgnoreCaseOrderByDeployedAtDesc(String deploymentStatus);

    List<ReleaseHistory> findByVersionOrderByDeployedAtDesc(String version);
}
