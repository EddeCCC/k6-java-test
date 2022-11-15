package poc.export;

import com.opencsv.exceptions.CsvException;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import poc.export.csv.CSVImporter;
import poc.export.grafana.ThresholdImporter;

import java.io.IOException;
import java.util.List;

@Component
public class OTExporter {

    @Autowired
    private CSVImporter csvImporter;
    @Autowired
    private ThresholdImporter thresholdImporter;
    @Value("${otel.host}")
    private String host;

    private OtlpHttpMetricExporter exporter;

    public void export(String csvPath, String config) throws IOException, CsvException {
        this.buildExporter();
        this.exportMetricData(csvPath);
        if(config != null) this.exportThreshold(config);
    }

    private void exportMetricData(String csvPath) throws IOException, CsvException {
        List<MetricData> metrics = csvImporter.importMetricData(csvPath);
        exporter.export(metrics);
    }

    private void exportThreshold(String config) {
        List<MetricData> thresholds = thresholdImporter.importThresholds(config);
        exporter.export(thresholds);
    }

    private void buildExporter() {
        this.exporter = OtlpHttpMetricExporter
                .builder()
                .setEndpoint("http://" + host +  ":4318/v1/metrics")
                .build();
    }
}