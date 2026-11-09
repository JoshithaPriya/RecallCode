
// Processes AI analysis jobs and updates each user's independent revision progress.

package com.recallcode.service;

import com.recallcode.model.*;
import com.recallcode.repository.*;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AgentAnalysisWorker {

    private final AgentAnalysisJobRepository jobRepository;
    private final AttemptRepository attemptRepository;
    private final AgentAssessmentRecordRepository assessmentRepository;
    private final AgentAssessmentService assessmentService;
    private final RevisionService revisionService;

    public AgentAnalysisWorker(
            AgentAnalysisJobRepository jobRepository,
            AttemptRepository attemptRepository,
            AgentAssessmentRecordRepository assessmentRepository,
            AgentAssessmentService assessmentService,
            RevisionService revisionService) {

        this.jobRepository = jobRepository;
        this.attemptRepository = attemptRepository;
        this.assessmentRepository = assessmentRepository;
        this.assessmentService = assessmentService;
        this.revisionService = revisionService;
    }

    @Scheduled(fixedDelay = 5000)
    public void processPendingJobs() {

        List<AgentAnalysisJob> jobs =
                jobRepository.findTop20ByStatusOrderByCreatedAtAsc("PENDING");

        for (AgentAnalysisJob job : jobs) {
            processJob(job);
        }
    }

    private void processJob(AgentAnalysisJob job) {

        job.setStatus("PROCESSING");
        job.setProcessingStartedAt(LocalDateTime.now());
        jobRepository.save(job);

        try {
            Attempt attempt = attemptRepository
                    .findByIdWithProblemTagsAndUser(job.getAttemptId())
                    .orElseThrow(() ->
                            new IllegalStateException("Attempt not found"));

            if (attempt.getUser() == null) {
                throw new IllegalStateException(
                        "Attempt has no associated user");
            }

            Long userId = attempt.getUser().getId();
            Long problemId = attempt.getProblem().getId();

            var savedAssessment =
                    assessmentRepository.findByAttemptId(job.getAttemptId());

            String recallQuality;

            if (savedAssessment.isPresent()) {
                recallQuality =
                        savedAssessment.get().getRecallQuality();
            } else {
                AgentAssessment assessment =
                        assessmentService.analyseAttempt(attempt);

                AgentAssessmentRecord record =
                        new AgentAssessmentRecord(
                                job.getAttemptId(), assessment);

                assessmentRepository.save(record);
                recallQuality = assessment.getRecallQuality();
            }

            revisionService.completeRevision(
                    userId, problemId, recallQuality);

            job.setStatus("COMPLETED");
            job.setCompletedAt(LocalDateTime.now());
            job.setProcessingStartedAt(null);
            job.setLastError(null);

        } catch (Exception exception) {

            job.incrementRetryCount();

            job.setLastError(
                    exception.getMessage() == null
                            ? "Unknown analysis error"
                            : exception.getMessage());

            job.setProcessingStartedAt(null);

            job.setStatus(job.getRetryCount() >= 3
                    ? "FAILED"
                    : "PENDING");
        }

        jobRepository.save(job);
    }

    @Scheduled(fixedDelay = 60000)
    public void recoverStuckJobs() {

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);

        List<AgentAnalysisJob> stuckJobs =
                jobRepository.findByStatusAndProcessingStartedAtBefore(
                        "PROCESSING", cutoff);

        for (AgentAnalysisJob job : stuckJobs) {
            job.setStatus("PENDING");
            job.setProcessingStartedAt(null);
            job.setLastError("Recovered after processing timed out.");
            jobRepository.save(job);
        }
    }
}
