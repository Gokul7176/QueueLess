package com.queueless;

import com.queueless.dto.CurrentVersionDTO;
import com.queueless.dto.ReleaseRecordRequest;
import com.queueless.model.ReleaseHistory;
import com.queueless.repository.ReleaseHistoryRepository;
import com.queueless.service.ReleaseHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseHistoryServiceTest {

    @Mock
    private ReleaseHistoryRepository releaseRepository;

    @InjectMocks
    private ReleaseHistoryServiceImpl releaseHistoryService;

    private ReleaseRecordRequest sampleRequest;
    private ReleaseHistory existingRelease;

    @BeforeEach
    void setUp() {
        sampleRequest = new ReleaseRecordRequest(
                "1.1.0",
                "c038603f4a6de3b89e05920791c4b95f745f434e",
                "c038603",
                "main",
                "feat: release v1.1.0 with CI/CD status badge and automated deployment footer",
                "14",
                "http://localhost:8080/job/QueueLess-CICD/14/",
                "PASSED (17/17 tests)",
                "VERIFIED",
                "production",
                "queueless:1.1.0-c038603",
                "QueueLess-CICD",
                "QueueLess-CICD-14"
        );

        existingRelease = new ReleaseHistory(
                "1.0.0",
                "0ad2b04615f767f64f83de042c369ab8cb4d0629",
                "0ad2b04",
                "main",
                "feat: initial commit",
                "1",
                "http://localhost:8080/job/QueueLess-CICD/1/",
                "PASSED (17/17 tests)",
                "VERIFIED",
                "production",
                "queueless:1.0.0",
                "QueueLess-CICD",
                "QueueLess-CICD-1"
        );
        existingRelease.setCurrent(true);
    }

    @Test
    @DisplayName("Unit Test 1: Record new verified release sets it as current and unsets previous")
    void testRecordRelease_NewVerifiedRelease_SetsAsCurrent() {
        when(releaseRepository.findByDeploymentId("QueueLess-CICD-14")).thenReturn(Optional.empty());
        when(releaseRepository.findAll()).thenReturn(Collections.singletonList(existingRelease));
        when(releaseRepository.save(any(ReleaseHistory.class))).thenAnswer(i -> i.getArgument(0));

        ReleaseHistory recorded = releaseHistoryService.recordRelease(sampleRequest);

        assertNotNull(recorded);
        assertEquals("1.1.0", recorded.getVersion());
        assertEquals("c038603", recorded.getGitCommitShort());
        assertEquals("VERIFIED", recorded.getDeploymentStatus());
        assertTrue(recorded.isCurrent(), "New verified release must be flagged as current");
        assertFalse(existingRelease.isCurrent(), "Previous release must have current flag unset");
        verify(releaseRepository, atLeastOnce()).save(any(ReleaseHistory.class));
    }

    @Test
    @DisplayName("Unit Test 2: Idempotency - Submitting identical deployment ID updates existing record without duplicates")
    void testRecordRelease_IdempotentUpdate() {
        ReleaseHistory existingDeployment = new ReleaseHistory(
                "1.1.0",
                "c038603f4a6de3b89e05920791c4b95f745f434e",
                "c038603",
                "main",
                "Initial message",
                "14",
                "http://localhost:8080/job/QueueLess-CICD/14/",
                "PASSED",
                "DEPLOYED",
                "production",
                "queueless:1.1.0-c038603",
                "QueueLess-CICD",
                "QueueLess-CICD-14"
        );

        when(releaseRepository.findByDeploymentId("QueueLess-CICD-14")).thenReturn(Optional.of(existingDeployment));
        when(releaseRepository.findAll()).thenReturn(Collections.singletonList(existingDeployment));
        when(releaseRepository.save(any(ReleaseHistory.class))).thenAnswer(i -> i.getArgument(0));

        // Submit updated test results for the same deployment
        sampleRequest.setTestResult("PASSED (All 17 tests passed)");
        ReleaseHistory updated = releaseHistoryService.recordRelease(sampleRequest);

        assertNotNull(updated);
        assertEquals("QueueLess-CICD-14", updated.getDeploymentId());
        assertEquals("PASSED (All 17 tests passed)", updated.getTestResult());
        verify(releaseRepository).findByDeploymentId("QueueLess-CICD-14");
    }

    @Test
    @DisplayName("Unit Test 3: Failed release build does not become current active release")
    void testRecordRelease_FailedBuild_DoesNotBecomeCurrent() {
        sampleRequest.setDeploymentStatus("FAILED");
        sampleRequest.setTestResult("FAILED (1 test broken)");
        sampleRequest.setDeploymentId("QueueLess-CICD-15");

        when(releaseRepository.findByDeploymentId("QueueLess-CICD-15")).thenReturn(Optional.empty());
        when(releaseRepository.save(any(ReleaseHistory.class))).thenAnswer(i -> i.getArgument(0));

        ReleaseHistory recorded = releaseHistoryService.recordRelease(sampleRequest);

        assertNotNull(recorded);
        assertEquals("FAILED", recorded.getDeploymentStatus());
        assertFalse(recorded.isCurrent(), "Failed release must NEVER be flagged as current active release");
    }

    @Test
    @DisplayName("Unit Test 4: Retrieve all releases filtered by status")
    void testGetAllReleases_WithFilter() {
        when(releaseRepository.findByDeploymentStatusIgnoreCaseOrderByDeployedAtDesc("VERIFIED"))
                .thenReturn(Collections.singletonList(existingRelease));

        List<ReleaseHistory> results = releaseHistoryService.getAllReleases("VERIFIED");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("VERIFIED", results.get(0).getDeploymentStatus());
        verify(releaseRepository).findByDeploymentStatusIgnoreCaseOrderByDeployedAtDesc("VERIFIED");
    }

    @Test
    @DisplayName("Unit Test 5: Get current active release returns accurate DTO")
    void testGetCurrentRelease_Found() {
        when(releaseRepository.findFirstByIsCurrentTrueOrderByDeployedAtDesc())
                .thenReturn(Optional.of(existingRelease));

        CurrentVersionDTO current = releaseHistoryService.getCurrentRelease();

        assertNotNull(current);
        assertEquals("1.0.0", current.getVersion());
        assertEquals("0ad2b04", current.getGitCommitShort());
        assertTrue(current.isDeployed());
    }

    @Test
    @DisplayName("Unit Test 6: Get current release falls back to baseline when database is empty")
    void testGetCurrentRelease_EmptyDatabase_ReturnsBaseline() {
        when(releaseRepository.findFirstByIsCurrentTrueOrderByDeployedAtDesc()).thenReturn(Optional.empty());
        when(releaseRepository.findFirstByOrderByDeployedAtDesc()).thenReturn(Optional.empty());

        CurrentVersionDTO current = releaseHistoryService.getCurrentRelease();

        assertNotNull(current);
        assertEquals("1.1.0", current.getVersion());
        assertEquals("c038603", current.getGitCommitShort());
        assertEquals("main", current.getGitBranch());
        assertTrue(current.isDeployed());
    }
}
