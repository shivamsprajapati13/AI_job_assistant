package ai_assistant_job_analyzer.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateEmailRequest {
    private JobDetailsDto jobDetails;
    private MatchAnalysisDto matchAnalysis;
}
