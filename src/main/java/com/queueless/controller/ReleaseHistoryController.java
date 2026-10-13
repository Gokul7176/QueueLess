package com.queueless.controller;

import com.queueless.dto.ApiResponse;
import com.queueless.dto.CurrentVersionDTO;
import com.queueless.dto.ReleaseRecordRequest;
import com.queueless.model.ReleaseHistory;
import com.queueless.service.ReleaseHistoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Version History Monitoring and CI/CD Release Management.
 */
@RestController
@RequestMapping("/api/version-history")
@CrossOrigin(origins = "*")
public class ReleaseHistoryController {

    private final ReleaseHistoryService releaseHistoryService;

    @Value("${queueless.release-token:}")
    private String configuredReleaseToken;

    public ReleaseHistoryController(ReleaseHistoryService releaseHistoryService) {
        this.releaseHistoryService = releaseHistoryService;
    }

    /**
     * Read-only endpoint returning all deployment release history records.
     * Accessible by the frontend Version History dashboard and monitoring systems.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReleaseHistory>>> getAllReleases(
            @RequestParam(required = false) String status) {
        List<ReleaseHistory> releases = releaseHistoryService.getAllReleases(status);
        return ResponseEntity.ok(ApiResponse.success(releases));
    }

    /**
     * Read-only endpoint returning comprehensive details for the currently active deployed release.
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<CurrentVersionDTO>> getCurrentVersion() {
        CurrentVersionDTO currentVersion = releaseHistoryService.getCurrentRelease();
        return ResponseEntity.ok(ApiResponse.success(currentVersion));
    }

    /**
     * Look up a specific release record by its deployment identifier.
     */
    @GetMapping("/{deploymentId}")
    public ResponseEntity<ApiResponse<ReleaseHistory>> getReleaseByDeploymentId(
            @PathVariable String deploymentId) {
        return releaseHistoryService.getReleaseByDeploymentId(deploymentId)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Release record with deployment ID '" + deploymentId + "' not found")));
    }

    /**
     * Secure endpoint called by the Jenkins CI/CD pipeline after healthcheck verification passes.
     * Protected with token verification via X-Release-Token or Authorization Bearer header.
     * Repeated submissions with identical deploymentId are idempotent.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReleaseHistory>> recordRelease(
            @RequestHeader(value = "X-Release-Token", required = false) String tokenHeader,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ReleaseRecordRequest request) {

        // Enforce token security if a secret token is configured in application properties/environment
        if (configuredReleaseToken != null && !configuredReleaseToken.trim().isEmpty()) {
            boolean validToken = false;
            if (tokenHeader != null && tokenHeader.trim().equals(configuredReleaseToken.trim())) {
                validToken = true;
            } else if (authHeader != null && authHeader.trim().startsWith("Bearer ")) {
                String bearerToken = authHeader.trim().substring(7).trim();
                if (bearerToken.equals(configuredReleaseToken.trim())) {
                    validToken = true;
                }
            }

            if (!validToken) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Unauthorized: Invalid or missing release authentication token"));
            }
        }

        ReleaseHistory recorded = releaseHistoryService.recordRelease(request);
        return new ResponseEntity<>(
                ApiResponse.success("Release deployment record successfully recorded", recorded),
                HttpStatus.CREATED
        );
    }
}
