package poc.opentelemetry;

import com.opencsv.exceptions.CsvValidationException;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.config.PathConfig;

import java.io.IOException;

@Component
public final class OTRecorder {

    @Autowired
    private CSVImporter csvReader;

    private ObservableDoubleGauge doubleGauge;
    private final OpenTelemetry openTelemetry = OTConfig.initOpenTelemetry();

    public void record(String csvPath) {
        System.out.println("### START RECORDING OUTPUT ###");
        Meter meter = openTelemetry.getMeter("k6.csv.ouput");
        DoubleGaugeBuilder dgb = meter.gaugeBuilder("csv_results");

        this.doubleGauge = dgb.buildWithCallback(measurement ->
            csvReader.importOutput(measurement, csvPath)
        );
    }

    public void close() {
        doubleGauge.close();
        System.out.println("### RECORDER CLOSED ###");
    }
}