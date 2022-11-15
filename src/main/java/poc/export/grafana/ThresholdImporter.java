package poc.export.grafana;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.resources.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class ThresholdImporter {

    public List<MetricData> importThresholds(String config) {
        JSONObject configJSON = new JSONObject(config);
        JSONArray thresholds = configJSON.getJSONObject("grafana").getJSONArray("thresholds");
        List<MetricData> data = new LinkedList<>();

        for(int i = 0; i < thresholds.length(); i++) {
            JSONObject threshold = thresholds.getJSONObject(i);
            String type = threshold.getString("type");
            String unit = threshold.getString("unit");
            double value = threshold.getDouble("value");
            Attributes attributes = Attributes.of(stringKey("type"), type);
            long epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
            MetricData metricData = this.createDoubleGaugeData(unit, attributes, value, epochNanos);
            data.add(metricData);
        }
        return data;
    }

    private MetricData createDoubleGaugeData(String unit, Attributes attributes, double metric, long epochNanos) {
        return ImmutableMetricData.createDoubleGauge(
                Resource.empty(),
                InstrumentationScopeInfo.empty(),
                "thresholds",
                "DoubleGauge",
                unit,
                ImmutableGaugeData.create(Collections.singletonList(
                        ImmutableDoublePointData.create(
                                epochNanos, epochNanos, attributes, metric)))
        );
    }
}