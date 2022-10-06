package poc.loadtest.path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PathConfig {

    @Value("${path.script}")
    private String script;
    @Value("${path.output}")
    private String output;
    @Value("${path.config}")
    private String config;
    @Value("${path.logging}")
    private String logging;
    private final String resources = getResourcePath();


    public String getScript() { return resources + script; }

    public String getOutput() { return resources + output; }

    //Path of config file in relation to script
    public String getLocalConfig() { return "../" + config; }

    public String getGlobalConfig() { return resources + config; }

    public String getLogging() { return resources + logging; }

    private String getResourcePath() {
        return this.getClass().getClassLoader()
                .getResource("")
                .getFile()
                .substring(1); //remove '/' at the beginning of the string
    }
}