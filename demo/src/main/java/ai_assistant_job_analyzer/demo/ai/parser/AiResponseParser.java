package ai_assistant_job_analyzer.demo.ai.parser;

import ai_assistant_job_analyzer.demo.ai.model.EmailDraft;
import ai_assistant_job_analyzer.demo.dto.JobDetailsDto;
import ai_assistant_job_analyzer.demo.dto.MatchAnalysisDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public JobDetailsDto parseJobDetails(String jsonResponse) {
        try {
            JsonNode node = objectMapper.readTree(cleanJsonResponse(jsonResponse));

            return JobDetailsDto.builder()
                    .companyName(textOrNull(node, "companyName"))
                    .jobTitle(textOrNull(node, "jobTitle"))
                    .requiredExperience(textOrNull(node, "requiredExperience"))
                    .requiredSkills(jsonArrayToList(node, "requiredSkills"))
                    .location(textOrNull(node, "location"))
                    .recruiterEmail(textOrNull(node, "recruiterEmail"))
                    .applicationInstructions(textOrNull(node, "applicationInstructions"))
                    .highlightedKeywords(jsonArrayToList(node, "highlightedKeywords"))
                    .rawExtractedText(textOrNull(node, "rawExtractedText"))
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse job details JSON: {}", jsonResponse, e);
            throw new RuntimeException("Failed to parse AI response for job details");
        }
    }

    public EmailDraft parseEmailDraft(String jsonResponse) {
        try {
            JsonNode node = objectMapper.readTree(cleanJsonResponse(jsonResponse));
            return new EmailDraft(
                    textOrNull(node, "subject"),
                    textOrNull(node, "body")
            );
        } catch (Exception e) {
            log.error("Failed to parse email draft JSON", e);
            throw new RuntimeException("Failed to parse generated email");
        }
    }

    public MatchAnalysisDto parseMatchAnalysis(String jsonResponse) {
        try {
            JsonNode node = objectMapper.readTree(cleanJsonResponse(jsonResponse));
            return MatchAnalysisDto.builder()
                    .matchScore(node.path("matchScore").asInt(0))
                    .matchedSkills(jsonArrayToList(node, "matchedSkills"))
                    .missingSkills(jsonArrayToList(node, "missingSkills"))
                    .highlightedKeywords(jsonArrayToList(node, "highlightedKeywords"))
                    .summary(textOrNull(node, "summary"))
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse match analysis JSON", e);
            throw new RuntimeException("Failed to parse match analysis");
        }
    }

    private String cleanJsonResponse(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private List<String> jsonArrayToList(JsonNode node, String field) {
        List<String> list = new ArrayList<>();
        if (node.has(field) && node.get(field).isArray()) {
            node.get(field).forEach(item -> list.add(item.asText()));
        }
        return list;
    }
}
