package poc.export.json;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.export.metric.DoubleGaugeCreater;
import poc.export.metric.ResultType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class JSONMetricCreater {

    @Autowired
    private JSONMetricCreaterHelper helper;
    @Autowired
    private DoubleGaugeCreater gaugeCreater;

    public List<MetricData> createRequestMetric(List<JSONObject> results, String unit) {
        List<MetricData> data = new LinkedList<>();
        Map<String, List<JSONObject>> groupedResults = results.stream()
                .collect(Collectors.groupingBy( json -> json.getString("metric") ));

        for(Map.Entry<String, List<JSONObject>> entry : groupedResults.entrySet()) {
            List<JSONObject> resultGroup = entry.getValue();

            for(JSONObject result : resultGroup) {
                String name = result.getString("metric");
                String url = result.getJSONObject("data").getJSONObject("tags").getString("url");
                String method = result.getJSONObject("data").getJSONObject("tags").getString("method");
                Attributes attributes = Attributes.builder()
                        .put(stringKey("endpoint"), url)
                        .put(stringKey("http_method"), method)
                        .build();

                double metric = result.getJSONObject("data").getDouble("value");
                String time = result.getJSONObject("data").getString("time");
                long epochNanos = this.getEpochNanos(time);

                MetricData metricData = gaugeCreater.createDoubleGaugeData(name, unit, attributes, metric, epochNanos);
                data.add(metricData);
            }
        }
        return data;
    }

    public List<MetricData> createGaugeMetricList(List<JSONObject> results, String name, String unit) {
        List<MetricData> data = new LinkedList<>();

        for(JSONObject result : results) {
            Attributes attributes = Attributes.empty();

            double metric = result.getJSONObject("data").getDouble("value");
            String time = result.getJSONObject("data").getString("time");
            long epochNanos = this.getEpochNanos(time);

            MetricData metricData = gaugeCreater.createDoubleGaugeData(name, unit, attributes, metric, epochNanos);
            data.add(metricData);
        }
        return data;
    }

    public List<MetricData> createSingleGaugeMetric(List<JSONObject> results, ResultType type) {
        if(results.isEmpty()) return Collections.emptyList();

        long timestamp = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        String name = type.toString().toLowerCase();
        Attributes attributes = Attributes.empty();
        double metric = 0.0;
        String unit = "1";

        switch (type) {
            case MAX_LOAD -> metric = helper.getMaxLoad(results);
            case ITERATIONS, HTTP_REQS -> metric = helper.getAmount(results);
            case CHECKS -> {
                metric = helper.getAverage(results);
                unit = "%";
            }
            case ITERATION_DURATION -> {
                metric = helper.getAverage(results);
                unit = "ms";
            }
        }

        MetricData metricData = gaugeCreater.createDoubleGaugeData(name, unit, attributes, metric, timestamp);
        return Collections.singletonList(metricData);
    }

    private long getEpochNanos(String time) {
        OffsetDateTime odt = OffsetDateTime.parse(time);
        long timestamp = odt.toEpochSecond();
        long epochNanos =  TimeUnit.SECONDS.toNanos(timestamp);

        return epochNanos;
    }
}