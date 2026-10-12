package com.queueless.service;

import com.queueless.dto.JoinQueueRequest;
import com.queueless.dto.QueueStatsDTO;
import com.queueless.exception.InvalidTicketStateException;
import com.queueless.exception.TicketNotFoundException;
import com.queueless.model.QueueTicket;
import com.queueless.model.TicketStatus;
import com.queueless.repository.QueueTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueueServiceImpl implements QueueService {

    private final QueueTicketRepository ticketRepository;
    private static final int ESTIMATED_MINUTES_PER_TICKET = 5;

    public QueueServiceImpl(QueueTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public synchronized QueueTicket joinQueue(JoinQueueRequest request) {
        Long maxId = ticketRepository.getMaxId();
        long nextSeq = (maxId != null ? maxId : 0) + 1;
        String ticketNumber = String.format("QL-%04d", nextSeq);

        QueueTicket ticket = new QueueTicket(ticketNumber, request.getCustomerName(), request.getServiceType());
        QueueTicket savedTicket = ticketRepository.save(ticket);

        enrichTicketPosition(savedTicket);
        return savedTicket;
    }

    @Override
    @Transactional(readOnly = true)
    public QueueTicket getTicketStatus(String ticketNumber) {
        QueueTicket ticket = ticketRepository.findByTicketNumber(ticketNumber.trim().toUpperCase())
                .orElseThrow(() -> new TicketNotFoundException(ticketNumber, true));

        enrichTicketPosition(ticket);
        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QueueTicket> getAllTickets(TicketStatus filterStatus) {
        List<QueueTicket> tickets;
        if (filterStatus != null) {
            tickets = ticketRepository.findByStatusOrderByCreatedAtAsc(filterStatus);
        } else {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        }

        for (QueueTicket t : tickets) {
            enrichTicketPosition(t);
        }
        return tickets;
    }

    @Override
    @Transactional
    public synchronized QueueTicket callNextTicket(String counterName) {
        QueueTicket nextTicket = ticketRepository.findFirstByStatusOrderByCreatedAtAsc(TicketStatus.WAITING)
                .orElseThrow(() -> new InvalidTicketStateException("No waiting customers currently in the queue."));

        if (nextTicket.getStatus() != TicketStatus.WAITING) {
            throw new InvalidTicketStateException("Ticket " + nextTicket.getTicketNumber() + " cannot be served twice.");
        }

        String assignedCounter = (counterName != null && !counterName.trim().isEmpty())
                ? counterName.trim()
                : "Counter 1";

        nextTicket.setStatus(TicketStatus.SERVING);
        nextTicket.setCounterName(assignedCounter);
        nextTicket.setServedAt(LocalDateTime.now());
        nextTicket.setQueuePosition(0);
        nextTicket.setEstimatedWaitMinutes(0);

        return ticketRepository.save(nextTicket);
    }

    @Override
    @Transactional
    public synchronized QueueTicket completeTicket(String ticketNumber) {
        QueueTicket ticket = ticketRepository.findByTicketNumber(ticketNumber.trim().toUpperCase())
                .orElseThrow(() -> new TicketNotFoundException(ticketNumber, true));

        if (ticket.getStatus() == TicketStatus.COMPLETED) {
            throw new InvalidTicketStateException("Ticket '" + ticket.getTicketNumber() + "' is already completed.");
        }

        if (ticket.getStatus() == TicketStatus.WAITING) {
            throw new InvalidTicketStateException("Ticket '" + ticket.getTicketNumber() + "' cannot be completed before being called to a service counter.");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException("Ticket '" + ticket.getTicketNumber() + "' was cancelled and cannot be completed.");
        }

        ticket.setStatus(TicketStatus.COMPLETED);
        ticket.setCompletedAt(LocalDateTime.now());
        ticket.setQueuePosition(null);
        ticket.setEstimatedWaitMinutes(0);

        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public synchronized QueueTicket cancelTicket(String ticketNumber) {
        QueueTicket ticket = ticketRepository.findByTicketNumber(ticketNumber.trim().toUpperCase())
                .orElseThrow(() -> new TicketNotFoundException(ticketNumber, true));

        if (ticket.getStatus() == TicketStatus.COMPLETED) {
            throw new InvalidTicketStateException("Completed ticket cannot be cancelled.");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setQueuePosition(null);
        ticket.setEstimatedWaitMinutes(0);

        return ticketRepository.save(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public QueueStatsDTO getQueueStats() {
        long waiting = ticketRepository.countByStatus(TicketStatus.WAITING);
        long serving = ticketRepository.countByStatus(TicketStatus.SERVING);
        long completed = ticketRepository.countByStatus(TicketStatus.COMPLETED);
        long cancelled = ticketRepository.countByStatus(TicketStatus.CANCELLED);
        long total = ticketRepository.count();

        int estimatedWait = (int) (waiting * ESTIMATED_MINUTES_PER_TICKET);

        return new QueueStatsDTO(waiting, serving, completed, cancelled, total, estimatedWait);
    }

    @Override
    @Transactional
    public synchronized void resetQueue() {
        ticketRepository.deleteAll();
    }

    private void enrichTicketPosition(QueueTicket ticket) {
        if (ticket.getStatus() == TicketStatus.WAITING) {
            List<QueueTicket> waitingTickets = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.WAITING);
            int position = 1;
            for (QueueTicket wt : waitingTickets) {
                if (wt.getId().equals(ticket.getId())) {
                    break;
                }
                position++;
            }
            ticket.setQueuePosition(position);
            ticket.setEstimatedWaitMinutes(position * ESTIMATED_MINUTES_PER_TICKET);
        } else if (ticket.getStatus() == TicketStatus.SERVING) {
            ticket.setQueuePosition(0);
            ticket.setEstimatedWaitMinutes(0);
        } else {
            ticket.setQueuePosition(null);
            ticket.setEstimatedWaitMinutes(0);
        }
    }
}
