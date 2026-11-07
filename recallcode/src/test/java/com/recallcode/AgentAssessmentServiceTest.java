
/*
 * Tests compact prompt limits without database access or AI API calls.
 */
package com.recallcode;

import com.recallcode.model.Attempt;
import com.recallcode.model.Problem;
import com.recallcode.repository.AttemptRepository;
import com.recallcode.service.AgentAssessmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentAssessmentServiceTest {

    private AgentAssessmentService service;

    @BeforeEach
    void setUp() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(mock(ChatClient.class));

        service = new AgentAssessmentService(
                builder, mock(AttemptRepository.class));
    }

    @Test
    void serviceCanBeCreated() {
        assertNotNull(service);
    }

    @Test
    void promptLimitsPreviousAttemptsToThree() {
        Problem problem = makeProblem();

        Attempt current = makeAttempt(100L, problem, "current notes");
        Attempt first = makeAttempt(1L, problem, "previous-one");
        Attempt second = makeAttempt(2L, problem, "previous-two");
        Attempt third = makeAttempt(3L, problem, "previous-three");
        Attempt fourth = makeAttempt(4L, problem, "previous-four");

        String prompt = service.buildAssessmentPrompt(
                current, List.of(first, second, third, fourth, current));

        assertTrue(prompt.contains("previous-one"));
        assertTrue(prompt.contains("previous-two"));
        assertTrue(prompt.contains("previous-three"));
        assertFalse(prompt.contains("previous-four"));
    }

    @Test
    void promptLimitsNotesToTwoHundredCharacters() {
        Problem problem = makeProblem();
        String longNotes = "N".repeat(250);

        Attempt current = makeAttempt(100L, problem, longNotes);

        String prompt = service.buildAssessmentPrompt(
                current, List.of(current));

        assertTrue(prompt.contains("N".repeat(200)));
        assertFalse(prompt.contains("N".repeat(201)));
    }

    @Test
    void promptIncludesAtMostFiveTags() {
        Problem problem = makeProblem();
        when(problem.getTags()).thenReturn(
                Set.of("TagA", "TagB", "TagC", "TagD", "TagE", "TagF"));

        Attempt current = makeAttempt(100L, problem, "notes");

        String prompt = service.buildAssessmentPrompt(
                current, List.of(current));

        long includedTags = List.of(
                "TagA", "TagB", "TagC", "TagD", "TagE", "TagF"
        ).stream().filter(prompt::contains).count();

        assertEquals(5, includedTags);
    }

    @Test
    void promptExcludesLargeMetadataFields() {
        Problem problem = makeProblem();
        Attempt current = makeAttempt(100L, problem, "notes");

        String prompt = service.buildAssessmentPrompt(
                current, List.of(current));

        assertFalse(prompt.contains("similarQuestionsJson"));
        assertFalse(prompt.contains("starterCodeText"));
        assertFalse(prompt.contains("exampleTestcases"));
        assertFalse(prompt.contains("hintsText"));
    }

    private Problem makeProblem() {
        Problem problem = mock(Problem.class);
        when(problem.getTitle()).thenReturn("Two Sum");
        when(problem.getDifficulty()).thenReturn("Easy");
        when(problem.getTags()).thenReturn(Set.of("Array", "Hash Table"));
        return problem;
    }

    private Attempt makeAttempt(
            Long id, Problem problem, String notes) {
        Attempt attempt = mock(Attempt.class);
        when(attempt.getId()).thenReturn(id);
        when(attempt.getProblem()).thenReturn(problem);
        when(attempt.getOutcome()).thenReturn("SOLVED");
        when(attempt.getTimeTakenMinutes()).thenReturn(10);
        when(attempt.getNotes()).thenReturn(notes);
        return attempt;
    }
}
