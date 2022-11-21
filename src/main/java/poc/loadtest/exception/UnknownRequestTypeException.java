package poc.loadtest.exception;

public class UnknownRequestTypeException extends RuntimeException {

    public UnknownRequestTypeException(String type) { super("Unknown request type: " + type); }
}
