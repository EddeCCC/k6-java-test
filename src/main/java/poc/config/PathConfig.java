package poc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import poc.loadtest.exception.UnknownOutputTypeException;

@Component
public class PathConfig {

    @Value("${path.config:config/exampleConfig.json}")
    private String config;
    private final String script = "scripts/createdScript.js";
    private final String outputCSV = "output/output.csv";
    private final String outputJSON = "output/output.json";
    private final String logging = "output/logging.txt";
    private final String resources = getResourcePath();

    public String getConfig() { return resources + config; }

    public String getScript() { return resources + script; }

    public String getOutput(String outputType) {
        return switch (outputType) {
            case "json" -> resources + outputJSON;
            case "csv" -> resources + outputCSV;
            default -> throw new UnknownOutputTypeException(outputType);
        };
    }

    public String getLogging() { return resources + logging; }

    private String getResourcePath() {
        return this.getClass().getClassLoader()
                .getResource("")
                .getFile()
                .substring(1); //remove '/' at the beginning of the string
    }
}