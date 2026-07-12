package ai_assistant_job_analyzer.controller;

import ai_assistant_job_analyzer.dto.UserProfileDto;
import ai_assistant_job_analyzer.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile(
            @RequestParam(defaultValue = "") String email) {
        return ResponseEntity.ok(userProfileService.getProfile(email));
    }

    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(@RequestBody UserProfileDto dto) {
        return ResponseEntity.ok(userProfileService.updateProfile(dto));
    }

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadResume(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam(defaultValue = "") String email) {
        try {
            UserProfileDto profile = userProfileService.uploadResume(email, resume);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Upload failed"));
        }
    }
}
