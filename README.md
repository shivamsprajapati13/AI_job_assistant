# Job Application Assistant

AI-powered platform that automates job applications from hiring post screenshots.

## Features

- **Screenshot Upload** — Upload hiring posts from LinkedIn, X, WhatsApp, Telegram, Slack, etc.
- **AI Extraction** — Gemini Vision extracts company, title, skills, location, recruiter email, and more
- **Match Analysis** — Compares your resume against job requirements with a match score
- **Email Generation** — AI writes personalized application emails based on your profile
- **Gmail Integration** — Send applications with resume attached via Gmail OAuth
- **Application History** — Track all sent and draft applications

## Prerequisites

- Java 17+
- Maven 3.8+
- [Google AI API Key](https://aistudio.google.com/apikey) (Gemini)
- [Google Cloud OAuth Credentials](https://console.cloud.google.com/) (Gmail sending)

## Setup

### 1. Configure secrets (.env)

Copy the example env file and add your credentials:

```bash
cd demo
cp .env.example .env
```

Edit `.env` with your values:

```env
MYSQL_URL=jdbc:mysql://localhost:3306/jobassistant?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
MYSQL_USER=root
MYSQL_PASSWORD=your-mysql-password

GEMINI_API_KEY=your-gemini-api-key

GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret

DEFAULT_USER_EMAIL=you@example.com
```

The app loads `.env` automatically on startup. You can also set the same variables as OS environment variables (they take precedence over `.env`).

> **Important:** Never commit `.env` to git. Only `.env.example` is tracked.

Get API keys from:
- [Google AI Studio](https://aistudio.google.com/apikey) (Gemini)
- [Google Cloud Console](https://console.cloud.google.com/) (Gmail OAuth)

### 2. Google Cloud Setup (Gmail OAuth)

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project and enable the **Gmail API**
3. Create OAuth 2.0 credentials (Web application type)
4. Add authorized redirect URI: `http://localhost:8600/login/oauth2/code/google`
5. Copy Client ID and Client Secret into your `.env`

### 3. MySQL Database

Create a database (optional if `createDatabaseIfNotExist=true` is in the URL):

```sql
CREATE DATABASE jobassistant;
```

Set `MYSQL_URL`, `MYSQL_USER`, and `MYSQL_PASSWORD` in `.env`. Tables are created automatically on first run.

### 4. Run the Application

```bash
cd demo
./mvnw spring-boot:run
```

Open [http://localhost:8600](http://localhost:8600) in your browser.

## Usage Workflow

1. **Set up your profile** — Go to Profile, fill in your details, and upload your resume (PDF supported)
2. **Connect Gmail** — Click "Connect Gmail" in the navbar to authorize sending
3. **Apply to a job**:
   - Upload a screenshot of a hiring post
   - Review and edit the AI-extracted job details
   - Run match analysis to see how well your resume fits
   - Generate a personalized application email
   - Edit the email and send (resume is attached automatically)
4. **Track applications** — View all sent/draft applications in History

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/extract` | Extract job details from screenshot |
| POST | `/api/analyze-match` | Analyze resume-to-job match |
| POST | `/api/generate-email` | Generate application email |
| POST | `/api/applications/send` | Send application via Gmail |
| POST | `/api/applications/draft` | Save application as draft |
| GET | `/api/applications/history` | Get application history |
| GET/PUT | `/api/profile` | Get/update user profile |
| POST | `/api/profile/resume` | Upload resume |
| GET | `/api/ai/providers` | List available AI providers |
| GET | `/api/ai/default-provider` | Get default AI provider |

All AI endpoints accept an optional `provider` query param (`openai`, `gemini`). If omitted, the user's profile preference or `ai.default-provider` is used.

## Adding a New AI Provider

1. Add a value to `AiProviderType` enum
2. Create a class implementing `AiProviderStrategy` in `ai/<provider>/`
3. Register config in `application.properties` under `ai.providers.<id>`
4. Spring auto-registers the strategy bean via `@Component`

```java
@Component
@ConditionalOnProperty(prefix = "ai.providers.anthropic", name = "enabled", havingValue = "true")
public class AnthropicProviderStrategy implements AiProviderStrategy {
    // implement extractJobDetails, generateApplicationEmail, analyzeResumeMatch
}
```

## Tech Stack

- **Backend**: Spring Boot 4.1, Spring Security, Spring Data JPA
- **AI**: Google Gemini 2.0 Flash (Vision + Text)
- **Email**: Gmail API with OAuth2
- **Database**: MySQL (configurable via `.env`)
- **Frontend**: Vanilla HTML/CSS/JS
- **Resume Parsing**: Apache PDFBox

## Project Structure

```
demo/
├── src/main/java/.../
│   ├── config/          # Security, Web, Exception handling
│   ├── controller/      # REST API endpoints
│   ├── dto/             # Data transfer objects
│   ├── entity/          # JPA entities
│   ├── repository/      # Data access
│   └── service/         # Business logic (Gemini, Gmail, etc.)
├── src/main/resources/
│   ├── application.properties
│   └── static/          # Frontend (HTML, CSS, JS)
└── pom.xml
```


## Working
<img width="1851" height="817" alt="image" src="https://github.com/user-attachments/assets/312054a6-324e-409a-83ec-f71ab9b9881c" />

<img width="1887" height="879" alt="image" src="https://github.com/user-attachments/assets/a9ce5b79-93d2-4f53-8444-daf5272557f0" />

<img width="1879" height="889" alt="image" src="https://github.com/user-attachments/assets/e5b641d3-ed4f-4e05-9fc4-ea9a2cb6bdd9" />

<img width="1883" height="587" alt="image" src="https://github.com/user-attachments/assets/066f5227-53c1-48fc-871d-2f3b9d9d8909" />

<img width="1564" height="698" alt="image" src="https://github.com/user-attachments/assets/c01cba38-6947-4d28-9bec-7a0688069a08" />

