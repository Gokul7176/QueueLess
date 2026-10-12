# Experiment 9: Continuous Testing

**Course Code:** 23IT723 — DevOps Laboratory  
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System  

---

## 1. Aim
To design, implement, and execute an automated test suite comprising 17 JUnit 5 test cases for the QueueLess virtual queue application (covering unit tests with Mockito and integration tests with Spring Boot Test and MockMvc), integrate automated testing into the CI/CD pipeline, publish graphical test reports in Jenkins, and demonstrate how a failing test halts pipeline execution before deployment.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 10/11 or Linux
- **Software:** Java JDK 17+, Apache Maven 3.9+, Jenkins 2.400+, Git
- **Libraries:** JUnit Jupiter 5.x, Mockito 5.x, Spring Boot Starter Test (v3.3.4), AssertJ, Hamcrest
- **Source Paths:** `src/test/java/com/queueless/`

---

## 3. Theory

### What is Continuous Testing?
Continuous Testing is the practice of executing automated tests as part of the software delivery pipeline to obtain immediate feedback on the business risks associated with a software release candidate. In modern DevOps, testing is not a deferred phase performed manually before release; it is an automated quality gate executed on every commit.

### Testing Pyramid in QueueLess:

```
           ▲
          / \
         /   \      E2E / Browser Tests (Browser Subagent / UI Verification)
        /─────\
       /       \    Integration Tests (QueueControllerIntegrationTest / MockMvc)
      /─────────\   - REST API Endpoints, HTTP Statuses, Validation, JSON Serialization
     /           \
    /─────────────\ Unit Tests (QueueServiceTest / Mockito)
   /               \- Business Rules, FIFO Ordering, State Transitions, Edge Cases
  ───────────────────
```

### Key Frameworks Used:
1. **JUnit 5 (Jupiter):** Next-generation testing framework featuring `@Test`, `@DisplayName`, `@BeforeEach`, and `assertThrows()`.
2. **Mockito:** Mocking framework used in `QueueServiceTest` to isolate service business logic from database dependencies.
3. **Spring Boot Test (`@SpringBootTest`, `@AutoConfigureMockMvc`):** Boots the application context and performs mock HTTP requests to verify REST endpoints, response codes (200, 201, 400, 404), and JSON structures.
4. **Maven Surefire Plugin:** Automatically discovers and executes test classes matching `**/*Test.java` and generates standard XML reports in `target/surefire-reports/`.

---

## 4. Comprehensive Test Suite Breakdown (17 Tests)

### Group A: Unit Tests (`src/test/java/com/queueless/QueueServiceTest.java`)

