package poc.loadtest;

import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import poc.config.PathConfig;
import poc.loadtest.exception.RunnerFailedException;
import poc.opentelemetry.OTRecorder;
import poc.util.ProcessLogger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private OTRecorder recorder;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        System.out.println("### LOAD TEST STARTED ###");
        String scriptPath = paths.getScript();
        String outputPath = paths.getOutput();

        try {
            String config = loader.loadConfig();
            parser.parse(config, scriptPath);
            this.runLoadTest(scriptPath, outputPath);
            recorder.record(outputPath);
        } catch (IOException | InterruptedException | URISyntaxException | CsvException e) {
            System.out.println("### LOAD TEST FAILED ###");
            throw new RunnerFailedException(e.getMessage());
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