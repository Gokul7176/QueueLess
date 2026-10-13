package com.queueless.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an immutable or updated deployment release record in QueueLess.
 * Tracks source Git commit, Jenkins CI build, automated test results, Docker container tag,
 * and deployment verification status.
 */
@Entity
@Table(name = "release_history", indexes = {
    @Index(name = "idx_release_deployment_id", columnList = "deploymentId", unique = true),
    @Index(name = "idx_release_version", columnList = "version"),
    @Index(name = "idx_release_deployed_at", columnList = "deployedAt")
})
public class ReleaseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String version;

    @Column(nullable = false, length = 64)
    private String gitCommit;

    @Column(nullable = false, length = 16)
    private String gitCommitShort;

    @Column(nullable = false, length = 64)
    private String gitBranch;

    @Column(nullable = false, length = 500)
    private String commitMessage;

    @Column(length = 32)
    private String jenkinsBuildNumber;

    @Column(length = 255)
    private String jenkinsBuildUrl;

    @Column(nullable = false, length = 64)
    private String testResult;

    @Column(nullable = false, length = 32)
    private String deploymentStatus;

    @Column(nullable = false)
    private LocalDateTime deployedAt;

    @Column(nullable = false, length = 64)
    private String environment;

    @Column(nullable = false, length = 128)
    private String dockerImageTag;

    @Column(length = 128)
    private String pipelineJob;

    @Column(nullable = false, unique = true, length = 128)
    private String deploymentId;

    @Column(nullable = false)
    private boolean isCurrent;

    public ReleaseHistory() {
        this.deployedAt = LocalDateTime.now();
        this.isCurrent = false;
    }

    public ReleaseHistory(String version, String gitCommit, String gitCommitShort, String gitBranch,
                          String commitMessage, String jenkinsBuildNumber, String jenkinsBuildUrl,
                          String testResult, String deploymentStatus, String environment,
                          String dockerImageTag, String pipelineJob, String deploymentId) {
        this.version = version;
        this.gitCommit = gitCommit;
        this.gitCommitShort = gitCommitShort;
        this.gitBranch = gitBranch;
        this.commitMessage = commitMessage;
        this.jenkinsBuildNumber = jenkinsBuildNumber;
        this.jenkinsBuildUrl = jenkinsBuildUrl;
        this.testResult = testResult;
        this.deploymentStatus = deploymentStatus;
        this.environment = environment;
        this.dockerImageTag = dockerImageTag;
        this.pipelineJob = pipelineJob;
        this.deploymentId = deploymentId;
        this.deployedAt = LocalDateTime.now();
        this.isCurrent = false;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGitCommit() {
        return gitCommit;
    }

    public void setGitCommit(String gitCommit) {
        this.gitCommit = gitCommit;
        if (gitCommit != null && (this.gitCommitShort == null || this.gitCommitShort.isEmpty())) {
            this.gitCommitShort = gitCommit.length() >= 7 ? gitCommit.substring(0, 7) : gitCommit;
        }
    }

    public String getGitCommitShort() {
        return gitCommitShort;
    }

    public void setGitCommitShort(String gitCommitShort) {
        this.gitCommitShort = gitCommitShort;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getJenkinsBuildNumber() {
        return jenkinsBuildNumber;
    }

    public void setJenkinsBuildNumber(String jenkinsBuildNumber) {
        this.jenkinsBuildNumber = jenkinsBuildNumber;
    }

    public String getJenkinsBuildUrl() {
        return jenkinsBuildUrl;
    }

    public void setJenkinsBuildUrl(String jenkinsBuildUrl) {
        this.jenkinsBuildUrl = jenkinsBuildUrl;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getDeploymentStatus() {
        return deploymentStatus;
    }

    public void setDeploymentStatus(String deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
    }

    public LocalDateTime getDeployedAt() {
        return deployedAt;
    }

    public void setDeployedAt(LocalDateTime deployedAt) {
        this.deployedAt = deployedAt;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getDockerImageTag() {
        return dockerImageTag;
    }

    public void setDockerImageTag(String dockerImageTag) {
        this.dockerImageTag = dockerImageTag;
    }

    public String getPipelineJob() {
        return pipelineJob;
    }

    public void setPipelineJob(String pipelineJob) {
        this.pipelineJob = pipelineJob;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    @Override
    public String toString() {
        return "ReleaseHistory{" +
                "id=" + id +
                ", version='" + version + '\'' +
                ", gitCommitShort='" + gitCommitShort + '\'' +
                ", gitBranch='" + gitBranch + '\'' +
                ", jenkinsBuildNumber='" + jenkinsBuildNumber + '\'' +
                ", testResult='" + testResult + '\'' +
                ", deploymentStatus='" + deploymentStatus + '\'' +
                ", deployedAt=" + deployedAt +
                ", isCurrent=" + isCurrent +
                '}';
    }
}
