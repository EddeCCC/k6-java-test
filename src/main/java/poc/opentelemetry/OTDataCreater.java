package poc.opentelemetry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
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
public class OTDataCreater {

    public List<MetricData> createMetricData(List<String[]> rows, String name, String unit) {
        List<MetricData> data = new LinkedList<>();
        for (String[] row : rows) {
            String key = row[0] + "_" + row[3] + "_" + row[8];
            String url = row[9];
            double metric = Double.parseDouble(row[2]);
            long timestamp = Long.parseLong(row[1]);

            MetricData singleMetric = this.createSingleMetricData(name, unit, key, url, metric, timestamp);
            data.add(singleMetric);
        }
        return data;
    }

    private MetricData createSingleMetricData(
            String name,String unit, String key, String value, double metric, long timestamp) {

        return ImmutableMetricData.createDoubleGauge(Resource.empty(),
                InstrumentationScopeInfo.empty(),
                name,
                "k6-result-metric",
                unit,
                ImmutableGaugeData.create(Collections.singletonList(
                        ImmutableDoublePointData.create(
                                timestamp, timestamp, Attributes.of(stringKey(key), value), metric))));
    }
}