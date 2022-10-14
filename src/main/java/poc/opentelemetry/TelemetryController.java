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

    private static final Logger LOGGER = LogManager.getLogger(JavaK6Application.class);
    private final AttributeKey<String> ATTR_METHOD = AttributeKey.stringKey("method");

    private final Tracer tracer;
    private final Random random = new Random();
    private final LongHistogram doWorkHistogram;

     // it is important to initialize your SDK as early as possible in your application's lifecycle
     private final OpenTelemetry openTelemetry = MyConfig.initOpenTelemetry();

    @Autowired
    private MyMetric metric;

    @Autowired
    public TelemetryController() {
        tracer = openTelemetry.getTracer(JavaK6Application.class.getName());
        Meter meter = openTelemetry.getMeter(JavaK6Application.class.getName());
        doWorkHistogram = meter.histogramBuilder("do-work").ofLongs().build();
    }

    @GetMapping("/ping")
    public String ping() throws InterruptedException {
        int sleepTime = random.nextInt(200);
        doWork(sleepTime);
        doWorkHistogram.record(sleepTime, Attributes.of(ATTR_METHOD, "ping"));
        return "pong";
    }

    private void doWork(int sleepTime) throws InterruptedException {
        Span span = tracer.spanBuilder("doWork").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            Thread.sleep(sleepTime);
            LOGGER.info("A sample log message!");
        } finally {
            span.end();
        }
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