package poc.export;

import com.opencsv.exceptions.CsvException;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import poc.export.csv.CSVImporter;
import poc.export.json.JSONImporter;

import java.io.IOException;
import java.util.List;

@Component
public class OTExporter {

    @Autowired
    private CSVImporter csvImporter;
    @Autowired
    private JSONImporter jsonImporter;
    @Value("${otel.host:localhost}")
    private String host;

    private OtlpHttpMetricExporter exporter;

    public void export(String outputPath) throws IOException, CsvException {
        this.buildExporter();
        if(outputPath.endsWith(".json")) this.exportJSONMetricData(outputPath);
        else this.exportCSVMetricData(outputPath);
    }

    private void exportCSVMetricData(String csvPath) throws IOException, CsvException {
        List<MetricData> metrics = csvImporter.importMetricData(csvPath);
        exporter.export(metrics);
    }

    private void exportJSONMetricData(String jsonPath) throws IOException {
        List<MetricData> metrics = jsonImporter.importMetricData(jsonPath);
        exporter.export(metrics);
    }

    private void buildExporter() {
        this.exporter = OtlpHttpMetricExporter
                .builder()
                .setEndpoint("http://" + host +  ":4318/v1/metrics")
                .build();
    }
}