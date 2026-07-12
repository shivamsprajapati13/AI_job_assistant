package ai_assistant_job_analyzer.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendApplicationRequest {
    private String companyName;
    private String jobTitle;
    private String requiredExperience;
    private List<String> requiredSkills;
    private String location;
    private String recruiterEmail;
    private String applicationInstructions;
    private String emailSubject;
    private String emailBody;
    private Integer matchScore;
    private List<String> missingSkills;
    private List<String> matchedSkills;
    private List<String> highlightedKeywords;
}
