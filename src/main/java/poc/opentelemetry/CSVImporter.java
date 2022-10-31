package poc.opentelemetry;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class CSVImporter {

    @Autowired
    private OTDataCreater dataCreater;

    public List<MetricData> importMetricData(String filePath) throws IOException, CsvException {
        CSVReader reader = this.createCSVReader(filePath);
        List<String[]> csv = reader.readAll();

        List<String[]> http_durations = csv.stream()
                .filter(row -> row[0].startsWith("http_req_")).toList();
        List<String[]> checks = csv.stream()
                .filter(row -> row[0].equals("checks")).toList();
        List<String[]> vusMax = csv.stream()
                .filter(row -> row[0].startsWith("vus_max")).toList();
        List<String[]> vusTrend = csv.stream()
                .filter(row -> row[0].equals("vus")).toList();

        List<MetricData> durationData = dataCreater.createHTTPRequestData(http_durations);
        List<MetricData> checkData = dataCreater.createAccuracyData(checks);
        List<MetricData> vusMaxData = dataCreater.createDoubleGaugeData(vusMax);
        List<MetricData> vusTrendData = dataCreater.createHistogramData(vusTrend);

        List<MetricData> data = new LinkedList<>();
        Stream.of(durationData, checkData, vusMaxData, vusTrendData).forEach(data::addAll);

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