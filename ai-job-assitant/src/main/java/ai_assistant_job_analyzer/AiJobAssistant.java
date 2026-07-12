package ai_assistant_job_analyzer;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiJobAssistant {

	public static void main(String[] args) {
		loadEnvFile();
		SpringApplication.run(AiJobAssistant.class, args);
	}

	private static void loadEnvFile() {
		Dotenv dotenv = Dotenv.configure()
				.directory("./")
				.ignoreIfMissing()
				.load();
		dotenv.entries().forEach(entry -> {
			String key = entry.getKey();
			if (System.getenv(key) == null && System.getProperty(key) == null) {
				System.setProperty(key, entry.getValue());
			}
		});
	}
}
