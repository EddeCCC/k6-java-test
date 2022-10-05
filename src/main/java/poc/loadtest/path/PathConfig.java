package poc.loadtest.path;

import org.springframework.stereotype.Component;

@Component
public class PathConfig {

    private final String resources = getResourcePath();

    public final String script = resources + "scripts/createdScript.js";
    public final String output = resources + "output/output.csv";
    public final String logging = resources + "output/logging.txt";

    public final String config = "config/config.json";
    public final String localConfig = "../" + config;         //Path of config file in relation to script
    public final String globalConfig = resources + config;

    private String getResourcePath() {
        return this.getClass().getClassLoader()
                .getResource("")
                .getFile()
                .substring(1); //remove '/' at the beginning of the string
    }
}
