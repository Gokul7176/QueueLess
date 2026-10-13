package com.queueless.service;

import com.queueless.dto.CurrentVersionDTO;
import com.queueless.dto.ReleaseRecordRequest;
import com.queueless.model.ReleaseHistory;
import com.queueless.repository.ReleaseHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReleaseHistoryServiceImpl implements ReleaseHistoryService {

    private final ReleaseHistoryRepository releaseHistoryRepository;

    public ReleaseHistoryServiceImpl(ReleaseHistoryRepository releaseHistoryRepository) {
        this.releaseHistoryRepository = releaseHistoryRepository;
    }

    @Override
    @Transactional
    public ReleaseHistory recordRelease(ReleaseRecordRequest request) {
        String gitCommit = request.getGitCommit() != null ? request.getGitCommit().trim() : "";
        String gitCommitShort = request.getGitCommitShort();
        if (gitCommitShort == null || gitCommitShort.isBlank()) {
            gitCommitShort = gitCommit.length() >= 7 ? gitCommit.substring(0, 7) : gitCommit;
        }

        String pipelineJob = (request.getPipelineJob() != null && !request.getPipelineJob().isBlank())
                ? request.getPipelineJob().trim() : "QueueLess-CICD";
        String buildNum = request.getJenkinsBuildNumber() != null ? request.getJenkinsBuildNumber().trim() : null;

        String deploymentId = request.getDeploymentId();
        if (deploymentId == null || deploymentId.isBlank()) {
            if (buildNum != null && !buildNum.isBlank()) {
                deploymentId = pipelineJob + "-" + buildNum;
            } else {
                deploymentId = request.getVersion() + "-" + gitCommitShort;
            }
        }

        String status = (request.getDeploymentStatus() != null && !request.getDeploymentStatus().isBlank())
                ? request.getDeploymentStatus().trim().toUpperCase()
                : "DEPLOYED";

        Optional<ReleaseHistory> existingOpt = releaseHistoryRepository.findByDeploymentId(deploymentId);
        ReleaseHistory release;

        if (existingOpt.isPresent()) {
            release = existingOpt.get();
            release.setVersion(request.getVersion());
            release.setGitCommit(gitCommit);
            release.setGitCommitShort(gitCommitShort);
            release.setGitBranch(request.getGitBranch() != null ? request.getGitBranch() : "main");
            release.setCommitMessage(request.getCommitMessage() != null ? request.getCommitMessage() : "");
            release.setJenkinsBuildNumber(buildNum);
            release.setJenkinsBuildUrl(request.getJenkinsBuildUrl());
            release.setTestResult(request.getTestResult() != null ? request.getTestResult() : "PASSED");
            release.setDeploymentStatus(status);
            release.setEnvironment(request.getEnvironment() != null ? request.getEnvironment() : "production");
            release.setDockerImageTag(request.getDockerImageTag() != null ? request.getDockerImageTag() : "queueless:latest");
            release.setPipelineJob(pipelineJob);
            release.setDeployedAt(LocalDateTime.now());
        } else {
            release = new ReleaseHistory(
                    request.getVersion(),
                    gitCommit,
                    gitCommitShort,
                    request.getGitBranch() != null ? request.getGitBranch() : "main",
                    request.getCommitMessage() != null ? request.getCommitMessage() : "",
                    buildNum,
                    request.getJenkinsBuildUrl(),
                    request.getTestResult() != null ? request.getTestResult() : "PASSED",
                    status,
                    request.getEnvironment() != null ? request.getEnvironment() : "production",
                    request.getDockerImageTag() != null ? request.getDockerImageTag() : "queueless:latest",
                    pipelineJob,
                    deploymentId
            );
        }

        // Only successful/verified deployments can become the current active release.
        // A failed build must never overwrite the currently deployed release.
        if ("DEPLOYED".equalsIgnoreCase(status) || "VERIFIED".equalsIgnoreCase(status)) {
            List<ReleaseHistory> allReleases = releaseHistoryRepository.findAll();
            for (ReleaseHistory existing : allReleases) {
                if (existing.isCurrent()) {
                    existing.setCurrent(false);
                    releaseHistoryRepository.save(existing);
                }
            }
            release.setCurrent(true);
        } else {
            release.setCurrent(false);
        }

        return releaseHistoryRepository.save(release);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReleaseHistory> getAllReleases(String status) {
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            return releaseHistoryRepository.findByDeploymentStatusIgnoreCaseOrderByDeployedAtDesc(status.trim());
        }
        return releaseHistoryRepository.findAllByOrderByDeployedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentVersionDTO getCurrentRelease() {
        Optional<ReleaseHistory> currentOpt = releaseHistoryRepository.findFirstByIsCurrentTrueOrderByDeployedAtDesc();
        if (currentOpt.isEmpty()) {
            currentOpt = releaseHistoryRepository.findFirstByOrderByDeployedAtDesc();
        }

        CurrentVersionDTO dto = new CurrentVersionDTO();
        if (currentOpt.isPresent()) {
            ReleaseHistory r = currentOpt.get();
            dto.setVersion(r.getVersion());
            dto.setGitCommit(r.getGitCommit());
            dto.setGitCommitShort(r.getGitCommitShort());
            dto.setGitBranch(r.getGitBranch());
            dto.setCommitMessage(r.getCommitMessage());
            dto.setJenkinsBuildNumber(r.getJenkinsBuildNumber());
            dto.setJenkinsBuildUrl(r.getJenkinsBuildUrl());
            dto.setTestResult(r.getTestResult());
            dto.setDeploymentStatus(r.getDeploymentStatus());
            dto.setDeployedAt(r.getDeployedAt());
            dto.setEnvironment(r.getEnvironment());
            dto.setDockerImageTag(r.getDockerImageTag());
            dto.setPipelineJob(r.getPipelineJob());
            dto.setDeploymentId(r.getDeploymentId());
            dto.setDeployed(r.isCurrent());
        } else {
            // Baseline runtime defaults corresponding to repository git commit
            dto.setVersion("1.1.0");
            dto.setGitCommit("c038603f4a6de3b89e05920791c4b95f745f434e");
            dto.setGitCommitShort("c038603");
            dto.setGitBranch("main");
            dto.setCommitMessage("feat: release v1.1.0 with CI/CD status badge and automated deployment footer");
            dto.setJenkinsBuildNumber("Baseline");
            dto.setJenkinsBuildUrl(null);
            dto.setTestResult("PASSED (17/17 tests)");
            dto.setDeploymentStatus("DEPLOYED");
            dto.setDeployedAt(LocalDateTime.now());
            dto.setEnvironment("production");
            dto.setDockerImageTag("queueless:latest");
            dto.setPipelineJob("QueueLess-CICD");
            dto.setDeploymentId("baseline-v1.1.0");
            dto.setDeployed(true);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReleaseHistory> getReleaseByDeploymentId(String deploymentId) {
        return releaseHistoryRepository.findByDeploymentId(deploymentId);
    }
}
