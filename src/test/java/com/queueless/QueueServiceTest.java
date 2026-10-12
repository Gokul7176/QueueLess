package com.queueless;

import com.queueless.dto.JoinQueueRequest;
import com.queueless.dto.QueueStatsDTO;
import com.queueless.exception.InvalidTicketStateException;
import com.queueless.exception.TicketNotFoundException;
import com.queueless.model.QueueTicket;
import com.queueless.model.TicketStatus;
import com.queueless.repository.QueueTicketRepository;
import com.queueless.service.QueueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private QueueTicketRepository ticketRepository;

    @InjectMocks
    private QueueServiceImpl queueService;

    private QueueTicket sampleWaitingTicket;
    private QueueTicket sampleServingTicket;

    @BeforeEach
    void setUp() {
        sampleWaitingTicket = new QueueTicket("QL-0001", "John Doe", "Customer Service");
        sampleWaitingTicket.setId(1L);

        sampleServingTicket = new QueueTicket("QL-0002", "Jane Smith", "Technical Support");
        sampleServingTicket.setId(2L);
        sampleServingTicket.setStatus(TicketStatus.SERVING);
        sampleServingTicket.setCounterName("Counter 1");
    }

    @Test
    @DisplayName("Test 1: Successfully join queue and generate sequential ticket")
    void testJoinQueue_Success() {
        when(ticketRepository.getMaxId()).thenReturn(0L);
        when(ticketRepository.save(any(QueueTicket.class))).thenAnswer(invocation -> {
            QueueTicket t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.WAITING))
                .thenReturn(Collections.singletonList(sampleWaitingTicket));

        JoinQueueRequest request = new JoinQueueRequest("John Doe", "Customer Service");
        QueueTicket createdTicket = queueService.joinQueue(request);

        assertNotNull(createdTicket);
        assertEquals("QL-0001", createdTicket.getTicketNumber());
        assertEquals("John Doe", createdTicket.getCustomerName());
        assertEquals(TicketStatus.WAITING, createdTicket.getStatus());
        assertEquals(1, createdTicket.getQueuePosition());
        assertEquals(5, createdTicket.getEstimatedWaitMinutes());
        verify(ticketRepository, times(1)).save(any(QueueTicket.class));
    }

    @Test
    @DisplayName("Test 2: Retrieve ticket status with accurate queue position and wait time")
    void testGetTicketStatus_Success() {
        QueueTicket ticket1 = new QueueTicket("QL-0001", "User One", "General");
        ticket1.setId(1L);
        QueueTicket ticket2 = new QueueTicket("QL-0002", "User Two", "General");
        ticket2.setId(2L);

        when(ticketRepository.findByTicketNumber("QL-0002")).thenReturn(Optional.of(ticket2));
        when(ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.WAITING))
                .thenReturn(Arrays.asList(ticket1, ticket2));

        QueueTicket result = queueService.getTicketStatus("QL-0002");

        assertNotNull(result);
        assertEquals("QL-0002", result.getTicketNumber());
        assertEquals(2, result.getQueuePosition());
        assertEquals(10, result.getEstimatedWaitMinutes());
    }

    @Test
    @DisplayName("Test 3: Look up invalid ticket throws TicketNotFoundException")
    void testGetTicketStatus_NotFound_ThrowsException() {
        when(ticketRepository.findByTicketNumber("QL-9999")).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> {
            queueService.getTicketStatus("QL-9999");
        });
    }

    @Test
    @DisplayName("Test 4: Call next waiting customer transitions status to SERVING")
    void testCallNextTicket_Success() {
        when(ticketRepository.findFirstByStatusOrderByCreatedAtAsc(TicketStatus.WAITING))
                .thenReturn(Optional.of(sampleWaitingTicket));
        when(ticketRepository.save(any(QueueTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QueueTicket servedTicket = queueService.callNextTicket("Counter 2");

        assertNotNull(servedTicket);
        assertEquals(TicketStatus.SERVING, servedTicket.getStatus());
        assertEquals("Counter 2", servedTicket.getCounterName());
        assertNotNull(servedTicket.getServedAt());
        assertEquals(0, servedTicket.getQueuePosition());
        verify(ticketRepository).save(sampleWaitingTicket);
    }

    @Test
    @DisplayName("Test 5: Call next ticket when queue is empty throws InvalidTicketStateException")
    void testCallNextTicket_EmptyQueue_ThrowsException() {
        when(ticketRepository.findFirstByStatusOrderByCreatedAtAsc(TicketStatus.WAITING))
                .thenReturn(Optional.empty());

        InvalidTicketStateException exception = assertThrows(InvalidTicketStateException.class, () -> {
            queueService.callNextTicket("Counter 1");
        });

        assertTrue(exception.getMessage().contains("No waiting customers"));
    }

    @Test
    @DisplayName("Test 6: Mark serving ticket as COMPLETED successfully")
    void testCompleteTicket_Success() {
        when(ticketRepository.findByTicketNumber("QL-0002")).thenReturn(Optional.of(sampleServingTicket));
        when(ticketRepository.save(any(QueueTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QueueTicket completedTicket = queueService.completeTicket("QL-0002");

        assertNotNull(completedTicket);
        assertEquals(TicketStatus.COMPLETED, completedTicket.getStatus());
        assertNotNull(completedTicket.getCompletedAt());
        assertNull(completedTicket.getQueuePosition());
    }

    @Test
    @DisplayName("Test 7: Completing a ticket that is still WAITING throws InvalidTicketStateException")
    void testCompleteTicket_WaitingTicket_ThrowsException() {
        when(ticketRepository.findByTicketNumber("QL-0001")).thenReturn(Optional.of(sampleWaitingTicket));

        InvalidTicketStateException exception = assertThrows(InvalidTicketStateException.class, () -> {
            queueService.completeTicket("QL-0001");
        });

        assertTrue(exception.getMessage().contains("cannot be completed before being called"));
    }

    @Test
    @DisplayName("Test 8: Completing an already COMPLETED ticket throws InvalidTicketStateException")
    void testCompleteTicket_AlreadyCompleted_ThrowsException() {
        QueueTicket completedTicket = new QueueTicket("QL-0003", "Old Ticket", "Service");
        completedTicket.setStatus(TicketStatus.COMPLETED);
        when(ticketRepository.findByTicketNumber("QL-0003")).thenReturn(Optional.of(completedTicket));

        assertThrows(InvalidTicketStateException.class, () -> {
            queueService.completeTicket("QL-0003");
        });
    }

    @Test
    @DisplayName("Test 9: Queue statistics calculations reflect counts accurately")
    void testGetQueueStats_Success() {
        when(ticketRepository.countByStatus(TicketStatus.WAITING)).thenReturn(4L);
        when(ticketRepository.countByStatus(TicketStatus.SERVING)).thenReturn(2L);
        when(ticketRepository.countByStatus(TicketStatus.COMPLETED)).thenReturn(10L);
        when(ticketRepository.countByStatus(TicketStatus.CANCELLED)).thenReturn(1L);
        when(ticketRepository.count()).thenReturn(17L);

        QueueStatsDTO stats = queueService.getQueueStats();

        assertNotNull(stats);
        assertEquals(4L, stats.getWaitingCount());
        assertEquals(2L, stats.getServingCount());
        assertEquals(10L, stats.getCompletedCount());
        assertEquals(1L, stats.getCancelledCount());
        assertEquals(17L, stats.getTotalCount());
        assertEquals(20, stats.getEstimatedWaitMinutes()); // 4 * 5 = 20 mins
    }
}
