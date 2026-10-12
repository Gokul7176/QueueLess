package com.queueless.controller;

import com.queueless.dto.ApiResponse;
import com.queueless.dto.CallNextRequest;
import com.queueless.dto.JoinQueueRequest;
import com.queueless.dto.QueueStatsDTO;
import com.queueless.model.QueueTicket;
import com.queueless.model.TicketStatus;
import com.queueless.service.QueueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Virtual Queue Management.
 */
@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Join the virtual queue.
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<QueueTicket>> joinQueue(@Valid @RequestBody JoinQueueRequest request) {
        QueueTicket ticket = queueService.joinQueue(request);
        return new ResponseEntity<>(
                ApiResponse.success("Successfully joined the queue. Your ticket number is " + ticket.getTicketNumber(), ticket),
                HttpStatus.CREATED
        );
    }

    /**
     * Retrieve all queue tickets with optional status filtering.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<QueueTicket>>> getAllTickets(
            @RequestParam(required = false) TicketStatus status) {
        List<QueueTicket> tickets = queueService.getAllTickets(status);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    /**
     * Check status of a specific ticket.
     */
    @GetMapping("/{ticketNumber}")
    public ResponseEntity<ApiResponse<QueueTicket>> getTicketStatus(@PathVariable String ticketNumber) {
        QueueTicket ticket = queueService.getTicketStatus(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    /**
     * Call the next waiting customer to a service counter.
     */
    @PostMapping("/call-next")
    public ResponseEntity<ApiResponse<QueueTicket>> callNextTicket(
            @RequestBody(required = false) CallNextRequest request) {
        String counter = (request != null && request.getCounterName() != null)
                ? request.getCounterName()
                : "Counter 1";
        QueueTicket ticket = queueService.callNextTicket(counter);
        return ResponseEntity.ok(ApiResponse.success("Now calling ticket " + ticket.getTicketNumber(), ticket));
    }

    /**
     * Mark a ticket as completed.
     */
    @PostMapping("/{ticketNumber}/complete")
    public ResponseEntity<ApiResponse<QueueTicket>> completeTicket(@PathVariable String ticketNumber) {
        QueueTicket ticket = queueService.completeTicket(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success("Ticket " + ticketNumber + " marked as completed.", ticket));
    }

    /**
     * Cancel a ticket.
     */
    @PostMapping("/{ticketNumber}/cancel")
    public ResponseEntity<ApiResponse<QueueTicket>> cancelTicket(@PathVariable String ticketNumber) {
        QueueTicket ticket = queueService.cancelTicket(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success("Ticket " + ticketNumber + " cancelled.", ticket));
    }

    /**
     * Get queue statistics summary.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<QueueStatsDTO>> getQueueStats() {
        QueueStatsDTO stats = queueService.getQueueStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Administrative queue reset.
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<String>> resetQueue() {
        queueService.resetQueue();
        return ResponseEntity.ok(ApiResponse.success("Queue has been reset successfully.", "OK"));
    }
}
