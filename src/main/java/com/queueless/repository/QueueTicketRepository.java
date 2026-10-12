package com.queueless.repository;

import com.queueless.model.QueueTicket;
import com.queueless.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Layer for QueueTicket entity.
 */
@Repository
public interface QueueTicketRepository extends JpaRepository<QueueTicket, Long> {

    Optional<QueueTicket> findByTicketNumber(String ticketNumber);

    List<QueueTicket> findByStatusOrderByCreatedAtAsc(TicketStatus status);

    List<QueueTicket> findAllByOrderByCreatedAtDesc();

    Optional<QueueTicket> findFirstByStatusOrderByCreatedAtAsc(TicketStatus status);

    long countByStatus(TicketStatus status);

    boolean existsByTicketNumber(String ticketNumber);

    @Query("SELECT COALESCE(MAX(t.id), 0) FROM QueueTicket t")
    Long getMaxId();
}
