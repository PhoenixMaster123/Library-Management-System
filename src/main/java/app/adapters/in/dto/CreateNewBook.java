package app.adapters.in.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CreateNewBook {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "ISBN is required")
    private String isbn;
    @Min(value = 1000, message = "Year must be valid")
    @Max(value = 9999, message = "Year must be valid")
    private int publicationYear;
    private List<CreateNewAuthor> authors;
}
