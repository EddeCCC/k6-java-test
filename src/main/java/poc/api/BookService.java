package poc.api;

import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
@Getter
@ToString
public class BookService {

    private Map<Long,Book> books = new HashMap<>();

    public BookService() {
        Book book1 = new Book(1L, "Fancy Stories", "Martin",LocalDate.of(2020,5,22));
        Book book2 = new Book(2L, "Long Stories", "Steve",LocalDate.of(2018,2,20));
        Book book3 = new Book(3L, "Short Stories", "Angela",LocalDate.of(2021,8,4));
        books.put(book1.getId(), book1);
        books.put(book2.getId(), book2);
        books.put(book3.getId(), book3);
    }

    public boolean addBook(Book book) {
        Long newKey = book.getId();
        if(books.get(newKey) != null) return false;
        books.put(newKey, book);
        return true;
    }

    public Book getBook(Long id) {
        return books.get(id);
    }

    public boolean putBook(Book book, Long id) {
        books.put(id, book);
        return true;
    }

    public Book deleteBook(Long id) {
        return books.remove(id);
    }
}