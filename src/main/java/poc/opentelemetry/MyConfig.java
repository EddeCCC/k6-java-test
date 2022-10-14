package poc.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;


import java.time.Duration;
import java.util.concurrent.TimeUnit;


public class MyConfig {

    static OpenTelemetry initOpenTelemetry() {
        // Include required service.name resource attribute on all spans and metrics
        Resource resource =
                Resource.getDefault()
                        .merge(Resource.builder().put("SERVICE_NAME", "OtlpExporterExample").build());

        OpenTelemetrySdk openTelemetrySdk =
                OpenTelemetrySdk.builder()
                        .setTracerProvider(
                                SdkTracerProvider.builder()
                                        .setResource(resource)
                                        .addSpanProcessor(
                                                BatchSpanProcessor.builder(
                                                                OtlpGrpcSpanExporter.builder()
                                                                        .setTimeout(2, TimeUnit.SECONDS)
                                                                        .build())
                                                        .setScheduleDelay(100, TimeUnit.MILLISECONDS)
                                                        .build())
                                        .build())

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