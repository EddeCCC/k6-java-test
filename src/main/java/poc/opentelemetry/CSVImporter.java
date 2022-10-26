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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CSVImporter {

    @Autowired
    private OTDataCreater dataCreater;

    @SneakyThrows
    public List<MetricData> importMetricData(String filePath) {
        CSVReader reader = this.createCSVReader(filePath);
        List<String[]> csv = reader.readAll();
        List<MetricData> data = new LinkedList<>();

        List<String[]> durations = csv.stream()
                .filter(row -> row[0].startsWith("http_") || row[0].equals("iteration_duration"))
                .toList();
        List<String[]> checks = csv.stream()
                .filter(row -> row[0].equals("checks"))
                .toList();
        List<String[]> amounts = csv.stream()
                .filter(row -> row[0].startsWith("vus") || row[0].equals("iterations"))
                .toList();
        List<String[]> bytes = csv.stream()
                .filter(row -> row[0].startsWith("data_"))
                .toList();

        List<MetricData> durationData = dataCreater.createMetricData(durations, "durations", "ms");
        List<MetricData> checkData = dataCreater.createMetricData(checks, "checks", "boolean");
        List<MetricData> amountData = dataCreater.createMetricData(amounts, "amounts", "");
        List<MetricData> byteData = dataCreater.createMetricData(bytes, "bytes", "bytes");

        Stream.of(durationData, checkData, amountData, byteData).forEach(data::addAll);

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