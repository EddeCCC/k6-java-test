package poc.loadtest.exception;

public class UnknownOutputTypeException extends RuntimeException {

    public UnknownOutputTypeException(String type) {
        super("Unknown output type: " + type);
    }
}
