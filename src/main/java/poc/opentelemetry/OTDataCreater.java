package poc.opentelemetry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.*;
import io.opentelemetry.sdk.resources.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.opentelemetry.csv.CSVResponseType;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class OTDataCreater {

    @Autowired
    private OTDataCreaterHelper helper;

    public List<MetricData> createRequestData(List<String[]> csv, String unit) {
        List<MetricData> data = new LinkedList<>();
        Map<String, List<String[]>> groupedRequests = csv.stream().collect(Collectors.groupingBy(row -> row[0]));

        for(Map.Entry<String, List<String[]>> entry : groupedRequests.entrySet()) {
            List<String[]> rows = entry.getValue();
            int idCounter = 1;

            for (String [] row : rows) {
                String name = row[0];
                String url = row[16];
                String method = row[8];
                String id = Integer.toString(idCounter);
                Attributes attributes = Attributes.builder()
                        .put(stringKey("endpoint"), url)
                        .put(stringKey("http_method"), method)
                        .put(stringKey("vu_ID"), id)
                        .build();

                double metric = Double.parseDouble(row[2]);
                long timestamp = Long.parseLong(row[1]);
                long epochNanos = TimeUnit.SECONDS.toNanos(timestamp);

                MetricData metricData = this.createDoubleGaugeData(name, unit, attributes, metric, epochNanos);
                data.add(metricData);
                idCounter++;
            }
        }
        return data;
    }

    public List<MetricData> createGaugeDataList(List<String[]> csv, String name, String unit) {
        List<MetricData> data = new LinkedList<>();
        int idCounter = 0;

        for(String[] row : csv) {
            String id = Integer.toString(idCounter);
            Attributes attributes = Attributes.of(stringKey("id"), id);

            double metric = Double.parseDouble(row[2]);
            long timestamp = Long.parseLong(row[1]);
            long epochNanos = TimeUnit.SECONDS.toNanos(timestamp);
            MetricData metricData = this.createDoubleGaugeData(name, unit, attributes, metric, epochNanos);
            data.add(metricData);
            idCounter++;
        }
        return data;
    }

    public List<MetricData> createSingleGaugeData(List<String[]> csv, CSVResponseType type) {
        long timestamp = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        String name = type.toString().toLowerCase();
        Attributes attributes = Attributes.empty();
        double metric = 0.0;
        String unit = "1";

        switch (type) {
            case VUS_MAX -> metric = helper.getVusMax(csv);
            case CHECKS -> metric = helper.getCheckAccuracy(csv);
            case ITERATIONS -> metric = helper.getIterations(csv);
        }

        MetricData metricData = this.createDoubleGaugeData(name, unit, attributes, metric, timestamp);
        return Collections.singletonList(metricData);
    }

    private MetricData createDoubleGaugeData(String name, String unit, Attributes attributes, double metric, long epochNanos) {
        return ImmutableMetricData.createDoubleGauge(
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                name,
                "DoubleGauge",
                unit,
                ImmutableGaugeData.create(Collections.singletonList(
                        ImmutableDoublePointData.create(
                                epochNanos, epochNanos, attributes, metric)))
        );
    }
}