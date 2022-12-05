package poc.export.csv;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.export.metric.ResultType;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class CSVImporter {

    @Autowired
    private CSVMetricCreater metricCreater;

    public List<MetricData> importMetricData(String filePath) throws IOException, CsvException {
        List<String[]> csv = this.readFile(filePath);

        List<String[]> http_reqs = csv.stream().filter(row -> row[0].startsWith("http_req_")).toList();
        List<String[]> vus = this.filterMetric(csv, "vus");
        List<String[]> data_sent = this.filterMetric(csv, "data_sent");
        List<String[]> data_received = this.filterMetric(csv, "data_received");
        List<String[]> data_sent_endpoint = this.filterMetric(csv,"data_sent_endpoint");
        List<String[]> data_received_endpoint = this.filterMetric(csv, "data_received_endpoint");

        List<String[]> checks = this.filterMetric(csv, "checks");
        List<String[]> iteration_duration = this.filterMetric(csv, "iteration_duration");
        List<String[]> iterations = this.filterMetric(csv, "iterations");
        List<String[]> http_req_count = this.filterMetric(csv, "http_reqs");

        List<MetricData> requestMetric = metricCreater.createRequestMetric(http_reqs, "ms");
        List<MetricData> vusMetric = metricCreater.createGaugeMetricList(vus, "vus", "1");
        List<MetricData> dataSentMetric = metricCreater.createGaugeMetricList(data_sent, "data_sent", "B");
        List<MetricData> dataReceivedMetric = metricCreater.createGaugeMetricList(data_received, "data_received", "B");
        List<MetricData> dataSentEndpointMetric = metricCreater.createGaugeMetricList(data_sent_endpoint, "data_sent_endpoint", "B");
        List<MetricData> dataReceivedEndpointMetric = metricCreater.createGaugeMetricList(data_received_endpoint, "data_received_endpoint", "B");

        List<MetricData> checksMetric = metricCreater.createSingleGaugeMetric(checks, ResultType.CHECKS);
        List<MetricData> vusMaxMetric = metricCreater.createSingleGaugeMetric(vus, ResultType.MAX_LOAD);
        List<MetricData> iterationMetric = metricCreater.createSingleGaugeMetric(iteration_duration, ResultType.ITERATION_DURATION);
        List<MetricData> iterationsCounterMetric = metricCreater.createSingleGaugeMetric(iterations, ResultType.ITERATIONS);
        List<MetricData> requestCounterMetric = metricCreater.createSingleGaugeMetric(http_req_count, ResultType.HTTP_REQS);

        return this.combineData(requestMetric, vusMetric, dataSentMetric, dataReceivedMetric, dataSentEndpointMetric, dataReceivedEndpointMetric,
                checksMetric, vusMaxMetric, iterationMetric, iterationsCounterMetric, requestCounterMetric);
    }

    private List<String[]> readFile(String path) throws IOException, CsvException {
        InputStream stream = new FileInputStream(path);
        Reader streamReader = new InputStreamReader(stream);

        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        CSVReader reader = new CSVReaderBuilder(streamReader)
                .withSkipLines(1)
                .withCSVParser(parser)
                .build();
        List<String[]> csv = reader.readAll();
        reader.close();
        return csv;
    }

    private List<String[]> filterMetric(List<String[]> csv, String filter) {
        return csv.stream().filter(row -> row[0].equals(filter)).toList();
    }

    @SafeVarargs
    private List<MetricData> combineData(List<MetricData>... createdData) {
        List<MetricData> combinedData = new LinkedList<>();
        Stream.of(createdData).forEach(combinedData::addAll);
        return combinedData;
    }
}