package ai_assistant_job_analyzer.ai;

import ai_assistant_job_analyzer.config.AiProviderProperties;
import ai_assistant_job_analyzer.dto.AiProviderInfoDto;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AiProviderRegistry {

    private final Map<AiProviderType, AiProviderStrategy> strategies;
    private final AiProviderProperties properties;

    public AiProviderRegistry(List<AiProviderStrategy> strategyList, AiProviderProperties properties) {
        this.properties = properties;
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(AiProviderStrategy::getType, Function.identity()));
    }

    public AiProviderStrategy getRequired(AiProviderType type) {
        AiProviderStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "AI provider '" + type.getId() + "' is not enabled. Enable it in application.properties.");
        }
        if (!strategy.isAvailable()) {
            throw new IllegalStateException(
                    "AI provider '" + type.getId() + "' is not configured. Set its API key in application.properties.");
        }
        return strategy;
    }

    public AiProviderStrategy getRequiredOrFallback(AiProviderType preferred) {
        AiProviderStrategy strategy = strategies.get(preferred);
        if (strategy != null && strategy.isAvailable()) {
            return strategy;
        }
        return getRequired(getDefaultType());
    }

    public List<AiProviderInfoDto> listProviders() {
        return strategies.values().stream()
                .sorted(Comparator.comparing(s -> s.getType().getId()))
                .map(this::toInfo)
                .toList();
    }

    public AiProviderType getDefaultType() {
        return AiProviderType.fromId(properties.getDefaultProvider());
    }

    private AiProviderInfoDto toInfo(AiProviderStrategy strategy) {
        AiProviderProperties.ProviderConfig config = properties.getProviderConfig(strategy.getType().getId());
        return AiProviderInfoDto.builder()
                .id(strategy.getType().getId())
                .displayName(strategy.getDisplayName())
                .model(config != null ? config.getModel() : null)
                .available(strategy.isAvailable())
                .supportsVision(true)
                .build();
    }
}
