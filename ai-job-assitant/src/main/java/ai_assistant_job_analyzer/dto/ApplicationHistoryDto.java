package ai_assistant_job_analyzer.dto;

import ai_assistant_job_analyzer.entity.JobApplication;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationHistoryDto {
    private Long id;
    private String companyName;
    private String jobTitle;
    private String requiredExperience;
    private String location;
    private String recruiterEmail;
    private String applicationInstructions;
    private Integer matchScore;
    private String status;
    private String emailSubject;
    private String emailBody;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private List<String> requiredSkills;
    private List<String> missingSkills;
    private List<String> matchedSkills;
    private List<String> highlightedKeywords;

    public static ApplicationHistoryDto fromEntity(JobApplication app) {
        return ApplicationHistoryDto.builder()
                .id(app.getId())
                .companyName(app.getCompanyName())
                .jobTitle(app.getJobTitle())
                .requiredExperience(app.getRequiredExperience())
                .location(app.getLocation())
                .recruiterEmail(app.getRecruiterEmail())
                .applicationInstructions(app.getApplicationInstructions())
                .matchScore(app.getMatchScore())
                .status(app.getStatus() != null ? app.getStatus().name() : "DRAFT")
                .emailSubject(app.getEmailSubject())
                .emailBody(app.getEmailBody())
                .sentAt(app.getSentAt())
                .createdAt(app.getCreatedAt())
                .requiredSkills(splitCsv(app.getRequiredSkills()))
                .missingSkills(splitCsv(app.getMissingSkills()))
                .matchedSkills(splitCsv(app.getMatchedSkills()))
                .highlightedKeywords(splitCsv(app.getHighlightedKeywords()))
                .build();
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
