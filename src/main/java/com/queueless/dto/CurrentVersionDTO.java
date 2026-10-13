package com.queueless.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object providing comprehensive metadata about the currently active
 * deployed release and server runtime.
 */
public class CurrentVersionDTO {

    private String version;
    private String gitCommit;
    private String gitCommitShort;
    private String gitBranch;
    private String commitMessage;
    private String jenkinsBuildNumber;
    private String jenkinsBuildUrl;
    private String testResult;
    private String deploymentStatus;
    private LocalDateTime deployedAt;
    private String environment;
    private String dockerImageTag;
    private String pipelineJob;
    private String deploymentId;
    private boolean isDeployed;
    private String serverTime;

    public CurrentVersionDTO() {
        this.serverTime = LocalDateTime.now().toString();
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

    public boolean isDeployed() {
        return isDeployed;
    }

    public void setDeployed(boolean deployed) {
        isDeployed = deployed;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }
}
