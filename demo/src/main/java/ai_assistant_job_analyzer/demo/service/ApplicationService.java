package ai_assistant_job_analyzer.demo.service;

import ai_assistant_job_analyzer.demo.dto.ApplicationHistoryDto;
import ai_assistant_job_analyzer.demo.dto.SendApplicationRequest;
import ai_assistant_job_analyzer.demo.entity.JobApplication;
import ai_assistant_job_analyzer.demo.entity.JobApplication.ApplicationStatus;
import ai_assistant_job_analyzer.demo.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository repository;
    private final GmailService gmailService;
    private final UserProfileService userProfileService;

    public JobApplication saveDraft(String userEmail, SendApplicationRequest request) {
        JobApplication app = JobApplication.builder()
                .userEmail(userEmail)
                .companyName(request.getCompanyName())
                .jobTitle(request.getJobTitle())
                .requiredExperience(request.getRequiredExperience())
                .requiredSkills(joinList(request.getRequiredSkills()))
                .location(request.getLocation())
                .recruiterEmail(request.getRecruiterEmail())
                .applicationInstructions(request.getApplicationInstructions())
                .highlightedKeywords(joinList(request.getHighlightedKeywords()))
                .emailSubject(request.getEmailSubject())
                .emailBody(request.getEmailBody())
                .matchScore(request.getMatchScore())
                .missingSkills(joinList(request.getMissingSkills()))
                .matchedSkills(joinList(request.getMatchedSkills()))
                .status(ApplicationStatus.DRAFT)
                .build();
        return repository.save(app);
    }

    public JobApplication updateDraft(Long id, String userEmail, SendApplicationRequest request) {
        JobApplication app = getOwnedApplication(id, userEmail);
        if (app.getStatus() == ApplicationStatus.SENT) {
            throw new IllegalStateException("Cannot edit an application that has already been sent");
        }
        applyRequestToApplication(app, request);
        app.setStatus(ApplicationStatus.DRAFT);
        return repository.save(app);
    }

    public JobApplication sendApplication(Authentication auth, String userEmail,
                                          SendApplicationRequest request) {
        JobApplication app = saveDraft(userEmail, request);
        return sendExistingApplication(auth, userEmail, app, request);
    }

    public JobApplication sendDraft(Authentication auth, String userEmail, Long id,
                                    SendApplicationRequest request) {
        JobApplication app = getOwnedApplication(id, userEmail);
        if (app.getStatus() == ApplicationStatus.SENT) {
            throw new IllegalStateException("This application has already been sent");
        }
        if (request != null) {
            applyRequestToApplication(app, request);
        }
        return sendExistingApplication(auth, userEmail, app, toRequest(app));
    }

    private JobApplication sendExistingApplication(Authentication auth, String userEmail,
                                                   JobApplication app, SendApplicationRequest request) {
        try {
            String recipient = request.getRecruiterEmail();
            if (recipient == null || recipient.isBlank()) {
                throw new IllegalArgumentException("Recipient email is required");
            }

            byte[] resumeBytes = null;
            String resumeFileName = "resume.pdf";
            try {
                resumeBytes = userProfileService.getResumeBytes(userEmail);
                resumeFileName = userProfileService.getResumeFileName(userEmail);
            } catch (Exception e) {
                log.warn("No resume to attach for user {}", userEmail);
            }

            gmailService.sendEmail(auth, recipient, request.getEmailSubject(),
                    request.getEmailBody(), resumeBytes, resumeFileName);

            app.setStatus(ApplicationStatus.SENT);
            app.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send application", e);
            app.setStatus(ApplicationStatus.FAILED);
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "Failed to send email");
        }

        return repository.save(app);
    }

    public void deleteDraft(Long id, String userEmail) {
        JobApplication app = getOwnedApplication(id, userEmail);
        if (app.getStatus() == ApplicationStatus.SENT) {
            throw new IllegalStateException("Cannot delete an application that has already been sent");
        }
        repository.delete(app);
    }

    public List<ApplicationHistoryDto> getHistory(String userEmail) {
        return repository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(ApplicationHistoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    public JobApplication getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
    }

    public ApplicationHistoryDto getDetail(Long id, String userEmail) {
        return ApplicationHistoryDto.fromEntity(getOwnedApplication(id, userEmail));
    }

    private JobApplication getOwnedApplication(Long id, String userEmail) {
        JobApplication app = getById(id);
        if (!app.getUserEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Application not found");
        }
        return app;
    }

    private void applyRequestToApplication(JobApplication app, SendApplicationRequest request) {
        app.setCompanyName(request.getCompanyName());
        app.setJobTitle(request.getJobTitle());
        app.setRequiredExperience(request.getRequiredExperience());
        app.setRequiredSkills(joinList(request.getRequiredSkills()));
        app.setLocation(request.getLocation());
        app.setRecruiterEmail(request.getRecruiterEmail());
        app.setApplicationInstructions(request.getApplicationInstructions());
        app.setHighlightedKeywords(joinList(request.getHighlightedKeywords()));
        app.setEmailSubject(request.getEmailSubject());
        app.setEmailBody(request.getEmailBody());
        app.setMatchScore(request.getMatchScore());
        app.setMissingSkills(joinList(request.getMissingSkills()));
        app.setMatchedSkills(joinList(request.getMatchedSkills()));
    }

    private SendApplicationRequest toRequest(JobApplication app) {
        return SendApplicationRequest.builder()
                .companyName(app.getCompanyName())
                .jobTitle(app.getJobTitle())
                .requiredExperience(app.getRequiredExperience())
                .requiredSkills(splitCsv(app.getRequiredSkills()))
                .location(app.getLocation())
                .recruiterEmail(app.getRecruiterEmail())
                .applicationInstructions(app.getApplicationInstructions())
                .highlightedKeywords(splitCsv(app.getHighlightedKeywords()))
                .emailSubject(app.getEmailSubject())
                .emailBody(app.getEmailBody())
                .matchScore(app.getMatchScore())
                .missingSkills(splitCsv(app.getMissingSkills()))
                .matchedSkills(splitCsv(app.getMatchedSkills()))
                .build();
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return List.of(csv.split(","));
    }

    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }
}
