package ai_assistant_job_analyzer.ai;

public enum AiProviderType {
    OPENAI("openai", "OpenAI"),
    GEMINI("gemini", "Google Gemini");

    private final String id;
    private final String displayName;

    AiProviderType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AiProviderType fromId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("AI provider id is required");
        }
        for (AiProviderType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AI provider: " + id);
    }
}
