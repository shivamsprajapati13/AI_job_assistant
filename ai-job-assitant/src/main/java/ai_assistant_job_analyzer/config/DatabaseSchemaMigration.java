package ai_assistant_job_analyzer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${spring.datasource.url:}'.contains('jdbc:h2')")
public class DatabaseSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        migrateColumn("user_profiles", "resume_text", "TEXT");
        migrateColumn("user_profiles", "summary", "TEXT");
        migrateColumn("user_profiles", "skills", "TEXT");
        migrateColumn("user_profiles", "experience", "TEXT");
        migrateColumn("user_profiles", "education", "TEXT");
        migrateColumn("user_profiles", "resume_file_path", "VARCHAR(500)");
    }

    private void migrateColumn(String table, String column, String type) {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE " + table + " ALTER COLUMN " + column + " SET DATA TYPE " + type);
            log.info("Migrated {}.{} to {}", table, column, type);
        } catch (Exception e) {
            log.debug("Column {}.{} migration skipped: {}", table, column, e.getMessage());
        }
    }
}
