package ai_assistant_job_analyzer.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiProviderInfoDto {
    private String id;
    private String displayName;
    private String model;
    private boolean available;
    private boolean supportsVision;
}
