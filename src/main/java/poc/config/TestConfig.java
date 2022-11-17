package poc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import poc.loadtest.exception.UnknownOutputTypeException;

@Component
public class TestConfig {

    @Value("${test.loops}")
    private int loops;
    @Value("${test.output}")
    private String outputType;

    public int getMaxLoops() {
        if(this.loops < 1) return 1;
        else return this.loops;
    }

    public String getOutputType() { return this.outputType; }
}