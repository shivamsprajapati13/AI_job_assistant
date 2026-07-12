package ai_assistant_job_analyzer.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProviderProperties {

    private String defaultProvider = "gemini";
    private Map<String, ProviderConfig> providers = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class ProviderConfig {
        private boolean enabled = true;
        private String apiKey;
        private String model;
        private String baseUrl;
    }

    public ProviderConfig getProviderConfig(String providerId) {
        return providers.get(providerId);
    }
}
