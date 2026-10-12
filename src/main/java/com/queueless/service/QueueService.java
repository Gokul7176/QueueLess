package com.queueless.service;

import com.queueless.dto.JoinQueueRequest;
import com.queueless.dto.QueueStatsDTO;
import com.queueless.model.QueueTicket;
import com.queueless.model.TicketStatus;

import java.util.List;

public interface QueueService {

    QueueTicket joinQueue(JoinQueueRequest request);

    QueueTicket getTicketStatus(String ticketNumber);

    List<QueueTicket> getAllTickets(TicketStatus filterStatus);

    QueueTicket callNextTicket(String counterName);

    QueueTicket completeTicket(String ticketNumber);

    QueueTicket cancelTicket(String ticketNumber);

    QueueStatsDTO getQueueStats();

    void resetQueue();
}
