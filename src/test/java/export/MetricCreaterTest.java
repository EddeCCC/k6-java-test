package export;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import poc.export.csv.CSVMetricCreater;
import poc.export.csv.CSVMetricCreaterHelper;
import poc.export.json.JSONMetricCreater;
import poc.export.json.JSONMetricCreaterHelper;
import poc.export.metric.GaugeCreater;
import poc.export.metric.ResultType;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = {JSONMetricCreater.class, JSONMetricCreaterHelper.class,
        CSVMetricCreater.class, CSVMetricCreaterHelper.class, GaugeCreater.class})
public class MetricCreaterTest {

    @Autowired
    private JSONMetricCreater jsonMetricCreater;
    @Autowired
    private CSVMetricCreater csvMetricCreater;

    @Nested
    @DisplayName("JSONMetricCreater")
    class JSONMetricCreaterTest{

        @ParameterizedTest
        @ValueSource(strings = {"allRequests.json", "missingRequests.json"})
        void createRequestMetricTest(String filepath) throws JSONException, IOException {
            List<JSONObject> list = this.loadJSON(filepath);

            List<MetricData> metricData = jsonMetricCreater.createRequestMetric(list, "unit");
            List<MetricData> brokenData = metricData.stream()
                    .filter(data -> data.getData().toString().contains("value=NaN")).toList();

            assertFalse(metricData.isEmpty());
            assertTrue(brokenData.isEmpty());
        }

        @Test
        void createGaugeMetricListTest() throws JSONException, IOException {
            List<JSONObject> list = this.loadJSON("metrics.json");

            List<MetricData> metricData = jsonMetricCreater.createGaugeMetricList(list, "name", "unit");
            List<MetricData> brokenData = metricData.stream()
                    .filter(data -> data.getData().toString().contains("value=NaN")).toList();

            assertFalse(metricData.isEmpty());
            assertTrue(brokenData.isEmpty());
        }

        @Test
        void createSingleGaugeMetricTest() throws JSONException, IOException {
            List<JSONObject> list = this.loadJSON("metrics.json");
            ResultType type = ResultType.MAX_LOAD; //random Type

            List<MetricData> metricData = jsonMetricCreater.createSingleGaugeMetric(list, type);
            List<MetricData> brokenData = metricData.stream()
                    .filter(data -> data.getData().toString().contains("value=NaN")).toList();

            assertFalse(metricData.isEmpty());
            assertTrue(brokenData.isEmpty());
        }

        @Test
        void emptyOutputTest() {
            List<JSONObject> emptyList = Collections.emptyList();
            ResultType type = ResultType.MAX_LOAD; //random Type

            List<MetricData> metricData1 = jsonMetricCreater.createRequestMetric(emptyList, "unit");
            List<MetricData> metricData2 = jsonMetricCreater.createGaugeMetricList(emptyList, "name", "unit");
            List<MetricData> metricData3 = jsonMetricCreater.createSingleGaugeMetric(emptyList, type);

            assertTrue(metricData1.isEmpty());
            assertTrue(metricData2.isEmpty());
            assertTrue(metricData3.isEmpty());
        }

        private List<JSONObject> loadJSON(String filePath) throws IOException, JSONException {
            String resources = MetricCreaterTest.getResources();
            String absolutePath = resources + "json/" + filePath;
            List<JSONObject> objects = new LinkedList<>();
            BufferedReader br = new BufferedReader(new FileReader(absolutePath));
            String line;
            while ((line = br.readLine()) != null) {
                JSONObject json = new JSONObject(line);
                objects.add(json);
            }
            return objects;
        }
    }

    @Nested
    @DisplayName("CSVMetricCreater")
    class CSVMetricCreaterTest {

        @ParameterizedTest
        @ValueSource(strings = {"allRequests.csv", "missingRequests.csv"})
        void createRequestMetricTest(String filePath) throws IOException, CsvException {
            List<String[]> csv = this.loadCSV(filePath);

            List<MetricData> metricData = csvMetricCreater.createRequestMetric(csv, "unit");
            List<MetricData> brokenData = metricData.stream()
                    .filter(data -> data.getData().toString().contains("value=NaN")).toList();

            assertFalse(metricData.isEmpty());
            assertTrue(brokenData.isEmpty());
        }

        @Test
        void createGaugeMetricListTest() throws IOException, CsvException {
            List<String[]> csv = this.loadCSV("metrics.csv");

            List<MetricData> metricData = csvMetricCreater.createGaugeMetricList(csv, "name", "unit");
            List<MetricData> brokenData = metricData.stream()
                    .filter(data -> data.getData().toString().contains("value=NaN")).toList();

            assertFalse(metricData.isEmpty());
            assertTrue(brokenData.isEmpty());
        }

        @Test
        void createSingleGaugeMetricTest() throws IOException, CsvException {
            List<String[]> csv = this.loadCSV("metrics.csv");
            ResultType type = ResultType.MAX_LOAD; //random Type

            List<MetricData> metricData = csvMetricCreater.createSingleGaugeMetric(csv, type);
            List<MetricData> brokenData = metricData.stream()
                    .filter(data -> data.getData().toString().contains("value=NaN")).toList();

            assertFalse(metricData.isEmpty());
            assertTrue(brokenData.isEmpty());
        }

        @Test
        void emptyOutputTest() {
            List<String[]> emptyList = Collections.emptyList();
            ResultType type = ResultType.MAX_LOAD; //random Type

            List<MetricData> metricData1 = csvMetricCreater.createRequestMetric(emptyList, "unit");
            List<MetricData> metricData2 = csvMetricCreater.createGaugeMetricList(emptyList, "name", "unit");
            List<MetricData> metricData3 = csvMetricCreater.createSingleGaugeMetric(emptyList, type);

            assertTrue(metricData1.isEmpty());
            assertTrue(metricData2.isEmpty());
            assertTrue(metricData3.isEmpty());
        }

        private List<String[]> loadCSV(String filepath) throws IOException, CsvException {
            String resources = MetricCreaterTest.getResources();
            String absolutePath = resources + "csv/" + filepath;
            InputStream stream = new FileInputStream(absolutePath);
            Reader streamReader = new InputStreamReader(stream);

            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(',')
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            CSVReader reader = new CSVReaderBuilder(streamReader).withCSVParser(parser).build();
            List<String[]> csv = reader.readAll();
            reader.close();
            return csv;
        }
    }

    private static String getResources() {
        String resources = new File("src/test/resources").getAbsolutePath();
        return resources + "/export/";
    }
}