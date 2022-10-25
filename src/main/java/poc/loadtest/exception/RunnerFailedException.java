package poc.loadtest.exception;

public class RunnerFailedException extends RuntimeException {

    public RunnerFailedException(String message) {
        super(message);
    }
}