package poc.opentelemetry.csv;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.opentelemetry.metric.OTMetricCreater;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class CSVImporter {

    @Autowired
    private OTMetricCreater metricCreater;

    public List<MetricData> importMetricData(String filePath) throws IOException, CsvException {
        CSVReader reader = this.createCSVReader(filePath);
        List<String[]> csv = reader.readAll();
        reader.close();

        List<String[]> http_reqs = this.filterType(csv, "http_req_");
        List<String[]> vus_progress = this.filterType(csv, "vus");
        List<String[]> iteration_duration = this.filterType(csv, "iteration_duration");
        List<String[]> data_sent = this.filterType(csv, "data_sent");
        List<String[]> data_received = this.filterType(csv, "data_received");

        List<String[]> checks = this.filterType(csv, "checks");
        List<String[]> vus_max = this.filterType(csv, "vus_max");
        List<String[]> iterations = this.filterType(csv, "iterations");
        List<String[]> http_req_count = this.filterType(csv, "http_reqs");

        List<MetricData> requestMetric = metricCreater.createRequestMetric(http_reqs, "ms");
        List<MetricData> vusProgressMetric = metricCreater.createGaugeMetricList(vus_progress, "vus", "1");
        List<MetricData> iterationMetric = metricCreater.createGaugeMetricList(iteration_duration, "iteration_duration", "ms");
        List<MetricData> dataSentMetric = metricCreater.createGaugeMetricList(data_sent, "data_sent", "B");
        List<MetricData> dataReceivedMetric = metricCreater.createGaugeMetricList(data_received, "data_received", "B");

        List<MetricData> checksMetric = metricCreater.createSingleGaugeMetric(checks, CSVResponseType.CHECKS);
        List<MetricData> vusMaxMetric = metricCreater.createSingleGaugeMetric(vus_max, CSVResponseType.VUS_MAX);
        List<MetricData> iterationsCounterMetric = metricCreater.createSingleGaugeMetric(iterations, CSVResponseType.ITERATIONS);
        List<MetricData> requestCounterMetric = metricCreater.createSingleGaugeMetric(http_req_count, CSVResponseType.HTTP_REQS);

        return this.combineData(requestMetric, vusProgressMetric, iterationMetric, dataSentMetric, dataReceivedMetric,
                checksMetric, vusMaxMetric, iterationsCounterMetric, requestCounterMetric);
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