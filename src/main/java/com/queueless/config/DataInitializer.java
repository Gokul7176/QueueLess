package com.queueless.config;

import com.queueless.model.QueueTicket;
import com.queueless.model.TicketStatus;
import com.queueless.repository.QueueTicketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(QueueTicketRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                // Seed sample completed ticket
                QueueTicket t1 = new QueueTicket("QL-0001", "Diana Prince", "Account Services");
                t1.setStatus(TicketStatus.COMPLETED);
                t1.setCounterName("Counter 2");
                t1.setCreatedAt(LocalDateTime.now().minusMinutes(25));
                t1.setServedAt(LocalDateTime.now().minusMinutes(15));
                t1.setCompletedAt(LocalDateTime.now().minusMinutes(5));
                repository.save(t1);

                // Seed currently serving ticket
                QueueTicket t2 = new QueueTicket("QL-0002", "Alice Smith", "General Inquiry");
                t2.setStatus(TicketStatus.SERVING);
                t2.setCounterName("Counter 1");
                t2.setCreatedAt(LocalDateTime.now().minusMinutes(20));
                t2.setServedAt(LocalDateTime.now().minusMinutes(4));
                repository.save(t2);

                // Seed waiting tickets
                QueueTicket t3 = new QueueTicket("QL-0003", "Bob Johnson", "Technical Support");
                t3.setStatus(TicketStatus.WAITING);
                t3.setCreatedAt(LocalDateTime.now().minusMinutes(12));
                repository.save(t3);

                QueueTicket t4 = new QueueTicket("QL-0004", "Charlie Davis", "Billing & Payments");
                t4.setStatus(TicketStatus.WAITING);
                t4.setCreatedAt(LocalDateTime.now().minusMinutes(6));
                repository.save(t4);
            }
        };
    }
}
