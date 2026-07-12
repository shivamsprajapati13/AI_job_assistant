package ai_assistant_job_analyzer.ai.openai;

import ai_assistant_job_analyzer.ai.AiProviderStrategy;
import ai_assistant_job_analyzer.ai.AiProviderType;
import ai_assistant_job_analyzer.ai.model.EmailDraft;
import ai_assistant_job_analyzer.ai.parser.AiResponseParser;
import ai_assistant_job_analyzer.ai.prompt.AiPrompts;
import ai_assistant_job_analyzer.config.AiProviderProperties;
import ai_assistant_job_analyzer.dto.JobDetailsDto;
import ai_assistant_job_analyzer.dto.MatchAnalysisDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "ai.providers.openai", name = "enabled", havingValue = "true", matchIfMissing = false)
public class OpenAiProviderStrategy implements AiProviderStrategy {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AiResponseParser responseParser;
    private final AiProviderProperties.ProviderConfig config;

    public OpenAiProviderStrategy(AiProviderProperties properties,
                                  ObjectMapper objectMapper,
                                  AiResponseParser responseParser) {
        this.objectMapper = objectMapper;
        this.responseParser = responseParser;
        this.config = properties.getProviderConfig("openai");

        String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public AiProviderType getType() {
        return AiProviderType.OPENAI;
    }

    @Override
    public String getDisplayName() {
        return AiProviderType.OPENAI.getDisplayName();
    }

    @Override
    public boolean isAvailable() {
        return config != null
                && config.isEnabled()
                && config.getApiKey() != null
                && !config.getApiKey().isBlank()
                && !config.getApiKey().startsWith("your-");
    }

    @Override
    public JobDetailsDto extractJobDetails(byte[] imageBytes, String mimeType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String dataUrl = "data:" + mimeType + ";base64," + base64Image;

        Map<String, Object> requestBody = Map.of(
                "model", config.getModel(),
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", AiPrompts.JOB_EXTRACTION),
                                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                        )
                )),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.2
        );

        String response = callChatCompletions(requestBody);
        return responseParser.parseJobDetails(response);
    }

    @Override
    public EmailDraft generateApplicationEmail(String userProfile, String jobDetails, String matchContext) {
        String response = callTextCompletion(AiPrompts.applicationEmail(userProfile, jobDetails, matchContext), 0.7);
        return responseParser.parseEmailDraft(response);
    }

    @Override
    public MatchAnalysisDto analyzeResumeMatch(String resumeText, String jobDetails) {
        String response = callTextCompletion(AiPrompts.resumeMatch(resumeText, jobDetails), 0.2);
        return responseParser.parseMatchAnalysis(response);
    }

    private String callTextCompletion(String prompt, double temperature) {
        Map<String, Object> requestBody = Map.of(
                "model", config.getModel(),
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "response_format", Map.of("type", "json_object"),
                "temperature", temperature
        );
        return callChatCompletions(requestBody);
    }

    private String callChatCompletions(Map<String, Object> requestBody) {
        try {
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").get(0)
                    .path("message").path("content").asText();
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            throw new RuntimeException("OpenAI request failed: " + e.getMessage());
        }
    }
}
