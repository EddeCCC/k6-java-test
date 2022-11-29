package poc.export.json;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.export.metric.GaugeCreater;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class ThresholdImporter {

    @Autowired
    private JSONMetricCreaterHelper helper;
    @Autowired
    private GaugeCreater gaugeCreater;

    public List<MetricData> importThreshold(List<JSONObject> allResults) {
        List<JSONObject> thresholdData = allResults.stream()
                .filter(result -> result.getString("type").equals("Metric"))
                .map(result -> result.getJSONObject("data"))
                .filter(data -> data.getJSONArray("thresholds").length() > 0).toList();

        if(thresholdData.isEmpty()) return Collections.emptyList();

        List<MetricData> metrics = new LinkedList<>();
        long startEpochNanos = this.getStartEpochNanos(allResults);

        for(JSONObject data : thresholdData) {
            List<MetricData> metric = this.createThresholdMetric(data, startEpochNanos);
            metrics.addAll(metric);
        }
        return metrics;
    }

    private List<MetricData> createThresholdMetric(JSONObject data, long startEpochNanos) {
        String name = "thresholds";
        String type = data.getString("type");
        String unit = this.getUnit(type);
        String thresholdType = data.getString("name");
        JSONArray thresholds = data.getJSONArray("thresholds");
        long endEpochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        List<MetricData> metrics = new LinkedList<>();

        for(int i = 0; i < thresholds.length(); i++) {
            Object thresholdObject = thresholds.get(i);

            String threshold = "";
            if(thresholdObject instanceof JSONObject) threshold = ((JSONObject) thresholdObject).getString("threshold");
            else if(thresholdObject instanceof String) threshold = thresholdObject.toString();

            //remove logical operator and everything after
            String aggregation = threshold.replaceAll("(?=<=|<|>|>=|!=|==)([^=].*)", "").trim();
            //remove logical operator and everything before
            String valueString = threshold.replaceAll("(.*)(?<=<=|<|>|>=|!=|==)", "").trim();

            if(aggregation.isEmpty() || valueString.isEmpty()) continue;

            double value = Double.parseDouble(valueString);
            Attributes attributes = Attributes.builder()
                    .put(stringKey("threshold_type"), thresholdType)
                    .put(stringKey("aggregation"), aggregation)
                    .build();

            //Create two DataPoints so a line can be drawn in visualization
            MetricData metricStart = gaugeCreater.createDoubleGaugeData(name, unit, attributes, value, startEpochNanos);
            MetricData metricEnd = gaugeCreater.createDoubleGaugeData(name, unit, attributes, value, endEpochNanos);
            metrics.add(metricStart);
            metrics.add(metricEnd);
        }
        return metrics;
    }

    /**
     * Get first timestamp in JSON list
     */
    private long getStartEpochNanos(List<JSONObject> allResults) {
        String time = allResults.stream()
                .filter(result -> result.getString("type").equals("Point"))
                .findFirst().get()
                .getJSONObject("data")
                .getString("time");

        return helper.getEpochNanos(time);
    }

    private String getUnit(String type) {
        return switch (type) {
            case "trend" -> "ms";
            case "counter", "gauge" -> "1";
            case "rate" -> "%";
            default -> throw new IllegalStateException("Unexpected threshold type: " + type);
        };
    }
}