package ai_assistant_job_analyzer.demo.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDetailsDto {
    private String companyName;
    private String jobTitle;
    private String requiredExperience;
    private List<String> requiredSkills;
    private String location;
    private String recruiterEmail;
    private String applicationInstructions;
    private List<String> highlightedKeywords;
    private String rawExtractedText;
}
