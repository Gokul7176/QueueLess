package com.queueless.dto;

public class QueueStatsDTO {

    private long waitingCount;
    private long servingCount;
    private long completedCount;
    private long cancelledCount;
    private long totalCount;
    private int estimatedWaitMinutes;

    public QueueStatsDTO() {
    }

    public QueueStatsDTO(long waitingCount, long servingCount, long completedCount,
                         long cancelledCount, long totalCount, int estimatedWaitMinutes) {
        this.waitingCount = waitingCount;
        this.servingCount = servingCount;
        this.completedCount = completedCount;
        this.cancelledCount = cancelledCount;
        this.totalCount = totalCount;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public long getWaitingCount() {
        return waitingCount;
    }

    public void setWaitingCount(long waitingCount) {
        this.waitingCount = waitingCount;
    }

    public long getServingCount() {
        return servingCount;
    }

    public void setServingCount(long servingCount) {
        this.servingCount = servingCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }

    public long getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(long cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }
}
