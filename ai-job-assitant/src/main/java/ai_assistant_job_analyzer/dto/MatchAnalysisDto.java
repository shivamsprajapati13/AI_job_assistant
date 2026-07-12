package ai_assistant_job_analyzer.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchAnalysisDto {
    private Integer matchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> highlightedKeywords;
    private String summary;
}
