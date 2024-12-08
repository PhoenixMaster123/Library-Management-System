package app.adapters.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateNewAuthor {
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}
