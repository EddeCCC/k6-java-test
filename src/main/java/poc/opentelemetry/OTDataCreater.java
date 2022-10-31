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
                double metric = Double.parseDouble(row[2]);
                String method = row[8];
                String url = row[16];
                String id = Integer.toString(idCounter);
                MetricData metricData = this.createRequestHistogram(name, method, url, id, metric, timestamp);
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

        MetricData metricData = this.createDoubleGauge(name, timestamp, attributes, accuracy);

        return Collections.singletonList(metricData);
    }

    public List<MetricData> createDoubleGaugeData(List<String[]> csv) {
        String name = "vus_max";
        long timestamp = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        Attributes attributes = Attributes.of(stringKey("amount"), "value");

        String[] vus_max = csv.get(0);
        double metric = Double.parseDouble(vus_max[2]);

        MetricData metricData = this.createDoubleGauge(name, timestamp, attributes, metric);

        return Collections.singletonList(metricData);
    }

    public List<MetricData> createHistogramData(List<String[]> csv) {
        List<MetricData> data = new LinkedList<>();
        String name = "vus";
        int idCounter = 1;

        for(String[] row : csv) {
            long timestamp = Long.parseLong(row[1]);
            double metric = Double.parseDouble(row[2]);
            String id = Integer.toString(idCounter);
            MetricData metricData = this.createDoubleHistogram(name, id, metric, timestamp);
            data.add(metricData);
        }
        return data;
    }

    private MetricData createRequestHistogram(
            String name, String method, String url,  String id, double metric, long timestamp) {

        Attributes attributes = Attributes.builder()
                .put(stringKey("endpoint"), url)
                .put(stringKey("http_method"), method)
                .put(stringKey("vu_ID"), id)
                .build();
        //Requirement: counts.size = boundaries.size + 1
        List<Double> boundaries = Collections.emptyList();
        List<Long> counts = Collections.singletonList(1L);

        ImmutableHistogramPointData pointData = ImmutableHistogramPointData.create(
                timestamp,
                timestamp,
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

    private MetricData createDoubleHistogram(String name, String id, double metric, long timestamp) {
        Attributes attributes = Attributes.of(stringKey("counter"), id);
        //Requirement: counts.size = boundaries.size + 1
        List<Double> boundaries = Collections.emptyList();
        List<Long> counts = Collections.singletonList(1L);

        ImmutableHistogramPointData pointData = ImmutableHistogramPointData.create(
                timestamp,
                timestamp,
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

    private MetricData createDoubleGauge(String name, long timestamp, Attributes attributes, double metric) {
        return ImmutableMetricData.createDoubleGauge(
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                name,
                "Double gauge",
                "1",
                ImmutableGaugeData.create(Collections.singletonList(
                        ImmutableDoublePointData.create(
                                timestamp, timestamp, attributes, metric)))
        );
    }
}