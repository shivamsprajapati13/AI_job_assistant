package ai_assistant_job_analyzer.demo.ai;

import ai_assistant_job_analyzer.demo.ai.model.EmailDraft;
import ai_assistant_job_analyzer.demo.dto.JobDetailsDto;
import ai_assistant_job_analyzer.demo.dto.MatchAnalysisDto;

public interface AiProviderStrategy {

    AiProviderType getType();

    String getDisplayName();

    boolean isAvailable();

    JobDetailsDto extractJobDetails(byte[] imageBytes, String mimeType);

    EmailDraft generateApplicationEmail(String userProfile, String jobDetails, String matchContext);

    MatchAnalysisDto analyzeResumeMatch(String resumeText, String jobDetails);
}
