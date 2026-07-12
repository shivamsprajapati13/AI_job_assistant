package ai_assistant_job_analyzer.controller;

import ai_assistant_job_analyzer.demo.dto.*;
import ai_assistant_job_analyzer.service.JobExtractionService;
import ai_assistant_job_analyzer.service.UserProfileService;
import ai_assistant_job_analyzer.dto.EmailDraftDto;
import ai_assistant_job_analyzer.dto.GenerateEmailRequest;
import ai_assistant_job_analyzer.dto.JobDetailsDto;
import ai_assistant_job_analyzer.dto.MatchAnalysisDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobPostController {

    private final JobExtractionService jobExtractionService;
    private final UserProfileService userProfileService;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobDetailsDto> extractJobDetails(
            @RequestParam("screenshot") MultipartFile screenshot,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String provider) throws Exception {
        String mimeType = screenshot.getContentType();
        if (mimeType == null) {
            mimeType = "image/png";
        }
        String userEmail = resolveEmail(email);
        JobDetailsDto details = jobExtractionService.extractFromScreenshot(
                userEmail, provider, screenshot.getBytes(), mimeType);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/analyze-match")
    public ResponseEntity<MatchAnalysisDto> analyzeMatch(
            @RequestBody JobDetailsDto jobDetails,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String provider) {
        String userEmail = resolveEmail(email);
        MatchAnalysisDto analysis = jobExtractionService.analyzeMatch(jobDetails, userEmail, provider);
        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/generate-email")
    public ResponseEntity<EmailDraftDto> generateEmail(
            @RequestBody GenerateEmailRequest request,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String provider) {
        String userEmail = resolveEmail(email);
        EmailDraftDto draft = jobExtractionService.generateEmail(request, userEmail, provider);
        return ResponseEntity.ok(draft);
    }

    private String resolveEmail(String email) {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return userProfileService.getDefaultUserEmail();
    }
}
