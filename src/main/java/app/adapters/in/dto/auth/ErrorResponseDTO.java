package app.adapters.in.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private int status;              // HTTP status code
    @NonNull
    private String message;          // Detailed error message
    private List<String> violations; // List of violations (optional)

    // Default constructor to handle empty violations list if not provided
    public ErrorResponseDTO(int status, String message) {
        this.status = status;
        this.message = message;
        this.violations = List.of();  // Default empty violations list
    }
}
