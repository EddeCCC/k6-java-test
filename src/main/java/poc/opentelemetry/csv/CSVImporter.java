package poc.opentelemetry.csv;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import poc.opentelemetry.OTDataCreater;

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
        reader.close();

        List<String[]> http_req = this.filterType(csv, "http_req_");
        List<String[]> checks = this.filterType(csv, "checks");
        List<String[]> vus_max = this.filterType(csv, "vus_max");
        List<String[]> vus_trend = this.filterType(csv, "vus");
        List<String[]> iterations = this.filterType(csv, "iterations");
        List<String[]> iteration_duration = this.filterType(csv, "iteration_duration");

        List<MetricData> requestData = dataCreater.createRequestData(http_req, "ms");
        List<MetricData> vusTrendData = dataCreater.createGaugeDataList(vus_trend, "vus", "1");
        List<MetricData> iterationData = dataCreater.createGaugeDataList(iteration_duration, "iteration_duration", "ms");

        List<MetricData> checkData = dataCreater.createSingleGaugeData(checks, CSVResponseType.CHECKS);
        List<MetricData> vusMaxData = dataCreater.createSingleGaugeData(vus_max, CSVResponseType.VUS_MAX);
        List<MetricData> iterationsData = dataCreater.createSingleGaugeData(iterations, CSVResponseType.ITERATIONS);

        return this.combineData(requestData, vusTrendData, iterationData, checkData, vusMaxData, iterationsData);
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

    private List<String[]> filterType(List<String[]> csv, String filter) {
        return csv.stream().filter(row -> row[0].startsWith(filter)).toList();
    }

    private List<MetricData> combineData(List<MetricData>... createdData) {
        List<MetricData> combinedData = new LinkedList<>();
        Stream.of(createdData).forEach(combinedData::addAll);
        return combinedData;
    }
}