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

    @Test
    @DisplayName("Test 10: Admin calls waiting ticket before estimated wait expires succeeds immediately")
    void testCallNextTicket_BeforeEstimatedWaitExpires_Success() {
        QueueTicket freshTicket = new QueueTicket("QL-0010", "Alice Quick", "Express Desk");
        freshTicket.setStatus(TicketStatus.WAITING);
        freshTicket.setCreatedAt(LocalDateTime.now().minusSeconds(5));
        freshTicket.setEstimatedWaitMinutes(15); // Informational wait is in the future

        when(ticketRepository.findFirstByStatusOrderByCreatedAtAsc(TicketStatus.WAITING))
                .thenReturn(Optional.of(freshTicket));
        when(ticketRepository.save(any(QueueTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QueueTicket servedTicket = queueService.callNextTicket("Counter 1");

        assertNotNull(servedTicket);
        assertEquals(TicketStatus.SERVING, servedTicket.getStatus());
        assertEquals("Counter 1", servedTicket.getCounterName());
        assertNotNull(servedTicket.getServedAt());
        assertEquals(0, servedTicket.getQueuePosition());
        assertEquals(0, servedTicket.getEstimatedWaitMinutes());
    }

    @Test
    @DisplayName("Test 11: Admin completes serving ticket before original estimated wait expires succeeds immediately")
    void testCompleteTicket_BeforeOriginalEstimatedWaitExpires_Success() {
        QueueTicket servingTicket = new QueueTicket("QL-0011", "Bob Fast", "General");
        servingTicket.setStatus(TicketStatus.SERVING);
        servingTicket.setCounterName("Counter 2");
        servingTicket.setCreatedAt(LocalDateTime.now().minusSeconds(20)); // Created 20s ago, well before 10m estimate
        servingTicket.setServedAt(LocalDateTime.now().minusSeconds(5));

        when(ticketRepository.findByTicketNumber("QL-0011")).thenReturn(Optional.of(servingTicket));
        when(ticketRepository.save(any(QueueTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QueueTicket completedTicket = queueService.completeTicket("QL-0011");

        assertNotNull(completedTicket);
        assertEquals(TicketStatus.COMPLETED, completedTicket.getStatus());
        assertNotNull(completedTicket.getCompletedAt());
        assertNull(completedTicket.getQueuePosition());
        assertEquals(0, completedTicket.getEstimatedWaitMinutes());
    }

    @Test
    @DisplayName("Test 12: Admin calling next customer strictly honors FIFO ordering regardless of wait duration")
    void testCallNextTicket_StrictFifoOrder_SelectsOldestWaitingTicket() {
        QueueTicket olderTicket = new QueueTicket("QL-0001", "Early Bird", "Desk");
        olderTicket.setStatus(TicketStatus.WAITING);
        olderTicket.setCreatedAt(LocalDateTime.now().minusMinutes(10));

        when(ticketRepository.findFirstByStatusOrderByCreatedAtAsc(TicketStatus.WAITING))
                .thenReturn(Optional.of(olderTicket));
        when(ticketRepository.save(any(QueueTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QueueTicket servedTicket = queueService.callNextTicket("Counter 3");

        assertEquals("QL-0001", servedTicket.getTicketNumber());
        assertEquals(TicketStatus.SERVING, servedTicket.getStatus());
    }

    @Test
    @DisplayName("Test 13: Cancelling a WAITING ticket transitions to CANCELLED and clears wait time")
    void testCancelTicket_WaitingTicket_Success() {
        QueueTicket waitingTicket = new QueueTicket("QL-0015", "Eve Polastri", "Security");
        waitingTicket.setStatus(TicketStatus.WAITING);
        waitingTicket.setEstimatedWaitMinutes(10);

        when(ticketRepository.findByTicketNumber("QL-0015")).thenReturn(Optional.of(waitingTicket));
        when(ticketRepository.save(any(QueueTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QueueTicket cancelled = queueService.cancelTicket("QL-0015");

        assertNotNull(cancelled);
        assertEquals(TicketStatus.CANCELLED, cancelled.getStatus());
        assertNull(cancelled.getQueuePosition());
        assertEquals(0, cancelled.getEstimatedWaitMinutes());
    }

    @Test
    @DisplayName("Test 14: Completing a CANCELLED ticket throws InvalidTicketStateException")
    void testCompleteTicket_CancelledTicket_ThrowsException() {
        QueueTicket cancelledTicket = new QueueTicket("QL-0016", "Villanelle", "Operations");
        cancelledTicket.setStatus(TicketStatus.CANCELLED);

        when(ticketRepository.findByTicketNumber("QL-0016")).thenReturn(Optional.of(cancelledTicket));

        InvalidTicketStateException ex = assertThrows(InvalidTicketStateException.class, () -> {
            queueService.completeTicket("QL-0016");
        });
        assertTrue(ex.getMessage().contains("was cancelled and cannot be completed"));
    }
}
