package poc.loadtest.exception;

public class InvalidConfigurationException extends RuntimeException {

    public InvalidConfigurationException() {
        super("Invalid Configuration file");
    }
}