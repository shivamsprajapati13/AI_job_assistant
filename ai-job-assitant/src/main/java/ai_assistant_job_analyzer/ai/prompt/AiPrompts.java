package ai_assistant_job_analyzer.ai.prompt;

public final class AiPrompts {

    private AiPrompts() {}

    public static final String JOB_EXTRACTION = """
            Analyze this hiring/job post screenshot. Extract all relevant job application information.
            Return ONLY valid JSON with these fields (use null if not found):
            {
              "companyName": "string",
              "jobTitle": "string",
              "requiredExperience": "string (e.g. 3-5 years)",
              "requiredSkills": ["skill1", "skill2"],
              "location": "string",
              "recruiterEmail": "string",
              "applicationInstructions": "string (how to apply)",
              "highlightedKeywords": ["keyword1", "keyword2"],
              "rawExtractedText": "full text visible in the image"
            }
            Understand context - this may be from LinkedIn, X/Twitter, WhatsApp, Telegram, Slack, or any platform.
            """;

    public static String applicationEmail(String userProfile, String jobDetails, String matchContext) {
        return """
                You are a professional career coach writing a job application email on behalf of the candidate.

                CANDIDATE PROFILE (use this as the primary source — reference their real name, title, skills, and experience):
                %s

                JOB DETAILS:
                %s

                MATCH ANALYSIS (if provided, emphasize matched skills and address gaps naturally):
                %s

                Instructions:
                - Write in first person as the candidate using details from their profile
                - Use the candidate's actual name from the profile in the greeting/sign-off
                - Highlight experience and skills from the profile that match the job requirements
                - Reference the company name and job title specifically
                - Keep it professional, concise, and genuine (3-4 short paragraphs)
                - Do NOT invent qualifications not present in the profile
                - Mention that their resume is attached

                Return ONLY valid JSON:
                {
                  "subject": "email subject line mentioning role and candidate name",
                  "body": "full email body with proper paragraphs"
                }
                """.formatted(userProfile, jobDetails, matchContext);
    }

    public static String resumeMatch(String resumeText, String jobDetails) {
        return """
                Compare this resume against the job requirements and provide a match analysis.

                RESUME:
                %s

                JOB DETAILS:
                %s

                Return ONLY valid JSON:
                {
                  "matchScore": 0-100 integer,
                  "matchedSkills": ["skills the candidate has"],
                  "missingSkills": ["required skills the candidate lacks"],
                  "highlightedKeywords": ["important keywords from job description"],
                  "summary": "brief 2-sentence match summary"
                }
                """.formatted(resumeText, jobDetails);
    }
}
