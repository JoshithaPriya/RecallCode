
package com.recallcode.service;

// Fetches real metadata for a single LeetCode problem URL and saves it to PostgreSQL.
// This service does not call the AI model.
import tools.jackson.databind.JsonNode;
import com.recallcode.model.Problem;
import com.recallcode.repository.ProblemRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LeetCodeMetadataService {

    private static final String GRAPHQL_URL =
            "https://leetcode.com/graphql";

    private static final String QUERY = """
        query getProblemDetails($titleSlug: String!) {
          question(titleSlug: $titleSlug) {
            questionFrontendId
            title
            titleSlug
            difficulty
            content
            topicTags {
              name
              slug
            }
            hints
            exampleTestcases
            codeSnippets {
              lang
              langSlug
              code
            }
            similarQuestions
          }
        }
        """;

    private final RestClient restClient;
    private final ProblemRepository problemRepository;

    public LeetCodeMetadataService(
            RestClient.Builder restClientBuilder,
            ProblemRepository problemRepository) {
        this.restClient = restClientBuilder.build();
        this.problemRepository = problemRepository;
    }

    public Problem collectProblem(String submittedUrl) {

        String slug = extractSlug(submittedUrl);
        String canonicalUrl =
                "https://leetcode.com/problems/" + slug + "/";

        Map<String, Object> requestBody = Map.of(
                "query", QUERY,
                "variables", Map.of("titleSlug", slug)
        );

        JsonNode response = restClient.post()
                .uri(GRAPHQL_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException(
                    "LeetCode returned an empty response.");
        }

        if (response.path("errors").isArray()
                && !response.path("errors").isEmpty()) {
            throw new IllegalStateException(
                    "LeetCode GraphQL returned an error.");
        }

        JsonNode question = response.path("data").path("question");

        if (question.isMissingNode()
                || question.isNull()
                || question.path("title").asText("").isBlank()) {
            throw new IllegalArgumentException(
                    "Problem not found. Check the URL.");
        }

        Optional<Problem> existing =
        problemRepository.findFirstByPlatformAndExternalId(
                "LeetCode",
                question.path("questionFrontendId").asText(""));

        Problem problem = existing.orElseGet(Problem::new);

        problem.setTitle(question.path("title").asText());
        problem.setPlatform("LeetCode");
        problem.setProblemUrl(canonicalUrl);
        problem.setExternalId(
                question.path("questionFrontendId").asText(""));
        problem.setDifficulty(
                question.path("difficulty").asText(""));
        problem.setDescription(
                question.path("content").asText(""));
        
                // Store extra metadata locally. None of this is sent to the AI here.

// Collect hints.
JsonNode hintsNode = question.path("hints");
StringBuilder hintsBuilder = new StringBuilder();

if (hintsNode.isArray()) {
    for (JsonNode hint : hintsNode) {
        String hintText = hint.asText("");

        if (!hintText.isBlank()) {
            if (!hintsBuilder.isEmpty()) {
                hintsBuilder.append("\n\n");
            }
            hintsBuilder.append(hintText);
        }
    }
} else if (hintsNode.isTextual()) {
    hintsBuilder.append(hintsNode.asText());
}

problem.setHintsText(hintsBuilder.toString());

// Store example test cases.
problem.setExampleTestcases(
        question.path("exampleTestcases").asText(""));

// Collect starter code for the available languages.
JsonNode snippetsNode = question.path("codeSnippets");
StringBuilder snippetsBuilder = new StringBuilder();

if (snippetsNode.isArray()) {
    for (JsonNode snippet : snippetsNode) {
        String language = snippet.path("lang").asText("");
        String languageSlug = snippet.path("langSlug").asText("");
        String code = snippet.path("code").asText("");

        if (!code.isBlank()) {
            snippetsBuilder
                    .append(language)
                    .append(" (")
                    .append(languageSlug)
                    .append(")\n")
                    .append(code)
                    .append("\n\n");
        }
    }
}

problem.setStarterCodeText(snippetsBuilder.toString());

// Store similar-question information as returned by LeetCode.
JsonNode similarNode = question.path("similarQuestions");

String similarQuestions = "";

if (!similarNode.isMissingNode() && !similarNode.isNull()) {
    similarQuestions = similarNode.isTextual()
            ? similarNode.asText()
            : similarNode.toString();
}

problem.setSimilarQuestionsJson(similarQuestions);

        Set<String> tags = new HashSet<>();
        JsonNode topicTags = question.path("topicTags");

        if (topicTags.isArray()) {
            for (JsonNode tag : topicTags) {
                String name = tag.path("name").asText("");
                if (!name.isBlank()) {
                    tags.add(name);
                }
            }
        }

        problem.setTags(tags);
        problem.setMetadataSource("LeetCode GraphQL");
        problem.setMetadataStatus("FETCHED");
        problem.setMetadataFetchedAt(LocalDateTime.now());

        return problemRepository.save(problem);
    }

    // Extracts the problem slug and rejects URLs from other websites.
    private String extractSlug(String submittedUrl) {

        if (submittedUrl == null || submittedUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a LeetCode problem URL.");
        }

        try {
            URI uri = URI.create(submittedUrl.trim());
            String host = uri.getHost();

            if (host == null
                    || !(host.equalsIgnoreCase("leetcode.com")
                    || host.equalsIgnoreCase("www.leetcode.com"))) {
                throw new IllegalArgumentException(
                        "Only LeetCode URLs are supported.");
            }

            Pattern pattern = Pattern.compile(
                    "^/problems/([a-zA-Z0-9-]+)/?$");

            Matcher matcher = pattern.matcher(
                    uri.getPath() == null ? "" : uri.getPath());

            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        "Enter a valid LeetCode problem URL.");
            }

            return matcher.group(1).toLowerCase();

        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid LeetCode URL.");
        }
    }
}
