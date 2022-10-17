package poc.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class OTRecorder {

    @Autowired
    private CSVImporter csvReader;
    @Autowired
    private OTConfig otConfig;

    private ObservableDoubleGauge doubleGauge;

    public void record(String csvPath) {
        System.out.println("### START RECORDING OUTPUT ###");
        OpenTelemetry openTelemetry = otConfig.initOpenTelemetry();
        Meter meter = openTelemetry.getMeter("k6.output");
        DoubleGaugeBuilder gaugeBuilder = meter.gaugeBuilder("CSV_RESULTS");

        //How does this work??? //The collector receives nothing...
        //ObservableDoubleMeasurement odm = gaugeBuilder.buildObserver();
        //csvReader.importFile(odm, csvPath);

        this.doubleGauge = gaugeBuilder.buildWithCallback(measurement ->
            csvReader.importFile(measurement, csvPath)
        );
    }

    public void close() {
        doubleGauge.close();
        System.out.println("### RECORDER CLOSED ###");
    }
}