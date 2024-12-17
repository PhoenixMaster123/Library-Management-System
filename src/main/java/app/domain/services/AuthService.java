package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${hochschule.api.url}")
    private String apiUrl;

    public String authenticateWithTHWS(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();

        // Request body
        Map<String, String> requestBody = Map.of(
                "username", username,
                "password", password
        );

        try {
            logger.info("Authenticating with THWS API at URL: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestBody, String.class);
            logger.info("Authentication successful, token received.");
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Authentication failed with status: {}", e.getStatusCode(), e);
            throw new RuntimeException("Authentication failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during authentication: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred during authentication: " + e.getMessage(), e);
        }
    }
}
