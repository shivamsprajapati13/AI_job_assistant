# 🚀 ApplyAI – AI-Powered Job Application Assistant

> An AI-powered platform that automates the job application process by extracting information from hiring post screenshots, analyzing resume compatibility, generating personalized application emails, and sending them securely through Gmail.

---

## ✨ Features

### 📸 Screenshot Upload
Upload hiring posts from multiple platforms:
- LinkedIn
- X (Twitter)
- WhatsApp
- Telegram
- Slack
- Discord
- Any image containing a hiring post

---

### 🤖 AI Job Extraction

Gemini Vision analyzes the uploaded screenshot and extracts:

- Company Name
- Job Title
- Required Experience
- Required Skills
- Location
- Recruiter Email
- Application Instructions

---

### 📊 Resume Match Analysis

Compare your uploaded resume against the extracted job description.

Provides:

- Resume Match Score
- Missing Skills
- Matching Skills
- AI-generated Suggestions

---

### ✉️ AI Email Generation

Automatically generates personalized job application emails using your profile and resume.

Includes:

- Professional Subject Line
- Personalized Email Body
- Recruiter-specific formatting

---

### 📧 Gmail Integration

Secure Gmail OAuth integration for sending applications.

- OAuth 2.0 Authentication
- Resume Attachment
- Secure Email Delivery
- No Password Storage

---

### 📁 Application History

Maintain complete application history.

- Draft Applications
- Sent Applications
- Company Information
- Job Role
- Timestamp

---

## 🏗️ System Architecture

```text
                    Hiring Post Screenshot
                               │
                               ▼
                     Gemini Vision (OCR + AI)
                               │
                               ▼
                  Structured Job Information
                               │
        ┌──────────────────────┼──────────────────────┐
        ▼                      ▼                      ▼
 Resume Match          Email Generation       Application Storage
        │                      │                      │
        └──────────────────────┼──────────────────────┘
                               ▼
                      User Review & Edit
                               │
                               ▼
                     Gmail API (OAuth 2.0)
                               │
                               ▼
                       Application Sent
```

---

# 🛠️ Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Maven

## AI

- Google Gemini 2.0 Flash
- Gemini Vision

## Database

- MySQL

## Email

- Gmail API
- OAuth2

## Resume Parsing

- Apache PDFBox

## Frontend

- HTML
- CSS
- JavaScript

---

# 📦 Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+
- Google AI API Key
- Google OAuth Credentials

---

# ⚙️ Setup

## 1. Clone Repository

```bash
git clone https://github.com/<username>/apply-ai.git

cd apply-ai
```

---

## 2. Configure Environment Variables

Create a `.env`

```env
MYSQL_URL=jdbc:mysql://localhost:3306/jobassistant?createDatabaseIfNotExist=true

MYSQL_USER=root

MYSQL_PASSWORD=password

GEMINI_API_KEY=YOUR_API_KEY

GOOGLE_CLIENT_ID=YOUR_CLIENT_ID

GOOGLE_CLIENT_SECRET=YOUR_CLIENT_SECRET

DEFAULT_USER_EMAIL=example@gmail.com
```

---

## 3. Google OAuth Setup

1. Create a project in Google Cloud Console.
2. Enable Gmail API.
3. Create OAuth 2.0 Credentials.
4. Add Redirect URI

```
http://localhost:8600/login/oauth2/code/google
```

5. Copy Client ID and Client Secret.

---

## 4. Database

```sql
CREATE DATABASE jobassistant;
```

---

## 5. Run Application

```bash
./mvnw spring-boot:run
```

Open

```
http://localhost:8600
```

---

# 📖 Usage

### Step 1

Create your profile.

Upload:

- Resume
- Experience
- Skills
- Contact Details

---

### Step 2

Connect Gmail.

---

### Step 3

Upload a hiring post screenshot.

---

### Step 4

Review extracted information.

---

### Step 5

Analyze resume match.

---

### Step 6

Generate application email.

---

### Step 7

Review and Send.

---

### Step 8

Track application history.

---

# 🔌 REST APIs

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/extract` | Extract Job Details |
| POST | `/api/analyze-match` | Resume Match Analysis |
| POST | `/api/generate-email` | Generate Email |
| POST | `/api/applications/send` | Send Application |
| POST | `/api/applications/draft` | Save Draft |
| GET | `/api/applications/history` | Get History |
| GET / PUT | `/api/profile` | User Profile |
| POST | `/api/profile/resume` | Upload Resume |

---

# 📂 Project Structure

```
src
│
├── config
├── controller
├── dto
├── entity
├── repository
├── service
├── exception
├── util
│
└── resources
    ├── static
    └── application.properties