| Test # | Test Method Name | Focus Area / Expected Behavior |
|:---|:---|:---|
| 1 | `testJoinQueue_Success()` | Generates sequential ticket format (`QL-0001`), assigns `WAITING` status, calculates position #1 and 5-min wait. |
| 2 | `testGetTicketStatus_Success()` | Calculates dynamic queue position (#2) and estimated wait time (10 mins) among active waiting tickets. |
| 3 | `testGetTicketStatus_NotFound()` | Throws `TicketNotFoundException` when looking up a nonexistent ticket number (`QL-9999`). |
| 4 | `testCallNextTicket_Success()` | Selects first waiting customer, transitions status to `SERVING`, assigns counter, and clears wait position to 0. |
| 5 | `testCallNextTicket_EmptyQueue()` | Throws `InvalidTicketStateException` when attempting to call next customer while waiting queue is empty. |
| 6 | `testCompleteTicket_Success()` | Transitions `SERVING` ticket to `COMPLETED` and records `completedAt` timestamp. |
| 7 | `testCompleteTicket_WaitingTicket()` | Throws `InvalidTicketStateException` preventing tickets from completing before being called. |
| 8 | `testCompleteTicket_AlreadyCompleted()` | Throws `InvalidTicketStateException` preventing double completion of already completed tickets. |
| 9 | `testGetQueueStats_Success()` | Accurately aggregates counts for waiting, serving, completed, cancelled, and total tickets. |

### Group B: Integration Tests (`src/test/java/com/queueless/QueueControllerIntegrationTest.java`)

| Test # | Test Method Name | Focus Area / Expected Behavior |
|:---|:---|:---|
| 10 | `testJoinQueueEndpoint_Success()` | `POST /api/tickets/join` creates new ticket and returns HTTP 201 Created with JSON envelope. |
| 11 | `testJoinQueueEndpoint_ValidationError()` | `POST /api/tickets/join` with empty customer name returns HTTP 400 Bad Request with validation error message. |
| 12 | `testGetAllTicketsEndpoint_Success()` | `GET /api/tickets` retrieves list of all tickets and returns HTTP 200 OK. |
| 13 | `testGetTicketStatusEndpoint_Success()` | `GET /api/tickets/{ticketNumber}` retrieves ticket details and returns HTTP 200 OK. |
| 14 | `testGetTicketStatusEndpoint_NotFound()` | `GET /api/tickets/QL-NONEXISTENT` returns HTTP 404 Not Found. |
| 15 | `testCallNextEndpoint_Success()` | `POST /api/tickets/call-next` calls next ticket in strict FIFO order and returns HTTP 200 OK. |
| 16 | `testCompleteTicketEndpoint_Success()` | `POST /api/tickets/{ticketNumber}/complete` marks serving ticket completed and returns HTTP 200 OK. |
| 17 | `testGetStatsEndpoint_Success()` | `GET /api/tickets/stats` retrieves aggregated queue metrics and returns HTTP 200 OK. |

---

## 5. Execution Procedures

### Step 1: Run Automated Tests Locally via Maven
```powershell
cd C:\queuless
mvn test
```

### Step 2: Inspect Generated Test Report Files
```powershell
Get-ChildItem -Path "target\surefire-reports\*.txt" | ForEach-Object {
    Write-Host "=== $($_.Name) ===" -ForegroundColor Cyan
    Get-Content $_.FullName | Select-Object -First 10
}
```

### Step 3: Publish JUnit Test Reports in Jenkins
In Jenkins Pipeline (`Jenkinsfile`), test publishing is handled automatically by the `post` block:
```groovy
post {
    always {
        junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
    }
}
```

### Step 4: Demonstrate Pipeline Failure Guard (Quality Gate)
To demonstrate that automated test failures halt the pipeline and prevent broken deployments:

1. Open `src/test/java/com/queueless/QueueServiceTest.java`.
2. Introduce an intentional assertion failure:
   ```java
   // Change:
   assertEquals(TicketStatus.WAITING, createdTicket.getStatus());
   // To intentional failure:
   assertEquals(TicketStatus.COMPLETED, createdTicket.getStatus());
   ```
3. Commit and push the code:
   ```powershell
   git commit -am "test: introduce intentional test failure for lab demonstration"
   git push origin main
   ```
4. Observe the Jenkins Pipeline execution:
   - Stage 1 (Checkout): **SUCCESS (Green)**
   - Stage 2 (Compile): **SUCCESS (Green)**
   - Stage 3 (Run Automated Tests): **FAILED (Red)**
   - Stages 4 (Package), 5 (Docker Build), 6 (Deploy), 7 (Verify): **ABORTED / SKIPPED**
5. Restore the correct test assertion and verify that the pipeline returns to all-green status.

---

## 6. Actual Verification Method

1. Check terminal output of `mvn test` confirming:
   ```
   [INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
   [INFO] BUILD SUCCESS
   ```
2. Verify XML report files in `target/surefire-reports/`:
   - `TEST-com.queueless.QueueServiceTest.xml`
   - `TEST-com.queueless.QueueControllerIntegrationTest.xml`
3. In Jenkins, open the job build page and click **Test Result**. Confirm that all 17 tests are listed with zero regressions.

---

## 7. Expected Result vs. Observed Result

- **Expected Result:**  
  All 17 tests execute in under 10 seconds. Surefire generates XML reports. When all tests pass, the build succeeds and deployment proceeds. When any test fails, Surefire exits with code 1, and the CI/CD pipeline immediately halts.

- **Observed Result (Actual Laboratory Execution):**  
  ```
  [INFO] Running com.queueless.QueueControllerIntegrationTest
  [INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.420 s
  [INFO] Running com.queueless.QueueServiceTest
  [INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.446 s
  [INFO] 
  [INFO] Results:
  [INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS
  ```

---

## 8. Screenshot Checklist

- [ ] `exp09_01_mvn_test_success.png`: Terminal output of `mvn test` showing all 17 tests passing with `BUILD SUCCESS`.
- [ ] `exp09_02_surefire_xml_files.png`: Directory listing of `target/surefire-reports/` showing generated XML test reports.
- [ ] `exp09_03_jenkins_test_trend.png`: Jenkins Test Result Trend graph showing 17 passing tests.
- [ ] `exp09_04_jenkins_failed_test_pipeline.png`: Jenkins Pipeline Stage View showing Stage 3 in red and Deploy stage skipped.
- [ ] `exp09_05_jenkins_test_failure_details.png`: Jenkins Test Failure report displaying the exact stack trace and failed assertion line.

---

## 9. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `No tests were executed!` | Test class or method names do not conform to Surefire naming conventions (`*Test.java`). | Ensure test classes end with `Test` (e.g., `QueueServiceTest.java`) and methods are annotated with `@Test`. |
| `BeanCreationException: Error creating bean with name 'dataSource'` | H2 database dependency missing or conflicting port. | Ensure `com.h2database:h2` is in `pom.xml` with runtime scope. |
| `MethodArgumentNotValidException not returning 400` | `@Valid` annotation omitted on controller request parameter. | Add `@Valid` to `@RequestBody JoinQueueRequest request` in `QueueController`. |
| Tests fail with `Address already in use` | Another Spring Boot instance is holding port 8085 during `@SpringBootTest`. | Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` or ensure local server is stopped prior to testing. |

---

## 10. Student Experiment Result Record

**Date of Execution:** ___________________  
**Student Name:** _______________________  
**Roll / Register No:** _________________  
**Staff Signature:** ____________________  

**Result:**  
A comprehensive automated test suite consisting of 17 JUnit 5 unit and integration tests was implemented, executed via Maven, and integrated into the Jenkins CI/CD pipeline. Test reporting, pass/fail trends, and deployment gating via automated quality gates were successfully verified.

---

## 11. Viva Questions and Answers

1. **What is the difference between a Unit Test and an Integration Test?**  
   *Answer:* A unit test tests a single class or method in complete isolation, replacing all external dependencies (databases, networks, services) with mocks or stubs. An integration test verifies that multiple software modules (e.g., controllers, services, repositories, and the database) cooperate and communicate correctly within the Spring application context.

2. **What does the `@AutoConfigureMockMvc` annotation do in Spring Boot testing?**  
   *Answer:* It configures and provides a `MockMvc` instance that simulates HTTP requests and responses against Spring controllers without starting a real HTTP network server, enabling fast, end-to-end REST endpoint testing.

3. **What is Mockito used for in `QueueServiceTest`?**  
   *Answer:* Mockito mocks the `QueueTicketRepository` interface. It allows developers to specify predefined return values (`when(...).thenReturn(...)`) and verify method invocations (`verify(...)`), isolating the business logic in `QueueServiceImpl` from actual database read/write operations.

4. **What is an "Automated Quality Gate" in a CI/CD pipeline?**  
   *Answer:* A quality gate is an automated check (such as unit test execution, code coverage threshold, or security vulnerability scan) embedded in the pipeline. If the software fails to meet the predefined criteria, the pipeline halts immediately, preventing defective code from reaching downstream deployment environments.

5. **Where does Maven store JUnit XML reports, and why is this format important?**  
   *Answer:* Maven stores XML reports in `target/surefire-reports/`. The XML format conforms to the standardized JUnit XML schema, allowing CI/CD servers (Jenkins, GitLab CI, GitHub Actions) to parse the results, calculate pass/fail ratios, display historical trends, and flag regressions.
