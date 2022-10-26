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

    public List<MetricData> createMetricData(List<String[]> rows, String key, String unit) {
        List<MetricData> data = new LinkedList<>();
        for (String[] row : rows) {
            String value = row[0] + "_" + row[3] + "_" + row[8];
            double metric = Double.parseDouble(row[2]);
            long timestamp = Long.parseLong(row[1]);

            MetricData singleMetric = this.createSingleMetricData(key, value, metric, timestamp, unit);
            data.add(singleMetric);
        }
        return data;
    }

    private MetricData createSingleMetricData(String key, String value, double metric, long timestamp, String unit) {
        //long timestamp = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        long endNs = timestamp + TimeUnit.MILLISECONDS.toNanos(900);

        return ImmutableMetricData.createDoubleGauge(Resource.empty(),
                InstrumentationScopeInfo.empty(),
                "k6_csv_output",
                "Result metrics after load test",
                unit,
                ImmutableGaugeData.create(Collections.singletonList(
                        ImmutableDoublePointData.create(
                                timestamp, endNs, Attributes.of(stringKey(key), value), metric))));
    }
}