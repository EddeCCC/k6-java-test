package poc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TestConfig {

    @Value("${test.type}")
    private String type;

    @Value("${test.loop.max}")
    private int maxLoop;

    public String getType() { return this.type; }

    public int getMaxLoop() { return this.maxLoop; }
}