package ai_assistant_job_analyzer.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailService {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public void sendEmail(Authentication auth, String to, String subject, String body,
                          byte[] attachment, String attachmentName) throws Exception {
        OAuth2AccessToken accessToken = getAccessToken(auth);
        Gmail gmail = buildGmailClient(accessToken.getTokenValue());

        String rawEmail = buildRawEmail(to, subject, body, attachment, attachmentName);
        Message message = new Message();
        message.setRaw(Base64.getUrlEncoder().encodeToString(rawEmail.getBytes()));

        gmail.users().messages().send("me", message).execute();
        log.info("Email sent successfully to {}", to);
    }

    public boolean isGmailConnected(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", auth.getName());
            return client != null && client.getAccessToken() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private OAuth2AccessToken getAccessToken(Authentication auth) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", auth.getName());
        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("Gmail not connected. Please sign in with Google first.");
        }
        return client.getAccessToken();
    }

    private Gmail buildGmailClient(String accessToken) throws Exception {
        com.google.api.client.http.HttpRequestInitializer requestInitializer =
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken);

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer
        ).setApplicationName("Job Application Assistant").build();
    }

    private String buildRawEmail(String to, String subject, String body,
                                 byte[] attachment, String attachmentName) {
        String boundary = "jobapply_boundary_" + System.currentTimeMillis();
        String htmlBody = body.replace("\n", "<br>");

        StringBuilder raw = new StringBuilder();
        raw.append("To: ").append(to).append("\r\n");
        raw.append("Subject: ").append(subject).append("\r\n");
        raw.append("MIME-Version: 1.0\r\n");
        raw.append("Content-Type: multipart/mixed; boundary=").append(boundary).append("\r\n\r\n");

        raw.append("--").append(boundary).append("\r\n");
        raw.append("Content-Type: text/html; charset=utf-8\r\n\r\n");
        raw.append(htmlBody).append("\r\n");

        if (attachment != null && attachment.length > 0) {
            raw.append("--").append(boundary).append("\r\n");
            raw.append("Content-Type: application/octet-stream\r\n");
            raw.append("Content-Disposition: attachment; filename=\"").append(attachmentName).append("\"\r\n");
            raw.append("Content-Transfer-Encoding: base64\r\n\r\n");
            raw.append(Base64.getMimeEncoder(76, "\r\n".getBytes()).encodeToString(attachment));
            raw.append("\r\n");
        }

        raw.append("--").append(boundary).append("--\r\n");
        return raw.toString();
    }
}
