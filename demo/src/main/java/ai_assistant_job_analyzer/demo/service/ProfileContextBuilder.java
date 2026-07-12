package ai_assistant_job_analyzer.demo.service;

import ai_assistant_job_analyzer.demo.entity.UserProfile;

public final class ProfileContextBuilder {

    private ProfileContextBuilder() {}

    public static String build(UserProfile profile) {
        StringBuilder sb = new StringBuilder();
        append(sb, "Full Name", profile.getFullName());
        append(sb, "Email", profile.getEmail());
        append(sb, "Phone", profile.getPhone());
        append(sb, "Current Title", profile.getCurrentTitle());
        append(sb, "LinkedIn", profile.getLinkedinUrl());
        append(sb, "GitHub", profile.getGithubUrl());
        append(sb, "Professional Summary", profile.getSummary());
        append(sb, "Skills", profile.getSkills());
        append(sb, "Work Experience", profile.getExperience());
        append(sb, "Education", profile.getEducation());
        if (profile.getResumeFileName() != null) {
            sb.append("Resume on file: ").append(profile.getResumeFileName()).append("\n");
        }
        if (profile.getResumeText() != null && !profile.getResumeText().isBlank()) {
            sb.append("\n--- Resume Content ---\n").append(profile.getResumeText()).append("\n");
        }
        return sb.toString().isBlank() ? "No profile details provided yet." : sb.toString();
    }

    public static boolean isProfileConfigured(UserProfile profile) {
        return isPresent(profile.getFullName())
                || isPresent(profile.getSummary())
                || isPresent(profile.getSkills())
                || isPresent(profile.getExperience())
                || isPresent(profile.getResumeText());
    }

    private static void append(StringBuilder sb, String label, String value) {
        if (isPresent(value)) {
            sb.append(label).append(": ").append(value.trim()).append("\n");
        }
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
