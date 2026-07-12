package ai_assistant_job_analyzer.service;

import ai_assistant_job_analyzer.dto.UserProfileDto;
import ai_assistant_job_analyzer.entity.UserProfile;
import ai_assistant_job_analyzer.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.default-user-email}")
    private String defaultUserEmail;

    public UserProfile getOrCreateProfile(String email) {
        String resolvedEmail = resolveEmail(email);
        return repository.findByEmail(resolvedEmail)
                .orElseGet(() -> repository.save(UserProfile.builder()
                        .email(resolvedEmail)
                        .fullName("")
                        .build()));
    }

    public UserProfileDto getProfile(String email) {
        return toDto(getOrCreateProfile(email));
    }

    public UserProfileDto updateProfile(UserProfileDto dto) {
        String email = resolveEmail(dto.getEmail());
        UserProfile profile = getOrCreateProfile(email);

        profile.setFullName(dto.getFullName());
        profile.setPhone(dto.getPhone());
        profile.setLinkedinUrl(dto.getLinkedinUrl());
        profile.setGithubUrl(dto.getGithubUrl());
        profile.setCurrentTitle(dto.getCurrentTitle());
        profile.setSummary(dto.getSummary());
        profile.setSkills(dto.getSkills());
        profile.setExperience(dto.getExperience());
        profile.setEducation(dto.getEducation());
        profile.setPreferredAiProvider(dto.getPreferredAiProvider());

        return toDto(repository.save(profile));
    }

    public UserProfileDto uploadResume(String email, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is empty");
        }
        UserProfile profile = getOrCreateProfile(email);
        Path uploadPath = Paths.get(uploadDir, "resumes");
        Files.createDirectories(uploadPath);

        String safeName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                : "resume.pdf";
        String fileName = profile.getEmail().replace("@", "_") + "_" + safeName;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String resumeText = extractText(file, filePath);

        profile.setResumeFileName(file.getOriginalFilename());
        profile.setResumeFilePath(filePath.toString());
        profile.setResumeText(resumeText);

        return toDto(repository.save(profile));
    }

    public byte[] getResumeBytes(String email) throws IOException {
        UserProfile profile = getOrCreateProfile(email);
        if (profile.getResumeFilePath() == null) {
            throw new IllegalStateException("No resume uploaded");
        }
        return Files.readAllBytes(Paths.get(profile.getResumeFilePath()));
    }

    public String getResumeFileName(String email) {
        UserProfile profile = getOrCreateProfile(email);
        return profile.getResumeFileName() != null ? profile.getResumeFileName() : "resume.pdf";
    }

    public String getDefaultUserEmail() {
        return defaultUserEmail;
    }

    private String resolveEmail(String email) {
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        return defaultUserEmail;
    }

    private String extractText(MultipartFile file, Path filePath) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        }
        return Files.readString(filePath);
    }

    private UserProfileDto toDto(UserProfile profile) {
        return UserProfileDto.builder()
                .id(profile.getId())
                .email(profile.getEmail())
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .linkedinUrl(profile.getLinkedinUrl())
                .githubUrl(profile.getGithubUrl())
                .currentTitle(profile.getCurrentTitle())
                .summary(profile.getSummary())
                .skills(profile.getSkills())
                .experience(profile.getExperience())
                .education(profile.getEducation())
                .resumeFileName(profile.getResumeFileName())
                .hasResume(profile.getResumeFilePath() != null)
                .preferredAiProvider(profile.getPreferredAiProvider())
                .build();
    }
}
