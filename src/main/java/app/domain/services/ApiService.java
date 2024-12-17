package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    private final AuthService authService;

    public ApiService(AuthService authService) {
        this.authService = authService;
    }

    public String fetchDataFromHochschuleApi(String endpoint) {
        RestTemplate restTemplate = new RestTemplate();

        String token = authService.authenticateWithTHWS("username", "password");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            logger.info("Sending request to Hochschule API at endpoint: {}", endpoint);
            return restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class).getBody();
        } catch (Exception e) {
            logger.error("Failed to fetch data from API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch data: " + e.getMessage(), e);
        }
    }
}
