package ai_assistant_job_analyzer.demo.service;

import ai_assistant_job_analyzer.demo.ai.AiProviderService;
import ai_assistant_job_analyzer.demo.ai.model.EmailDraft;
import ai_assistant_job_analyzer.demo.dto.*;
import ai_assistant_job_analyzer.demo.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobExtractionService {

    private final AiProviderService aiProviderService;
    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper;

    public JobDetailsDto extractFromScreenshot(String userEmail, String provider,
                                               byte[] imageBytes, String mimeType) {
        return aiProviderService.extractJobDetails(userEmail, provider, imageBytes, mimeType);
    }

    public MatchAnalysisDto analyzeMatch(JobDetailsDto jobDetails, String userEmail, String provider) {
        UserProfile profile = userProfileService.getOrCreateProfile(userEmail);
        String resumeText = resolveResumeContext(profile);
        String jobDetailsJson = toJson(jobDetails);
        return aiProviderService.analyzeResumeMatch(userEmail, provider, resumeText, jobDetailsJson);
    }

    public EmailDraftDto generateEmail(GenerateEmailRequest request, String userEmail, String provider) {
        JobDetailsDto jobDetails = request.getJobDetails();
        UserProfile profile = userProfileService.getOrCreateProfile(userEmail);

        String profileText = ProfileContextBuilder.build(profile);
        String jobDetailsJson = toJson(jobDetails);
        String matchContext = buildMatchContext(request.getMatchAnalysis());

        EmailDraft draft = aiProviderService.generateApplicationEmail(
                userEmail, provider, profileText, jobDetailsJson, matchContext);

        String recipient = jobDetails.getRecruiterEmail();
        if (recipient == null || recipient.isBlank()) {
            recipient = extractEmailFromInstructions(jobDetails.getApplicationInstructions());
        }

        return EmailDraftDto.builder()
                .subject(draft.subject())
                .body(draft.body())
                .recipientEmail(recipient)
                .build();
    }

    private String resolveResumeContext(UserProfile profile) {
        if (profile.getResumeText() != null && !profile.getResumeText().isBlank()) {
            return profile.getResumeText();
        }
        return ProfileContextBuilder.build(profile);
    }

    private String buildMatchContext(MatchAnalysisDto match) {
        if (match == null) {
            return "No match analysis available. Focus on profile skills relevant to the job.";
        }
        StringBuilder sb = new StringBuilder();
        if (match.getMatchScore() != null) {
            sb.append("Match Score: ").append(match.getMatchScore()).append("%\n");
        }
        if (match.getMatchedSkills() != null && !match.getMatchedSkills().isEmpty()) {
            sb.append("Matched Skills: ").append(String.join(", ", match.getMatchedSkills())).append("\n");
        }
        if (match.getMissingSkills() != null && !match.getMissingSkills().isEmpty()) {
            sb.append("Skills to address carefully: ").append(String.join(", ", match.getMissingSkills())).append("\n");
        }
        if (match.getSummary() != null) {
            sb.append("Summary: ").append(match.getSummary()).append("\n");
        }
        return sb.toString().isBlank() ? "No match analysis available." : sb.toString();
    }

    private String extractEmailFromInstructions(String instructions) {
        if (instructions == null) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}")
                .matcher(instructions);
        return matcher.find() ? matcher.group() : null;
    }

    private String toJson(JobDetailsDto dto) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("companyName", dto.getCompanyName());
            map.put("jobTitle", dto.getJobTitle());
            map.put("requiredExperience", dto.getRequiredExperience());
            map.put("requiredSkills", dto.getRequiredSkills());
            map.put("location", dto.getLocation());
            map.put("recruiterEmail", dto.getRecruiterEmail());
            map.put("applicationInstructions", dto.getApplicationInstructions());
            map.put("highlightedKeywords", dto.getHighlightedKeywords());
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize job details", e);
        }
    }
}
