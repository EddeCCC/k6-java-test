package poc.export.json;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class JSONMetricCreaterHelper {

    public double getAverage(List<JSONObject> results) {
        double sum = this.getAmount(results);
        int count = results.size();
        double average = sum/count;

        if(Double.isNaN(average)) throw new IllegalArgumentException("Average value is not a number (NaN)");
        else return average;
    }

    public double getMaxLoad(List<JSONObject> results) {
        Optional<Double> maybeMaxLoad = results.stream()
                .map(result -> result.getJSONObject("data").getDouble("value"))
                .max(Comparator.comparing(Double::valueOf));

        return maybeMaxLoad.orElse(0.0);
    }

    public double getAmount(List<JSONObject> results) {
        double sum = results.stream()
                .map(result -> result.getJSONObject("data").getDouble("value"))
                .reduce(0.0, Double::sum);
        return sum;
    }

    public long getEpochNanos(String time) {
        long timestamp = OffsetDateTime.parse(time).toEpochSecond();
        long epochNanos =  TimeUnit.SECONDS.toNanos(timestamp);
        return epochNanos;
    }
}