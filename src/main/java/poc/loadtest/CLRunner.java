package poc.loadtest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import poc.config.PathConfig;
import poc.util.ProcessLogger;

import java.io.*;
import java.net.URISyntaxException;

@Component
public class CLRunner {

    @Autowired
    private ConfigParser parser;
    @Autowired
    private ConfigLoader loader;
    @Autowired
    private PathConfig paths;
    @Autowired
    private ProcessLogger logger;

    //@EventListener(ApplicationReadyEvent.class)
    public void start() {
        System.out.println("### LOAD TEST STARTED ###");

        String scriptPath = paths.getScript();
        String outputPath = paths.getOutput();
        String serverAddress = paths.getServer();

        try {
            String config = loader.loadConfig(serverAddress);
            parser.parse(config, scriptPath);
            this.runLoadTest(scriptPath, outputPath);
        } catch (IOException | InterruptedException | URISyntaxException e) {
            System.out.println("### LOAD TEST FAILED ###");
            System.out.println(e.getMessage());
        }
    }

    private void runLoadTest(String scriptPath, String outputPath) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        String command = "k6 run " + scriptPath + " --out csv=" + outputPath;
        Process process = runtime.exec(command);

        String loggingPath = paths.getLogging();
        logger.log(process, loggingPath);
    }
}