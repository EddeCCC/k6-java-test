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

        long epochNanosEnd = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());

        List<MetricData> metrics = new LinkedList<>();

        for(int i = 0; i < thresholds.length(); i++) {
            String threshold = thresholds.getString(i);
            String aggregation = threshold.replaceAll("(?=<=|<|>|>=|!=|==)([^=].*)", "").trim();
            String valueString = threshold.replaceAll("(.*)(?<=<=|<|>|>=|!=|==)", "").trim();
            double value = Double.parseDouble(valueString);
            Attributes attributes = Attributes.builder()
                    .put(stringKey("threshold_type"), thresholdType)
                    .put(stringKey("aggregation"), aggregation)
                    .build();

            MetricData metricStart = gaugeCreater.createDoubleGaugeData(name, unit, attributes, value, startEpochNanos);
            MetricData metricEnd = gaugeCreater.createDoubleGaugeData(name, unit, attributes, value, epochNanosEnd);
            metrics.add(metricStart);
            metrics.add(metricEnd);
        }

        return metrics;
    }

    private long getStartEpochNanos(List<JSONObject> allResults) {
        String time = allResults.stream()
                .filter(result -> result.getString("type").equals("Point"))
                .findFirst().get()
                .getJSONObject("data")
                .getString("time");

        OffsetDateTime odt = OffsetDateTime.parse(time);
        long timestamp = odt.toEpochSecond();
        long epochNanos =  TimeUnit.SECONDS.toNanos(timestamp);

        return epochNanos;
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