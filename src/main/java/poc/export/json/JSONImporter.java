package poc.export.json;

import io.opentelemetry.sdk.metrics.data.MetricData;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.export.metric.ResultType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class JSONImporter {

    @Autowired
    private JSONMetricCreater metricCreater;

    public List<MetricData> importMetricData(String filePath) throws IOException {
        List<JSONObject> allResults = this.readFile(filePath);

        List<JSONObject> http_reqs = allResults.stream()
                .filter(json -> json.getString("type").equals("Point")
                        && json.getString("metric").startsWith("http_req_"))
                .toList();
        List<JSONObject> vus = this.filterMetric(allResults, "vus");
        List<JSONObject> data_sent = this.filterMetric(allResults, "data_sent");
        List<JSONObject> data_received = this.filterMetric(allResults, "data_received");

        List<JSONObject> checks = this.filterMetric(allResults, "checks");
        List<JSONObject> iteration_duration = this.filterMetric(allResults, "iteration_duration");
        List<JSONObject> iterations = this.filterMetric(allResults, "iterations");
        List<JSONObject> http_req_count = this.filterMetric(allResults, "http_reqs");

        List<MetricData> requestMetric = metricCreater.createRequestMetric(http_reqs, "ms");
        List<MetricData> vusMetric = metricCreater.createGaugeMetricList(vus, "vus", "1");
        List<MetricData> dataSentMetric = metricCreater.createGaugeMetricList(data_sent, "data_sent", "B");
        List<MetricData> dataReceivedMetric = metricCreater.createGaugeMetricList(data_received, "data_received", "B");

        List<MetricData> checksMetric = metricCreater.createSingleGaugeMetric(checks, ResultType.CHECKS);
        List<MetricData> vusMaxMetric = metricCreater.createSingleGaugeMetric(vus, ResultType.MAX_LOAD);
        List<MetricData> iterationMetric = metricCreater.createSingleGaugeMetric(iteration_duration, ResultType.ITERATION_DURATION);
        List<MetricData> iterationsCounterMetric = metricCreater.createSingleGaugeMetric(iterations, ResultType.ITERATIONS);
        List<MetricData> requestCounterMetric = metricCreater.createSingleGaugeMetric(http_req_count, ResultType.HTTP_REQS);

        return this.combineData(requestMetric, vusMetric, dataSentMetric, dataReceivedMetric,
                checksMetric, vusMaxMetric, iterationMetric, iterationsCounterMetric, requestCounterMetric);
    }

    private List<JSONObject> readFile(String path) throws IOException {
        List<JSONObject> objects = new LinkedList<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) {
            JSONObject json = new JSONObject(line);
            objects.add(json);
        }
        return objects;
    }

    private List<JSONObject> filterMetric(List<JSONObject> jsonList, String filter) {
        return jsonList.stream()
                .filter(json -> json.getString("type").equals("Point")
                        && json.getString("metric").equals(filter)
                ).toList();
    }

    @SafeVarargs
    private List<MetricData> combineData(List<MetricData>... createdData) {
        List<MetricData> combinedData = new LinkedList<>();
        Stream.of(createdData).forEach(combinedData::addAll);
        return combinedData;
    }
}
