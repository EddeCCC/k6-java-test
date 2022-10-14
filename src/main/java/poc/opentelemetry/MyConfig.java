package poc.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;


import java.time.Duration;


public class MyConfig {

    static OpenTelemetry initOpenTelemetry() {
        Resource resource = Resource.getDefault();

        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                        .setMeterProvider(
                                SdkMeterProvider.builder()
                                        .setResource(resource)
                                        .registerMetricReader(
                                                PeriodicMetricReader.builder(OtlpHttpMetricExporter.getDefault())
                                                        .setInterval(Duration.ofMillis(1000))
                                                        .build())
                                        .build())
                        .buildAndRegisterGlobal();

        Runtime.getRuntime()
                .addShutdownHook(new Thread(openTelemetrySdk.getSdkTracerProvider()::shutdown));
        Runtime.getRuntime()
                .addShutdownHook(new Thread(openTelemetrySdk.getSdkMeterProvider()::shutdown));

        return openTelemetrySdk;
    }
}