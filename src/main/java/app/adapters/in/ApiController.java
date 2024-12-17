package app.adapters.in;

import app.domain.services.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/hochschule")
    public ResponseEntity<String> getHochschuleData() {
        String endpoint = "https://hochschule-api.example.com/protected-data";

        try {
            logger.info("Fetching data from Hochschule API at endpoint: {}", endpoint);
            String data = apiService.fetchDataFromHochschuleApi(endpoint);
            logger.info("Successfully fetched data from Hochschule API.");
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            logger.error("Error fetching data from Hochschule API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching data: " + e.getMessage());
        }
    }
}


