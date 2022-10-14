package poc.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.*;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.resources.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class MyMetric {

    public void doStuff(OpenTelemetry openTelemetry) {
        OtlpHttpMetricExporter exporter = OtlpHttpMetricExporter.builder()
                .addHeader("Connection", "close")
                .build();


//        Meter meter = openTelemetry.getMeter("io.opentelemetry.example");
//        LongCounter counter = meter.counterBuilder("example_counter").build();
//        counter.add(1);
//        ObservableDoubleGauge o = meter
//                .gaugeBuilder("TEST")
//                .setUnit("ms")
//                .setDescription("description")
//                .buildWithCallback(measurement -> measurement.record(5.0, Attributes.empty()));

        List<MetricData> list = generateList();

        exporter.export(list);
    }

    static List<MetricData> generateList() {
        List<MetricData> data = new LinkedList<>();
        data.add(generateDoubleGaugeData());
        return data;
    }

    static MetricData generateDoubleGaugeData() {
        long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
        return ImmutableMetricData.createDoubleGauge(Resource.empty(),
                InstrumentationScopeInfo.empty(),
                "double_gauge_test",
                "description",
                "1",
                ImmutableGaugeData.create(Collections.singletonList(
                        ImmutableDoublePointData.create(
                                startNs, endNs, Attributes.of(stringKey("k"), "v"), 1))));
    }
}