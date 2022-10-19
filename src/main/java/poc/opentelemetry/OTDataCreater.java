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
import java.util.concurrent.TimeUnit;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class OTDataCreater {

    public MetricData createMetricData(String key, String value, double metric) {
        long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
        long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);

        return ImmutableMetricData.createDoubleGauge(Resource.empty(),
                InstrumentationScopeInfo.empty(),
                "k6_csv_output",
                "Result metrics after load test",
                "undefined",
                ImmutableGaugeData.create(Collections.singletonList(
                        ImmutableDoublePointData.create(
                                startNs, endNs, Attributes.of(stringKey(key), value), metric))));
    }
}