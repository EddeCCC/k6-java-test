package poc.opentelemetry.metric;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class OTMetricCreaterHelper {

    public double getAverage(List<String[]> csv) {
        double sum = csv.stream()
                .map(row -> Double.parseDouble(row[2]))
                .reduce(0.0, Double::sum);
        int count = csv.size();
        return sum / count;
    }

    public double getMaxLoad(List<String[]> csv) {
        double maxLoad = csv.stream()
                .map(row -> Double.parseDouble(row[2]))
                .max(Comparator.comparing(Double::valueOf))
                .get();

        return maxLoad;
    }

    public double getAmount(List<String[]> csv) {
        double sum = csv.stream()
                .map(row -> Double.parseDouble(row[2]))
                .reduce(0.0, Double::sum);
        return sum;
    }
}