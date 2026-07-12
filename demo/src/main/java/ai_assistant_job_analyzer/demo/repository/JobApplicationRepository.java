package ai_assistant_job_analyzer.demo.repository;

import ai_assistant_job_analyzer.demo.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
