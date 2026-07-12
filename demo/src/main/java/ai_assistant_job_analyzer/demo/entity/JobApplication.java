package ai_assistant_job_analyzer.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    private String companyName;
    private String jobTitle;
    private String requiredExperience;
    private String location;
    private String recruiterEmail;

    @Column(columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(columnDefinition = "TEXT")
    private String applicationInstructions;

    @Column(columnDefinition = "TEXT")
    private String highlightedKeywords;

    @Column(columnDefinition = "TEXT")
    private String emailSubject;

    @Column(columnDefinition = "TEXT")
    private String emailBody;

    private Integer matchScore;

    @Column(columnDefinition = "TEXT")
    private String missingSkills;

    @Column(columnDefinition = "TEXT")
    private String matchedSkills;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private String screenshotPath;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public enum ApplicationStatus {
        DRAFT, SENT, FAILED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = ApplicationStatus.DRAFT;
        }
    }
}
