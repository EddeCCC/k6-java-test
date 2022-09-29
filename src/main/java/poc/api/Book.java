package poc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    private Long id;

    private String name;

    private String author;

    private LocalDate releaseDate;

    public Book(String name, String author, LocalDate releaseDate) {
        this.name = name;
        this.author = author;
        this.releaseDate = releaseDate;
    }
}
