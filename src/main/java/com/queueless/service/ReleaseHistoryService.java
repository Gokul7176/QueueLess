package com.queueless.service;

import com.queueless.dto.CurrentVersionDTO;
import com.queueless.dto.ReleaseRecordRequest;
import com.queueless.model.ReleaseHistory;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for QueueLess Version History and Release Management.
 */
public interface ReleaseHistoryService {

    /**
     * Idempotently record or update a deployment release record.
     */
    ReleaseHistory recordRelease(ReleaseRecordRequest request);

    /**
     * Retrieve all recorded releases, optionally filtered by deployment status.
     */
    List<ReleaseHistory> getAllReleases(String status);

    /**
     * Get comprehensive metadata for the currently active deployed version.
     */
    CurrentVersionDTO getCurrentRelease();

    /**
     * Look up a specific release record by its deployment ID.
     */
    Optional<ReleaseHistory> getReleaseByDeploymentId(String deploymentId);
}
