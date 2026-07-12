package ai_assistant_job_analyzer.ai.gemini;

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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "ai.providers.gemini", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GeminiAiProviderStrategy implements AiProviderStrategy {

    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AiResponseParser responseParser;
    private final AiProviderProperties.ProviderConfig config;

    public GeminiAiProviderStrategy(AiProviderProperties properties,
                                    ObjectMapper objectMapper,
                                    AiResponseParser responseParser) {
        this.objectMapper = objectMapper;
        this.responseParser = responseParser;
        this.config = properties.getProviderConfig("gemini");

        String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public AiProviderType getType() {
        return AiProviderType.GEMINI;
    }

    @Override
    public String getDisplayName() {
        return AiProviderType.GEMINI.getDisplayName();
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
        String response = callVision(AiPrompts.JOB_EXTRACTION, imageBytes, mimeType, 0.2);
        return responseParser.parseJobDetails(response);
    }

    @Override
    public EmailDraft generateApplicationEmail(String userProfile, String jobDetails, String matchContext) {
        String response = callText(AiPrompts.applicationEmail(userProfile, jobDetails, matchContext), 0.7);
        return responseParser.parseEmailDraft(response);
    }

    @Override
    public MatchAnalysisDto analyzeResumeMatch(String resumeText, String jobDetails) {
        String response = callText(AiPrompts.resumeMatch(resumeText, jobDetails), 0.2);
        return responseParser.parseMatchAnalysis(response);
    }

    private String callVision(String prompt, byte[] imageBytes, String mimeType, double temperature) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("text", prompt),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", base64Image
                                ))
                        )
                )),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "responseMimeType", "application/json"
                )
        );
        return executeRequest(requestBody);
    }

    private String callText(String prompt, double temperature) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "responseMimeType", "application/json"
                )
        );
        return executeRequest(requestBody);
    }

    private String executeRequest(Map<String, Object> requestBody) {
        try {
            String url = "/v1beta/models/" + config.getModel() + ":generateContent?key=" + config.getApiKey();

            String responseBody = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("Gemini quota exceeded", e);
            throw new RuntimeException(buildQuotaErrorMessage(e));
        } catch (HttpClientErrorException e) {
            log.error("Gemini API error: {}", e.getStatusCode(), e);
            throw new RuntimeException("Gemini API error: " + extractApiMessage(e));
        } catch (Exception e) {
            log.error("Gemini request failed", e);
            throw new RuntimeException("Gemini request failed: " + e.getMessage());
        }
    }

    static String buildQuotaErrorMessage(HttpClientErrorException e) {
        String body = e.getResponseBodyAsString();
        if (body.contains("limit: 0")) {
            return """
                    Gemini free-tier quota is not active for this API key (limit: 0).
                    Fix: 1) Create a new API key at https://aistudio.google.com/apikey
                    2) Ensure the key is linked to a Google AI Studio project
                    3) Check usage at https://ai.dev/rate-limit
                    4) If needed, enable billing in AI Studio (free tier still applies)
                    5) Try model gemini-2.5-flash instead of gemini-2.0-flash""";
        }
        return "Gemini rate limit exceeded. Wait a minute and retry, or check https://ai.dev/rate-limit";
    }

    static String extractApiMessage(HttpClientErrorException e) {
        try {
            JsonNode root = new ObjectMapper().readTree(e.getResponseBodyAsString());
            return root.path("error").path("message").asText(e.getMessage());
        } catch (Exception ignored) {
            return e.getMessage();
        }
    }
}
