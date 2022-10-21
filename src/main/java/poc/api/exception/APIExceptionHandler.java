package poc.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
public class APIExceptionHandler {

     @ExceptionHandler
     public ResponseEntity<String> handleIOException(IOException exception) {
          return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
     }

     @ExceptionHandler
     public ResponseEntity<String> handleNullPointerException(NullPointerException exception) {
          return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
     }
}