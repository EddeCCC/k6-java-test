package poc.opentelemetry;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.*;


import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class CSVImporter {

    @SneakyThrows
    public void importFile(ObservableDoubleMeasurement obm, String filePath) {
        CSVReader reader = this.createCSVReader(filePath);
        String[] line;
        while( (line = reader.readNext() ) != null) {
            //Several lines in the CSV are identical, so there are sometimes multiple values for one key currently
            //metric_name + check + method + url
            String key = line[0] + " || " + line[3] + " || " + line[8] + " || " + line[16];
            String value = "metric_value";
            double metric = Double.parseDouble(line[2]);

            this.record(obm, metric, key, value);
        }
        reader.close();
    }

    private void record(ObservableDoubleMeasurement obm, double metric, String key, String value) {
        obm.record(metric, Attributes.of(stringKey(key), value));
    }

    private CSVReader createCSVReader(String path) throws FileNotFoundException {
        InputStream stream = new FileInputStream(path);
        Reader reader = new InputStreamReader(stream);

        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        return new CSVReaderBuilder(reader).withSkipLines(1).withCSVParser(parser).build();
    }
}