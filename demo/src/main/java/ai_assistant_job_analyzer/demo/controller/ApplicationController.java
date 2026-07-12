package ai_assistant_job_analyzer.demo.controller;

import ai_assistant_job_analyzer.demo.dto.ApplicationHistoryDto;
import ai_assistant_job_analyzer.demo.dto.SendApplicationRequest;
import ai_assistant_job_analyzer.demo.entity.JobApplication;
import ai_assistant_job_analyzer.demo.service.ApplicationService;
import ai_assistant_job_analyzer.demo.service.GmailService;
import ai_assistant_job_analyzer.demo.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final GmailService gmailService;
    private final UserProfileService userProfileService;

    @PostMapping("/send")
    public ResponseEntity<?> sendApplication(
            Authentication auth,
            @RequestBody SendApplicationRequest request,
            @RequestParam(defaultValue = "") String email) {
        String userEmail = resolveEmail(email);
        try {
            JobApplication app = applicationService.sendApplication(auth, userEmail, request);
            return ResponseEntity.ok(Map.of(
                    "id", app.getId(),
                    "status", app.getStatus().name(),
                    "sentAt", app.getSentAt() != null ? app.getSentAt().toString() : null
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/draft")
    public ResponseEntity<?> saveDraft(
            @RequestBody SendApplicationRequest request,
            @RequestParam(defaultValue = "") String email) {
        String userEmail = resolveEmail(email);
        JobApplication app = applicationService.saveDraft(userEmail, request);
        return ResponseEntity.ok(Map.of("id", app.getId(), "status", app.getStatus().name()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDraft(
            @PathVariable Long id,
            @RequestBody SendApplicationRequest request,
            @RequestParam(defaultValue = "") String email) {
        String userEmail = resolveEmail(email);
        try {
            JobApplication app = applicationService.updateDraft(id, userEmail, request);
            return ResponseEntity.ok(Map.of("id", app.getId(), "status", app.getStatus().name()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<?> sendDraft(
            Authentication auth,
            @PathVariable Long id,
            @RequestBody(required = false) SendApplicationRequest request,
            @RequestParam(defaultValue = "") String email) {
        String userEmail = resolveEmail(email);
        try {
            JobApplication app = applicationService.sendDraft(auth, userEmail, id, request);
            return ResponseEntity.ok(Map.of(
                    "id", app.getId(),
                    "status", app.getStatus().name(),
                    "sentAt", app.getSentAt() != null ? app.getSentAt().toString() : null
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ApplicationHistoryDto>> getHistory(
            @RequestParam(defaultValue = "") String email) {
        String userEmail = resolveEmail(email);
        return ResponseEntity.ok(applicationService.getHistory(userEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationHistoryDto> getApplication(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String email) {
        String userEmail = resolveEmail(email);
        return ResponseEntity.ok(applicationService.getDetail(id, userEmail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDraft(
            @PathVariable Long id,
            @RequestParam(defaultValue = "") String email) {
        String userEmail = resolveEmail(email);
        try {
            applicationService.deleteDraft(id, userEmail);
            return ResponseEntity.ok(Map.of("message", "Draft deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/gmail-status")
    public ResponseEntity<Map<String, Boolean>> gmailStatus(Authentication auth) {
        boolean connected = gmailService.isGmailConnected(auth);
        return ResponseEntity.ok(Map.of("connected", connected));
    }

    private String resolveEmail(String email) {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return userProfileService.getDefaultUserEmail();
    }
}
