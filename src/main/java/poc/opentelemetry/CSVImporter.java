package poc.opentelemetry;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

@Component
public class CSVImporter {

    @Autowired
    private OTDataCreater dataCreater;

    @SneakyThrows
    public List<MetricData> importMetricData(String filePath) {
        CSVReader reader = this.createCSVReader(filePath);
        List<MetricData> data = new LinkedList<>();
        String[] line;

        while( (line = reader.readNext() ) != null) {
            String key = line[0] + " | " + line[3] + " | " + line[8] + " | " + line[16];
            String value = "metric_value";
            double metric = Double.parseDouble(line[2]);

            MetricData singleMetric = dataCreater.createMetricData(key, value, metric);
            data.add(singleMetric);
        }
        reader.close();
        return data;
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