package poc.opentelemetry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Scope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import poc.JavaK6Application;

import java.util.Random;

@RestController
public class TelemetryController {

    private final OpenTelemetry openTelemetry = MyConfig.initOpenTelemetry();

    @Autowired
    private MyMetric metric;

    @Autowired
    public TelemetryController() {
        Meter meter = openTelemetry.getMeter("MY_METER_k6");
    }


    @GetMapping("/test")
    public String test()  {
        metric.doStuff(openTelemetry);
        return "STUFF DONE";
    }

    @Autowired
    private MyExample example;

    @GetMapping("/example")
    public String example() throws InterruptedException {
        example.doExample(openTelemetry);
        return "DONE";
    }
}