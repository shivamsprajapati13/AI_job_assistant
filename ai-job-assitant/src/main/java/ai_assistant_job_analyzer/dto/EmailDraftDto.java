package ai_assistant_job_analyzer.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDraftDto {
    private String subject;
    private String body;
    private String recipientEmail;
}
