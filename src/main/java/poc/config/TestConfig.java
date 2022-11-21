package poc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import poc.loadtest.exception.UnknownOutputTypeException;

@Component
public class TestConfig {

    @Value("${test.output:json}")
    private String outputType;
    @Value("${test.breakpoint:false}")
    private boolean breakpointConfig;
    @Value("${test.loops:1}")
    private int loops;

    public String getOutputType() { return this.outputType; }

    public boolean getBreakpointConfig() { return this.breakpointConfig; }

    public int getMaxLoops() {
        if(this.loops < 1) return 1;
        else return this.loops;
    }
}