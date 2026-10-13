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
    public CommandLineRunner initDatabase(QueueTicketRepository repository,
                                          com.queueless.repository.ReleaseHistoryRepository releaseRepository) {
        return args -> {
            // Seed baseline queue tickets
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

            // Seed initial recorded release history using actual Git commit metadata
            if (releaseRepository.count() == 0) {
                com.queueless.model.ReleaseHistory baseline = new com.queueless.model.ReleaseHistory(
                        "1.1.0",
                        "c038603f4a6de3b89e05920791c4b95f745f434e",
                        "c038603",
                        "main",
                        "feat: release v1.1.0 with CI/CD status badge and automated deployment footer",
                        "1",
                        "http://localhost:8080/job/QueueLess-CICD/1/",
                        "PASSED (17/17 tests)",
                        "VERIFIED",
                        "production",
                        "queueless:1.1.0-c038603",
                        "QueueLess-CICD",
                        "QueueLess-CICD-1"
                );
                baseline.setCurrent(true);
                baseline.setDeployedAt(LocalDateTime.now().minusHours(2));
                releaseRepository.save(baseline);
            }
        };
    }
}
