package com.queueless.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a Virtual Queue Ticket in QueueLess.
 */
@Entity
@Table(name = "queue_tickets", indexes = {
    @Index(name = "idx_ticket_number", columnList = "ticketNumber", unique = true),
    @Index(name = "idx_status", columnList = "status")
})
public class QueueTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String ticketNumber;

    @Column(nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false, length = 64)
    private String serviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime servedAt;

    private LocalDateTime completedAt;

    @Column(length = 64)
    private String counterName;

    @Transient
    private Integer queuePosition;

    @Transient
    private Integer estimatedWaitMinutes;

    public QueueTicket() {
    }

    public QueueTicket(String ticketNumber, String customerName, String serviceType) {
        this.ticketNumber = ticketNumber;
        this.customerName = customerName;
        this.serviceType = (serviceType != null && !serviceType.trim().isEmpty()) ? serviceType : "General Counter";
        this.status = TicketStatus.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getServedAt() {
        return servedAt;
    }

    public void setServedAt(LocalDateTime servedAt) {
        this.servedAt = servedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }

    public Integer getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    @Override
    public String toString() {
        return "QueueTicket{" +
                "id=" + id +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", counterName='" + counterName + '\'' +
                ", queuePosition=" + queuePosition +
                '}';
    }
}
