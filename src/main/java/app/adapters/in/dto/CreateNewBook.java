package app.adapters.in.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateNewBook {
    private String title;
    private String isbn;
    private int publicationYear;
}
