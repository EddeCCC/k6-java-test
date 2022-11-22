package export;

import io.opentelemetry.sdk.metrics.data.MetricData;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import poc.export.csv.CSVMetricCreater;
import poc.export.csv.CSVMetricCreaterHelper;
import poc.export.json.JSONMetricCreater;
import poc.export.json.JSONMetricCreaterHelper;
import poc.export.json.ThresholdImporter;
import poc.export.metric.GaugeCreater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {ThresholdImporter.class, JSONMetricCreaterHelper.class, GaugeCreater.class})
public class ThresholdImporterTest {

    @Autowired
    private ThresholdImporter importer;

    @Test
    void importThresholdCorrect() throws JSONException, IOException {
        List<JSONObject> list = loadJSON("thresholdCorrect.json");

        List<MetricData> metricData = importer.importThreshold(list);
        List<MetricData> brokenData = metricData.stream().filter(data -> data.getData().toString().contains("value=NaN")).toList();

        assertFalse(metricData.isEmpty());
        assertTrue(brokenData.isEmpty());
    }

    @Test
    void importThresholdIncorrect() throws JSONException, IOException {
        List<JSONObject> list = loadJSON("thresholdIncorrect.json");

        assertThrows(NumberFormatException.class, () -> {
            importer.importThreshold(list);
        });
    }

    @Test
    void emptyOutputListTest() {
        List<JSONObject> emptyList = Collections.emptyList();

        List<MetricData> metricData = importer.importThreshold(emptyList);

        assertTrue(metricData.isEmpty());
    }

    private List<JSONObject> loadJSON(String filePath) throws IOException, JSONException {
        String resources = this.getResources();
        String absolutePath = resources + "json/threshold/" + filePath;
        List<JSONObject> objects = new LinkedList<>();
        BufferedReader br = new BufferedReader(new FileReader(absolutePath));
        String line;
        while ((line = br.readLine()) != null) {
            JSONObject json = new JSONObject(line);
            objects.add(json);
        }
        return objects;
    }

    private String getResources() {
        String resources = new File("src/test/resources").getAbsolutePath();
        return resources + "/export/";
    }
}
