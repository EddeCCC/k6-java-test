package poc.export.json;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.export.metric.GaugeCreater;
import poc.export.metric.ResultType;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class JSONMetricCreater {

    @Autowired
    private JSONMetricCreaterHelper helper;
    @Autowired
    private GaugeCreater gaugeCreater;

    public List<MetricData> createRequestMetric(List<JSONObject> results, String unit) {
        List<MetricData> data = new LinkedList<>();
        Map<String, List<JSONObject>> groupedResults = results.stream()
                .collect(Collectors.groupingBy( json -> json.getString("metric") ));

        for(Map.Entry<String, List<JSONObject>> entry : groupedResults.entrySet()) {
            List<JSONObject> resultGroup = entry.getValue();

            for(JSONObject result : resultGroup) {
                String name = result.getString("metric");
                JSONObject dataObject = result.getJSONObject("data");
                String url = dataObject.getJSONObject("tags").getString("url");
                String method = dataObject.getJSONObject("tags").getString("method");
                Attributes attributes = Attributes.builder()
                        .put(stringKey("endpoint"), url)
                        .put(stringKey("http_method"), method)
                        .build();

                double metric = dataObject.getDouble("value");
                String time = dataObject.getString("time");
                long epochNanos = helper.getEpochNanos(time);

                MetricData metricData = gaugeCreater.createDoubleGaugeData(name, unit, attributes, metric, epochNanos);
                data.add(metricData);
            }
        }
        return data;
    }

    public List<MetricData> createGaugeMetricList(List<JSONObject> results, String name, String unit) {
        List<MetricData> data = new LinkedList<>();

        for(JSONObject result : results) {
            JSONObject dataObject = result.getJSONObject("data");
            Attributes attributes = Attributes.empty();
            if(!dataObject.isNull("tags") && dataObject.getJSONObject("tags").has("url")) {
                String url = dataObject.getJSONObject("tags").getString("url");
                attributes = Attributes.of(stringKey("endpoint"), url);
            }

            double metric = dataObject.getDouble("value");
            String time = dataObject.getString("time");
            long epochNanos = helper.getEpochNanos(time);

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
}