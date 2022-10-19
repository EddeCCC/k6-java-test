package poc.opentelemetry;

import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OTRecorder {

    @Autowired
    private CSVImporter csvReader;

    @Value("${otel.host}")
    private String host;

    public void record(String csvPath) {
        OtlpHttpMetricExporter exporter = OtlpHttpMetricExporter
                .builder()
                .setEndpoint("http://" + host +  ":4318/v1/metrics")
                .build();

        List<MetricData> data = csvReader.importMetricData(csvPath);
        exporter.export(data);
    }
}