package ai_assistant_job_analyzer.ai;

import ai_assistant_job_analyzer.ai.model.EmailDraft;
import ai_assistant_job_analyzer.config.AiProviderProperties;
import ai_assistant_job_analyzer.dto.JobDetailsDto;
import ai_assistant_job_analyzer.dto.MatchAnalysisDto;
import ai_assistant_job_analyzer.entity.UserProfile;
import ai_assistant_job_analyzer.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiProviderService {

    private final AiProviderRegistry registry;
    private final AiProviderProperties properties;
    private final UserProfileService userProfileService;

    public JobDetailsDto extractJobDetails(String userEmail, String providerOverride,
                                           byte[] imageBytes, String mimeType) {
        return resolveStrategy(userEmail, providerOverride).extractJobDetails(imageBytes, mimeType);
    }

    public EmailDraft generateApplicationEmail(String userEmail, String providerOverride,
                                               String userProfile, String jobDetails, String matchContext) {
        return resolveStrategy(userEmail, providerOverride)
                .generateApplicationEmail(userProfile, jobDetails, matchContext);
    }

    public MatchAnalysisDto analyzeResumeMatch(String userEmail, String providerOverride,
                                               String resumeText, String jobDetails) {
        return resolveStrategy(userEmail, providerOverride)
                .analyzeResumeMatch(resumeText, jobDetails);
    }

    public AiProviderStrategy resolveStrategy(String userEmail, String providerOverride) {
        AiProviderType type = resolveProviderType(userEmail, providerOverride);
        return registry.getRequiredOrFallback(type);
    }

    public AiProviderType resolveProviderType(String userEmail, String providerOverride) {
        if (providerOverride != null && !providerOverride.isBlank()) {
            return AiProviderType.fromId(providerOverride);
        }

        UserProfile profile = userProfileService.getOrCreateProfile(userEmail);
        if (profile.getPreferredAiProvider() != null && !profile.getPreferredAiProvider().isBlank()) {
            return AiProviderType.fromId(profile.getPreferredAiProvider());
        }

        return registry.getDefaultType();
    }

    public String getDefaultProviderId() {
        return properties.getDefaultProvider();
    }
}
