package poc.export.csv;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.export.metric.GaugeCreater;
import poc.export.metric.ResultType;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class CSVMetricCreater {

    @Autowired
    private CSVMetricCreaterHelper helper;
    @Autowired
    private GaugeCreater gaugeCreater;

    public List<MetricData> createRequestMetric(List<String[]> csv, String unit) {
        List<MetricData> data = new LinkedList<>();
        Map<String, List<String[]>> groupedRequests = csv.stream().collect(Collectors.groupingBy(row -> row[0]));

        for(Map.Entry<String, List<String[]>> entry : groupedRequests.entrySet()) {
            List<String[]> rows = entry.getValue();

            for (String [] row : rows) {
                String name = row[0];
                String url = row[16];
                String method = row[8];
                Attributes attributes = Attributes.builder()
                        .put(stringKey("endpoint"), url)
                        .put(stringKey("http_method"), method)
                        .build();

                double metric = Double.parseDouble(row[2]);
                long timestamp = Long.parseLong(row[1]);
                long epochNanos = TimeUnit.SECONDS.toNanos(timestamp);

                MetricData metricData = gaugeCreater.createDoubleGaugeData(name, unit, attributes, metric, epochNanos);
                data.add(metricData);
            }
        }
        return data;
    }

    public List<MetricData> createGaugeMetricList(List<String[]> csv, String name, String unit) {
        List<MetricData> data = new LinkedList<>();

        for(String[] row : csv) {
            String url = row[16];
            Attributes attributes = Attributes.empty();
            if(!url.isEmpty()) attributes = Attributes.of(stringKey("endpoint"), url);

            double metric = Double.parseDouble(row[2]);
            long timestamp = Long.parseLong(row[1]);
            long epochNanos = TimeUnit.SECONDS.toNanos(timestamp);

            MetricData metricData = gaugeCreater.createDoubleGaugeData(name, unit, attributes, metric, epochNanos);
            data.add(metricData);
        }
        return data;
    }

    public List<MetricData> createSingleGaugeMetric(List<String[]> csv, ResultType type) {
        if(csv.isEmpty()) return Collections.emptyList();

        long timestamp = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        String name = type.toString().toLowerCase();
        Attributes attributes = Attributes.empty();
        double metric = 0.0;
        String unit = "1";

        switch (type) {
            case MAX_LOAD -> metric = helper.getMaxLoad(csv);
            case ITERATIONS, HTTP_REQS -> metric = helper.getAmount(csv);
            case CHECKS -> {
                metric = helper.getAverage(csv);
                unit = "%";
            }
            case ITERATION_DURATION -> {
                metric = helper.getAverage(csv);
                unit = "ms";
            }
        }

        MetricData metricData = gaugeCreater.createDoubleGaugeData(name, unit, attributes, metric, timestamp);
        return Collections.singletonList(metricData);
    }
}