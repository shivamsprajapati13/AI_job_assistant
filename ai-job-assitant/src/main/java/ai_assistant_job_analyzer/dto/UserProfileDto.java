package ai_assistant_job_analyzer.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String linkedinUrl;
    private String githubUrl;
    private String currentTitle;
    private String summary;
    private String skills;
    private String experience;
    private String education;
    private String resumeFileName;
    private Boolean hasResume;
    private String preferredAiProvider;
}
