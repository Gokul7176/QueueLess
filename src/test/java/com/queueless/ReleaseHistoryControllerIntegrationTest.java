package com.queueless;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queueless.dto.ReleaseRecordRequest;
import com.queueless.model.ReleaseHistory;
import com.queueless.repository.ReleaseHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReleaseHistoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReleaseHistoryRepository releaseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTH_TOKEN = "test-secret-token";

    @BeforeEach
    void cleanUp() {
        releaseRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Test 1: GET /api/version-history returns list of recorded releases")
    void testGetAllReleases_Success() throws Exception {
        ReleaseHistory release = new ReleaseHistory(
                "1.1.0",
                "c038603f4a6de3b89e05920791c4b95f745f434e",
                "c038603",
                "main",
                "feat: release v1.1.0",
                "14",
                "http://localhost:8080/job/QueueLess-CICD/14/",
                "PASSED (17/17 tests)",
                "VERIFIED",
                "production",
                "queueless:1.1.0-c038603",
                "QueueLess-CICD",
                "QueueLess-CICD-14"
        );
        release.setCurrent(true);
        releaseRepository.save(release);

        mockMvc.perform(get("/api/version-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].version", is("1.1.0")))
                .andExpect(jsonPath("$.data[0].gitCommitShort", is("c038603")))
                .andExpect(jsonPath("$.data[0].deploymentStatus", is("VERIFIED")));
    }

    @Test
    @DisplayName("Integration Test 2: GET /api/version-history/current returns current active release")
    void testGetCurrentVersion_Success() throws Exception {
        ReleaseHistory release = new ReleaseHistory(
                "1.1.0",
                "c038603f4a6de3b89e05920791c4b95f745f434e",
                "c038603",
                "main",
                "feat: release v1.1.0",
                "14",
                "http://localhost:8080/job/QueueLess-CICD/14/",
                "PASSED",
                "VERIFIED",
                "production",
                "queueless:1.1.0-c038603",
                "QueueLess-CICD",
                "QueueLess-CICD-14"
        );
        release.setCurrent(true);
        releaseRepository.save(release);

        mockMvc.perform(get("/api/version-history/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.version", is("1.1.0")))
                .andExpect(jsonPath("$.data.gitCommitShort", is("c038603")))
                .andExpect(jsonPath("$.data.deployed", is(true)));
    }

    @Test
    @DisplayName("Integration Test 3: GET /api/version-history/{deploymentId} retrieves release record by deployment ID")
    void testGetReleaseByDeploymentId_Success() throws Exception {
        ReleaseHistory release = new ReleaseHistory(
                "1.1.0",
                "c038603f4a6de3b89e05920791c4b95f745f434e",
                "c038603",
                "main",
                "feat: release v1.1.0",
                "20",
                "http://localhost:8080/job/QueueLess-CICD/20/",
                "PASSED",
                "VERIFIED",
                "production",
                "queueless:1.1.0-c038603",
                "QueueLess-CICD",
                "QueueLess-CICD-20"
        );
        releaseRepository.save(release);

        mockMvc.perform(get("/api/version-history/QueueLess-CICD-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.deploymentId", is("QueueLess-CICD-20")));
    }

    @Test
    @DisplayName("Integration Test 4: GET /api/version-history/{deploymentId} with non-existent ID returns 404")
    void testGetReleaseByDeploymentId_NotFound() throws Exception {
        mockMvc.perform(get("/api/version-history/UNKNOWN-JOB-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Integration Test 5: POST /api/version-history with valid token records new release")
    void testRecordRelease_WithValidToken_Success() throws Exception {
        ReleaseRecordRequest request = new ReleaseRecordRequest(
                "1.2.0",
                "4bd1506dadc2f32aa047920d35bedd007a43e97c",
                "4bd1506",
                "main",
                "ci: include Docker Desktop bin in PATH",
                "25",
                "http://localhost:8080/job/QueueLess-CICD/25/",
                "PASSED (17/17 tests)",
                "VERIFIED",
                "production",
                "queueless:1.2.0-4bd1506",
                "QueueLess-CICD",
                "QueueLess-CICD-25"
        );

        mockMvc.perform(post("/api/version-history")
                        .header("X-Release-Token", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.version", is("1.2.0")))
                .andExpect(jsonPath("$.data.gitCommitShort", is("4bd1506")))
                .andExpect(jsonPath("$.data.deploymentId", is("QueueLess-CICD-25")))
                .andExpect(jsonPath("$.data.current", is(true)));
    }

    @Test
    @DisplayName("Integration Test 6: POST /api/version-history without token returns 401 Unauthorized")
    void testRecordRelease_MissingToken_Returns401() throws Exception {
        ReleaseRecordRequest request = new ReleaseRecordRequest(
                "1.2.0",
                "4bd1506dadc2f32aa047920d35bedd007a43e97c",
                "4bd1506",
                "main",
                "ci: commit",
                "26",
                "http://localhost:8080/job/QueueLess-CICD/26/",
                "PASSED",
                "VERIFIED",
                "production",
                "queueless:1.2.0",
                "QueueLess-CICD",
                "QueueLess-CICD-26"
        );

        mockMvc.perform(post("/api/version-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Integration Test 7: POST /api/version-history with blank version returns 400 Bad Request")
    void testRecordRelease_ValidationError_BlankVersion() throws Exception {
        ReleaseRecordRequest request = new ReleaseRecordRequest();
        request.setGitCommit("c038603");
        // version is blank

        mockMvc.perform(post("/api/version-history")
                        .header("X-Release-Token", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Error")));
    }

    @Test
    @DisplayName("Integration Test 8: POST /api/version-history idempotency - repeated submissions update existing record")
    void testRecordRelease_Idempotency() throws Exception {
        ReleaseRecordRequest request1 = new ReleaseRecordRequest(
                "1.1.0",
                "c038603f4a6de3b89e05920791c4b95f745f434e",
                "c038603",
                "main",
                "Initial deployment attempt",
                "30",
                "http://localhost:8080/job/QueueLess-CICD/30/",
                "RUNNING",
                "DEPLOYING",
                "production",
                "queueless:1.1.0-c038603",
                "QueueLess-CICD",
                "QueueLess-CICD-30"
        );

        mockMvc.perform(post("/api/version-history")
                        .header("X-Release-Token", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.deploymentStatus", is("DEPLOYING")));

        // Submit updated verification status for the exact same job & build
        ReleaseRecordRequest request2 = new ReleaseRecordRequest(
                "1.1.0",
                "c038603f4a6de3b89e05920791c4b95f745f434e",
                "c038603",
                "main",
                "Deployment verified successfully",
                "30",
                "http://localhost:8080/job/QueueLess-CICD/30/",
                "PASSED (17/17 tests)",
                "VERIFIED",
                "production",
                "queueless:1.1.0-c038603",
                "QueueLess-CICD",
                "QueueLess-CICD-30"
        );

        mockMvc.perform(post("/api/version-history")
                        .header("X-Release-Token", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.deploymentStatus", is("VERIFIED")));

        // Ensure database only contains 1 record with this deployment ID
        assertEquals(1, releaseRepository.count());
    }
}
