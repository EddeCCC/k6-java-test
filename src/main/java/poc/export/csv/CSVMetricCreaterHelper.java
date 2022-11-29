package poc.export.csv;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class CSVMetricCreaterHelper {

    public double getAverage(List<String[]> csv) {
        double sum = this.getAmount(csv);
        int count = csv.size();
        double average = sum/count;

        if(Double.isNaN(average)) throw new IllegalArgumentException("Average value is not a number (NaN)");
        else return average;
    }

    public double getMaxLoad(List<String[]> csv) {
        Optional<Double> maybeMaxLoad = csv.stream()
                .map(row -> Double.parseDouble(row[2]))
                .max(Comparator.comparing(Double::valueOf));

        return maybeMaxLoad.orElse(0.0);
    }

    public double getAmount(List<String[]> csv) {
        double sum = csv.stream()
                .map(row -> Double.parseDouble(row[2]))
                .reduce(0.0, Double::sum);
        return sum;
    }
}