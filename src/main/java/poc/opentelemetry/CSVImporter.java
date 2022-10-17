package poc.opentelemetry;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.*;


import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Component
public class CSVImporter {

    @SneakyThrows
    public void importOutput(ObservableDoubleMeasurement obm, String csvPath) {
        CSVReader reader = this.createCSVReader(csvPath);
        String[] line;
        while( (line = reader.readNext() ) != null) {
            String row = line[0];
            String column = "metric_value";
            Double value = Double.parseDouble(line[2]);

            this.record(obm, value, row, column);
        }
    }

    private void record(ObservableDoubleMeasurement obm, double value, String row, String column) {
        obm.record(value, Attributes.of(stringKey(row), column));
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