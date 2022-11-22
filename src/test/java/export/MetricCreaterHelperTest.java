package export;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import poc.export.csv.CSVMetricCreaterHelper;
import poc.export.json.JSONMetricCreaterHelper;
import poc.loadtest.exception.InvalidConfigurationException;

import java.io.*;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MetricCreaterHelperTest {

    private final JSONMetricCreaterHelper jsonHelper = new JSONMetricCreaterHelper();
    private final CSVMetricCreaterHelper csvHelper = new CSVMetricCreaterHelper();

    @Nested
    @DisplayName("JSONMetricCreaterHelper")
    class JSONMetricCreaterHelperTest {

        @Test
        void getMaxLoad() throws JSONException, IOException {
            List<JSONObject> list = loadJSON("metrics.json");

            double maxLoad = jsonHelper.getMaxLoad(list);

            assertEquals(maxLoad, 20.0);
        }

        @Test
        void getMaxLoadEmptyList() {
            List<JSONObject> emptyList = Collections.emptyList();

            double maxLoad = jsonHelper.getMaxLoad(emptyList);

            assertEquals(maxLoad, 0.0);
        }

        @Test
        void getAverage() throws JSONException, IOException {
            List<JSONObject> list = loadJSON("helper/checks.json");

            double average = jsonHelper.getAverage(list);

            assertEquals(average, 0.75);
        }

        @Test
        void getAverageEmptyList() {
            List<JSONObject> emptyList = Collections.emptyList();

            assertThrows(IllegalArgumentException.class, () -> {
                jsonHelper.getAverage(emptyList);
            });
        }

        @Test
        void getAmount() throws JSONException, IOException {
            List<JSONObject> list = loadJSON("helper/iterations.json");

            double amount = jsonHelper.getAmount(list);

            assertEquals(amount, 4);
        }

        @Test
        void getAmountEmptyList() {
            List<JSONObject> emptyList = Collections.emptyList();

            double amount= jsonHelper.getAmount(emptyList);

            assertEquals(amount, 0.0);
        }

        @Test
        void getEpochNanosCorrect() {
            String time = "2022-11-22T08:34:16.9032205+01:00";

            long epochNanos = jsonHelper.getEpochNanos(time);

            assertEquals(epochNanos, 1669102456000000000L);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "2022-11-22T08:34:16.9032205", "2018-07-14T14:31:30+0530", "Tue, 22 Nov 2022 08:34:16 UTC"})
        void getEpochNanosIncorrect(String time) {
            assertThrows(DateTimeParseException.class, () -> {
                jsonHelper.getEpochNanos(time);
            });
        }

        private List<JSONObject> loadJSON(String filePath) throws IOException, JSONException {
            String resources = MetricCreaterHelperTest.getResources();
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
    @DisplayName("CSVMetricCreaterHelper")
    class CSVMetricCreaterHelperTest {

        @Test
        void getMaxLoad() throws IOException, CsvException {
            List<String[]> list = loadCSV("metrics.csv");

            double maxLoad = csvHelper.getMaxLoad(list);

            assertEquals(maxLoad, 20.0);
        }

        @Test
        void getMaxLoadEmptyList() {
            List<JSONObject> emptyList = Collections.emptyList();

            double maxLoad = jsonHelper.getMaxLoad(emptyList);

            assertEquals(maxLoad, 0.0);
        }

        @Test
        void getAverage() throws IOException, CsvException {
            List<String[]> list = loadCSV("helper/checks.csv");

            double average = csvHelper.getAverage(list);

            assertEquals(average, 0.75);
        }

        @Test
        void getAverageEmptyList() {
            List<String[]> emptyList = Collections.emptyList();

            assertThrows(IllegalArgumentException.class, () -> {
                csvHelper.getAverage(emptyList);
            });
        }

        @Test
        void getAmount() throws IOException, CsvException {
            List<String[]> list = loadCSV("helper/iterations.csv");

            double amount = csvHelper.getAmount(list);

            assertEquals(amount, 4);
        }

        @Test
        void getAmountEmptyList() {
            List<String[]> emptyList = Collections.emptyList();

            double amount= csvHelper.getAmount(emptyList);

            assertEquals(amount, 0.0);
        }

        private List<String[]> loadCSV(String filepath) throws IOException, CsvException {
            String resources = MetricCreaterHelperTest.getResources();
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
