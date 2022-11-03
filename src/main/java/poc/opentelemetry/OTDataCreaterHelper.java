package poc.opentelemetry;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OTDataCreaterHelper {

    public double getCheckAccuracy(List<String[]> csv) {
        double sum = csv.stream()
                .map(row -> Double.parseDouble(row[2]))
                .reduce(0.0, Double::sum);
        int count = csv.size();
        return sum / count;
    }

    public double getVusMax(List<String[]> csv) {
        String[] vus_max = csv.get(0);
        return Double.parseDouble(vus_max[2]);
    }

    public double getIterations(List<String[]> csv) {
        double sum = csv.stream()
                .map(row -> Double.parseDouble(row[2]))
                .reduce(0.0, Double::sum);
        return sum;
    }
}
