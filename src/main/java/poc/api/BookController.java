package poc.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookShelf;

    @GetMapping(path = "/all", produces = "application/json")
    public ResponseEntity<Map<Long,Book>> getBooks() {
        Map<Long,Book> allBooks = bookShelf.getBooks();
        return new ResponseEntity<>(allBooks, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<Book> getBook(@PathVariable(value = "id") Long id) {
        Book book = bookShelf.getBook(id);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @PostMapping(path = "/new", produces = "application/json")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        boolean response = bookShelf.addBook(book);
        if(response) return new ResponseEntity<>(book, HttpStatus.CREATED);
        else return new ResponseEntity<>(book, HttpStatus.CONFLICT);
    }

    @PutMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<Book> putBook(@RequestBody Book book, @PathVariable(value = "id") Long id) {
        boolean response = bookShelf.putBook(book, id);
        if(response) return new ResponseEntity<>(book, HttpStatus.OK);
        else return new ResponseEntity<>(book, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<Book> deleteBook(@PathVariable(value = "id") Long id) {
        try {
            Book deletedBook = bookShelf.deleteBook(id);
            return new ResponseEntity<>(deletedBook, HttpStatus.NO_CONTENT);
        } catch (NullPointerException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}