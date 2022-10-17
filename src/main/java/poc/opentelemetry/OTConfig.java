package poc.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.time.Duration;

@Component
public class OTConfig {

    @Value("${otel.host}")
    private String host;
    @Value("${otel.init}")
    private boolean shouldOtelInit;

    public OpenTelemetry initOpenTelemetry() {
        if (!shouldOtelInit) return null;

        Resource resource = Resource.getDefault();
        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                        .setMeterProvider(
                                SdkMeterProvider.builder()
                                        .setResource(resource)
                                        .registerMetricReader(
                                                PeriodicMetricReader.builder(OtlpHttpMetricExporter
                                                                .builder()
                                                                .setEndpoint("http://" + host +  ":4318/v1/metrics")
                                                                .build())
                                                        .setInterval(Duration.ofMillis(1000))
                                                        .build())
                                        .build())
                        .buildAndRegisterGlobal();

        Runtime.getRuntime()
                .addShutdownHook(new Thread(openTelemetrySdk.getSdkMeterProvider()::shutdown));

        return openTelemetrySdk;
    }
}