```

---

# 🚀 Future Enhancements

### 🤖 AI Resume Optimization

- Generate job-specific resumes
- ATS keyword optimization
- Reorder projects based on job relevance

---

### 📝 AI Cover Letter Generator

- Personalized cover letters
- Multiple writing styles
- Company-specific customization

---

### 📊 Advanced Resume Analysis

- Resume Match Dashboard
- Skill Gap Analysis
- AI Recommendations
- Missing Keywords Detection

---

### 🎯 AI Interview Preparation

- Generate interview questions
- Technical interview preparation
- Behavioral questions
- Learning resource recommendations

---

### 🌐 Browser Extension

- One-click application from LinkedIn
- Automatic job extraction
- Apply directly from browser

---

### 📄 Multi-format Support

Support extraction from:

- Screenshots
- PDFs
- Images
- Plain Text
- LinkedIn URLs
- Job Portal Links

---

### 📂 Resume Management

- Multiple resumes
- Resume versioning
- AI resume selection

---

### 📈 Analytics Dashboard

Track:

- Total Applications
- Interview Rate
- Offer Rate
- Response Rate
- Most Applied Skills
- Application Timeline

---

### 📬 Follow-up Automation

- AI-generated follow-up emails
- Reminder notifications
- Scheduled follow-ups

---

### 👤 Candidate Profile

Maintain:

- Skills
- Education
- Experience
- Preferred Locations
- Notice Period
- Portfolio
- LinkedIn
- GitHub

---

### 🔐 Authentication & Security

- JWT Authentication
- Multi-user Support
- Role-based Access Control
- Audit Logs

---

### ☁️ Deployment

- Docker
- Kubernetes
- GitHub Actions
- AWS
- GCP
- PostgreSQL Support

---

# 🛣️ Roadmap

- ✅ Screenshot Upload
- ✅ AI Job Extraction
- ✅ Resume Upload
- ✅ Resume Match Analysis
- ✅ AI Email Generation
- ✅ Gmail OAuth Integration
- ✅ Application History
- ✅ Multi AI Provider Support
- ⏳ AI Resume Optimization
- ⏳ Cover Letter Generation
- ⏳ Browser Extension
- ⏳ Interview Preparation
- ⏳ Analytics Dashboard
- ⏳ Follow-up Automation
- ⏳ JWT Authentication
- ⏳ Docker Deployment

---

# 🌟 Why ApplyAI?

ApplyAI demonstrates modern backend engineering by combining:

- Spring Boot REST APIs
- Google Gemini Vision
- Generative AI
- Gmail OAuth Integration
- Resume Parsing
- Database Design
- AI-powered Workflow Automation

It showcases how Large Language Models can automate real-world recruitment workflows while maintaining user control through a review and approval process.

---

# 📸 Demo

> Add screenshots or GIFs here.

Home Page
<img width="1851" height="817" alt="image" src="https://github.com/user-attachments/assets/312054a6-324e-409a-83ec-f71ab9b9881c" />

Upload Screenshot
<img width="1877" height="781" alt="image" src="https://github.com/user-attachments/assets/5f0c6fb7-17f5-41fb-aece-8884a31c76ac" />


AI Extraction
<img width="1539" height="617" alt="image" src="https://github.com/user-attachments/assets/d9572f3e-6198-4194-889d-8948a4c53375" />

Match Analysis

<img width="1887" height="879" alt="image" src="https://github.com/user-attachments/assets/a9ce5b79-93d2-4f53-8444-daf5272557f0" />

Generated Email

<img width="1879" height="889" alt="image" src="https://github.com/user-attachments/assets/e5b641d3-ed4f-4e05-9fc4-ea9a2cb6bdd9" />

<img width="1564" height="698" alt="image" src="https://github.com/user-attachments/assets/c01cba38-6947-4d28-9bec-7a0688069a08" />

Application History
<img width="1885" height="699" alt="image" src="https://github.com/user-attachments/assets/11dde59c-ad3b-4024-bb7f-986c1c17309f" />

---

# 📄 License

This project is licensed under the MIT License.

---

⭐ If you found this project useful, consider giving it a star!
