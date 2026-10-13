package com.queueless.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for recording or updating deployment release metadata
 * submitted by the Jenkins CI/CD pipeline.
 */
public class ReleaseRecordRequest {

    @NotBlank(message = "Version cannot be blank")
    @Size(max = 32, message = "Version length cannot exceed 32 characters")
    private String version;

    @NotBlank(message = "Git commit hash cannot be blank")
    @Size(max = 64, message = "Git commit length cannot exceed 64 characters")
    private String gitCommit;

    @Size(max = 16, message = "Git commit short length cannot exceed 16 characters")
    private String gitCommitShort;

    @Size(max = 64, message = "Git branch length cannot exceed 64 characters")
    private String gitBranch;

    @Size(max = 2000, message = "Commit message length cannot exceed 2000 characters")
    private String commitMessage;

    @Size(max = 32, message = "Jenkins build number cannot exceed 32 characters")
    private String jenkinsBuildNumber;

    @Size(max = 255, message = "Jenkins build URL cannot exceed 255 characters")
    private String jenkinsBuildUrl;

    @Size(max = 64, message = "Test result cannot exceed 64 characters")
    private String testResult;

    @Size(max = 32, message = "Deployment status cannot exceed 32 characters")
    private String deploymentStatus;

    @Size(max = 64, message = "Environment cannot exceed 64 characters")
    private String environment;

    @Size(max = 128, message = "Docker image tag cannot exceed 128 characters")
    private String dockerImageTag;

    @Size(max = 128, message = "Pipeline job cannot exceed 128 characters")
    private String pipelineJob;

    @Size(max = 128, message = "Deployment ID cannot exceed 128 characters")
    private String deploymentId;

    public ReleaseRecordRequest() {
    }

    public ReleaseRecordRequest(String version, String gitCommit, String gitCommitShort, String gitBranch,
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
    }

    // Getters and Setters

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
}
