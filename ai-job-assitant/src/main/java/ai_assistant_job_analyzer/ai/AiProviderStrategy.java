package ai_assistant_job_analyzer.ai;

import ai_assistant_job_analyzer.ai.model.EmailDraft;
import ai_assistant_job_analyzer.dto.JobDetailsDto;
import ai_assistant_job_analyzer.dto.MatchAnalysisDto;

public interface AiProviderStrategy {

    AiProviderType getType();

    String getDisplayName();

    boolean isAvailable();

    JobDetailsDto extractJobDetails(byte[] imageBytes, String mimeType);

    EmailDraft generateApplicationEmail(String userProfile, String jobDetails, String matchContext);

    MatchAnalysisDto analyzeResumeMatch(String resumeText, String jobDetails);
}
