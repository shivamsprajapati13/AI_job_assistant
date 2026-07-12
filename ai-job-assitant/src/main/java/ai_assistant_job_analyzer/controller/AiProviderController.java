package ai_assistant_job_analyzer.controller;

import ai_assistant_job_analyzer.ai.AiProviderRegistry;
import ai_assistant_job_analyzer.ai.AiProviderService;
import ai_assistant_job_analyzer.dto.AiProviderInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiProviderController {

    private final AiProviderRegistry registry;
    private final AiProviderService aiProviderService;

    @GetMapping("/providers")
    public ResponseEntity<List<AiProviderInfoDto>> listProviders() {
        return ResponseEntity.ok(registry.listProviders());
    }

    @GetMapping("/default-provider")
    public ResponseEntity<Map<String, String>> getDefaultProvider() {
        return ResponseEntity.ok(Map.of("provider", aiProviderService.getDefaultProviderId()));
    }
}
