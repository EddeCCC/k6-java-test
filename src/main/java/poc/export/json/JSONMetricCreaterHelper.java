package poc.export.json;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class JSONMetricCreaterHelper {

    public double getAverage(List<JSONObject> results) {
        double sum = results.stream()
                .map(result -> result.getJSONObject("data").getDouble("value"))
                .reduce(0.0, Double::sum);
        int count = results.size();
        return sum/count;
    }

    public double getMaxLoad(List<JSONObject> results){
        double maxLoad = results.stream()
                .map(result -> result.getJSONObject("data").getDouble("value"))
                .max(Comparator.comparing(Double::valueOf))
                .get();

        return maxLoad;
    }

    public double getAmount(List<JSONObject> results) {
        double sum = results.stream()
                .map(result -> result.getJSONObject("data").getDouble("value"))
                .reduce(0.0, Double::sum);
        return sum;
    }

    public long getEpochNanos(String time) {
        OffsetDateTime odt = OffsetDateTime.parse(time);
        long timestamp = odt.toEpochSecond();
        long epochNanos =  TimeUnit.SECONDS.toNanos(timestamp);

        return epochNanos;
    }
}