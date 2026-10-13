package com.queueless;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueless.dto.CallNextRequest;
import com.queueless.dto.JoinQueueRequest;
import com.queueless.model.QueueTicket;
import com.queueless.model.TicketStatus;
import com.queueless.repository.QueueTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QueueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QueueTicketRepository ticketRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUpDatabase() {
        ticketRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Test 1: POST /api/tickets/join successfully enqueues customer")
    void testJoinQueueEndpoint_Success() throws Exception {
        JoinQueueRequest request = new JoinQueueRequest("Sarah Connor", "Consultation");

        mockMvc.perform(post("/api/tickets/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.customerName", is("Sarah Connor")))
                .andExpect(jsonPath("$.data.ticketNumber", notNullValue()))
                .andExpect(jsonPath("$.data.status", is("WAITING")))
                .andExpect(jsonPath("$.data.queuePosition", is(1)));
    }

    @Test
    @DisplayName("Integration Test 2: POST /api/tickets/join with blank name returns 400 Bad Request")
    void testJoinQueueEndpoint_ValidationError_BlankName() throws Exception {
        JoinQueueRequest request = new JoinQueueRequest("", "Consultation");

        mockMvc.perform(post("/api/tickets/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Error")));
    }

    @Test
    @DisplayName("Integration Test 3: GET /api/tickets returns list of all tickets")
    void testGetAllTicketsEndpoint_Success() throws Exception {
        QueueTicket ticket = new QueueTicket("QL-0001", "Bruce Wayne", "VIP Support");
        ticketRepository.save(ticket);

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].customerName", is("Bruce Wayne")));
    }

    @Test
    @DisplayName("Integration Test 4: GET /api/tickets/{ticketNumber} retrieves ticket details")
    void testGetTicketStatusEndpoint_Success() throws Exception {
        QueueTicket ticket = new QueueTicket("QL-0005", "Clark Kent", "Journalism Desk");
        ticketRepository.save(ticket);

        mockMvc.perform(get("/api/tickets/QL-0005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.ticketNumber", is("QL-0005")))
                .andExpect(jsonPath("$.data.customerName", is("Clark Kent")));
    }

    @Test
    @DisplayName("Integration Test 5: GET /api/tickets/{ticketNumber} with nonexistent ID returns 404 Not Found")
    void testGetTicketStatusEndpoint_NotFound() throws Exception {
        mockMvc.perform(get("/api/tickets/QL-NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("Integration Test 6: POST /api/tickets/call-next calls next customer in FIFO order")
    void testCallNextEndpoint_Success() throws Exception {
        QueueTicket ticket1 = new QueueTicket("QL-0010", "Barry Allen", "Speed Inquiry");
        QueueTicket ticket2 = new QueueTicket("QL-0011", "Hal Jordan", "Ring Support");
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        CallNextRequest callRequest = new CallNextRequest("Counter 3");

        mockMvc.perform(post("/api/tickets/call-next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.ticketNumber", is("QL-0010")))
                .andExpect(jsonPath("$.data.status", is("SERVING")))
                .andExpect(jsonPath("$.data.counterName", is("Counter 3")));
    }

    @Test
    @DisplayName("Integration Test 7: POST /api/tickets/{ticketNumber}/complete completes serving ticket")
    void testCompleteTicketEndpoint_Success() throws Exception {
        QueueTicket ticket = new QueueTicket("QL-0020", "Arthur Curry", "Maritime Service");
        ticket.setStatus(TicketStatus.SERVING);
        ticket.setCounterName("Counter 1");
        ticketRepository.save(ticket);

        mockMvc.perform(post("/api/tickets/QL-0020/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.ticketNumber", is("QL-0020")))
                .andExpect(jsonPath("$.data.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("Integration Test 8: GET /api/tickets/stats returns aggregate metrics")
    void testGetStatsEndpoint_Success() throws Exception {
        QueueTicket t1 = new QueueTicket("QL-0030", "User A", "Desk");
        t1.setStatus(TicketStatus.WAITING);
        QueueTicket t2 = new QueueTicket("QL-0031", "User B", "Desk");
        t2.setStatus(TicketStatus.SERVING);
        ticketRepository.save(t1);
        ticketRepository.save(t2);

        mockMvc.perform(get("/api/tickets/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.waitingCount", is(1)))
                .andExpect(jsonPath("$.data.servingCount", is(1)))
                .andExpect(jsonPath("$.data.totalCount", is(2)));
    }

    @Test
    @DisplayName("Integration Test 9: POST /api/tickets/call-next calls waiting ticket immediately before estimated wait expires")
    void testCallNextEndpoint_BeforeEstimatedWaitExpires_Success() throws Exception {
        QueueTicket freshTicket = new QueueTicket("QL-0035", "Tony Stark", "Tech Support");
        freshTicket.setStatus(TicketStatus.WAITING);
        ticketRepository.save(freshTicket);

        CallNextRequest callRequest = new CallNextRequest("Counter 1");

        mockMvc.perform(post("/api/tickets/call-next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.ticketNumber", is("QL-0035")))
                .andExpect(jsonPath("$.data.status", is("SERVING")))
                .andExpect(jsonPath("$.data.queuePosition", is(0)))
                .andExpect(jsonPath("$.data.estimatedWaitMinutes", is(0)));
    }

    @Test
    @DisplayName("Integration Test 10: POST /api/tickets/{ticketNumber}/complete completes ticket immediately before original estimate expires")
    void testCompleteTicketEndpoint_BeforeOriginalEstimatedWaitExpires_Success() throws Exception {
        QueueTicket servingTicket = new QueueTicket("QL-0036", "Steve Rogers", "Leadership");
        servingTicket.setStatus(TicketStatus.SERVING);
        servingTicket.setCounterName("Desk 4");
        ticketRepository.save(servingTicket);

        mockMvc.perform(post("/api/tickets/QL-0036/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.ticketNumber", is("QL-0036")))
                .andExpect(jsonPath("$.data.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("Integration Test 11: POST /api/tickets/{ticketNumber}/complete rejects completing a WAITING ticket directly")
    void testCompleteTicketEndpoint_WaitingTicketDirectly_FailsWithBadRequest() throws Exception {
        QueueTicket waitingTicket = new QueueTicket("QL-0037", "Wanda Maximoff", "Magic Desk");
        waitingTicket.setStatus(TicketStatus.WAITING);
        ticketRepository.save(waitingTicket);

        mockMvc.perform(post("/api/tickets/QL-0037/complete"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Invalid State Transition")))
                .andExpect(jsonPath("$.message", containsString("cannot be completed before being called to a service counter")));
    }

    @Test
    @DisplayName("Integration Test 12: POST /api/tickets/{ticketNumber}/cancel cancels waiting ticket successfully")
    void testCancelTicketEndpoint_WaitingTicket_Success() throws Exception {
        QueueTicket waitingTicket = new QueueTicket("QL-0038", "Peter Parker", "Photo Desk");
        waitingTicket.setStatus(TicketStatus.WAITING);
        ticketRepository.save(waitingTicket);

        mockMvc.perform(post("/api/tickets/QL-0038/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.ticketNumber", is("QL-0038")))
                .andExpect(jsonPath("$.data.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("Integration Test 13: POST /api/tickets/call-next preserves strict FIFO dispatch ordering")
    void testCallNextEndpoint_FifoOrderPreserved() throws Exception {
        QueueTicket t1 = new QueueTicket("QL-0041", "First Customer", "General");
        t1.setStatus(TicketStatus.WAITING);
        t1.setCreatedAt(java.time.LocalDateTime.now().minusMinutes(10));
        ticketRepository.save(t1);

        QueueTicket t2 = new QueueTicket("QL-0042", "Second Customer", "General");
        t2.setStatus(TicketStatus.WAITING);
        t2.setCreatedAt(java.time.LocalDateTime.now().minusMinutes(3));
        ticketRepository.save(t2);

        CallNextRequest callReq = new CallNextRequest("Counter 2");

        mockMvc.perform(post("/api/tickets/call-next")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(callReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.ticketNumber", is("QL-0041")))
                .andExpect(jsonPath("$.data.status", is("SERVING")));
    }
}
