package poc.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.data.*;
import io.opentelemetry.sdk.resources.Resource;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class OTDataCreater {

    public List<MetricData> createHTTPRequestData(List<String[]> csv) {
        List<MetricData> data = new LinkedList<>();
        Map<String, List<String[]>> map = csv.stream().collect(Collectors.groupingBy(row -> row[0]));

        for(Map.Entry<String, List<String[]>> entry : map.entrySet()) {
            List<String[]> rows = entry.getValue();
            int idCounter = 1;

            for (String [] row : rows) {
                String name = row[0];
                long timestamp = Long.parseLong(row[1]);
                long epochNanos = TimeUnit.SECONDS.toNanos(timestamp);
                double metric = Double.parseDouble(row[2]);
                String method = row[8];
                String url = row[16];
                String id = Integer.toString(idCounter);
                MetricData metricData = this.createRequestGauge(name, url, method, id, metric, epochNanos);
                data.add(metricData);
                idCounter++;
            }
        }
        return data;
    }

    public List<MetricData> createAccuracyData(List<String[]> csv) {
        String name = "checks";
        long timestamp = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        Attributes attributes = Attributes.of(stringKey("accuracy"), "value");

        double sum = csv.stream()
                .map(row -> Double.parseDouble(row[2]))
                .reduce(0.0, Double::sum);
        int count = csv.size();
        double accuracy =  sum / count;

        MetricData metricData = this.createDoubleGauge(name, attributes, accuracy, timestamp);

        return Collections.singletonList(metricData);
    }

    public List<MetricData> createDoubleGaugeData(List<String[]> csv, String name) {
        long timestamp = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        Attributes attributes = Attributes.of(stringKey("amount"), "value");

        String[] vus_max = csv.get(0);
        double metric = Double.parseDouble(vus_max[2]);
        MetricData metricData = this.createDoubleGauge(name, attributes, metric, timestamp);

        return Collections.singletonList(metricData);
    }

    public List<MetricData> createHistogramData(List<String[]> csv, String name) {
        List<MetricData> data = new LinkedList<>();
        int idCounter = 0;

        for(String[] row : csv) {
            long timestamp = Long.parseLong(row[1]);
            long epochNanos = TimeUnit.SECONDS.toNanos(timestamp);
            double metric = Double.parseDouble(row[2]);
            String id = Integer.toString(idCounter);
            MetricData metricData = this.createDoubleHistogram(name, id, metric, epochNanos);
            data.add(metricData);
            idCounter++;
        }
        return data;
    }

    private MetricData createRequestGauge(String name, String url, String method, String id, double metric, long epochNanos) {
        Attributes attributes = Attributes.builder()
                .put(stringKey("endpoint"), url)
                .put(stringKey("http_method"), method)
                .put(stringKey("vu_ID"), id)
                .build();

        return ImmutableMetricData.createDoubleGauge(
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                name,
                "k6-result-metric",
                "ms",
                ImmutableGaugeData.create(Collections.singletonList(
                        ImmutableDoublePointData.create(
                                epochNanos, epochNanos, attributes, metric))));
    }

    @Deprecated
    private MetricData createRequestHistogram(
            String name, String method, String url,  String id, double metric, long epochNanos) {

        Attributes attributes = Attributes.builder()
                .put(stringKey("endpoint"), url)
                .put(stringKey("http_method"), method)
                .put(stringKey("vu_ID"), id)
                .build();
        //Requirement: counts.size = boundaries.size + 1
        List<Double> boundaries = Collections.emptyList();
        List<Long> counts = Collections.singletonList(1L);

        ImmutableHistogramPointData pointData = ImmutableHistogramPointData.create(
                epochNanos,
                epochNanos,
                attributes,
                metric, null, null,
                boundaries,
                counts
        );

        return ImmutableMetricData.createDoubleHistogram(
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                name,
                "k6-result-metric",
                "ms",
                ImmutableHistogramData.create(AggregationTemporality.CUMULATIVE, Collections.singletonList(pointData))
        );
    }

    private MetricData createDoubleHistogram(String name, String id, double metric, long epochNanos) {
        Attributes attributes = Attributes.of(stringKey("counter"), id);
        //Requirement: counts.size = boundaries.size + 1
        List<Double> boundaries = Collections.emptyList();
        List<Long> counts = Collections.singletonList(1L);

        ImmutableHistogramPointData pointData = ImmutableHistogramPointData.create(
                epochNanos,
                epochNanos,
                attributes,
                metric, null, null,
                boundaries,
                counts
        );
        return ImmutableMetricData.createDoubleHistogram(
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                name,
                "Double histogram",
                "1",
                ImmutableHistogramData.create(AggregationTemporality.CUMULATIVE,
                        Collections.singletonList(pointData))
        );
    }

    private MetricData createDoubleGauge(String name, Attributes attributes, double metric, long epochNanos) {
        return ImmutableMetricData.createDoubleGauge(
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                name,
                "Double gauge",
                "1",
                ImmutableGaugeData.create(Collections.singletonList(
                        ImmutableDoublePointData.create(
                                epochNanos, epochNanos, attributes, metric)))
        );
    }
}