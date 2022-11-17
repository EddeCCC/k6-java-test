package poc.loadtest;

import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import poc.config.PathConfig;
import poc.config.TestConfig;
import poc.loadtest.exception.RunnerFailedException;
import poc.export.OTExporter;
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
    private TestConfig tests;
    @Autowired
    private ProcessLogger logger;
    @Autowired
    private OTExporter exporter;

    private String scriptPath;
    private String outputPath;
    private String loggingPath;
    private int maxLoop;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        System.out.println("### LOAD TEST STARTED ###");
        this.getApplicationConfig();
        try {
            this.startLoadTest();
        } catch (IOException | InterruptedException | URISyntaxException | CsvException e) {
            System.out.println("### TEST FAILED ###");
            throw new RunnerFailedException(e.getMessage());
        }
    }

    private void startLoadTest() throws URISyntaxException, IOException, InterruptedException, CsvException {
        int thresholdHaveFailedErrorCode = 99; //Not sure, if all failed thresholds return 99, that´s their generic errorCode

        for(int currentLoop = 0; currentLoop < maxLoop; currentLoop++) {
            String config = loader.loadConfig();
            parser.parse(config, scriptPath);
            int exitCode = this.runCommand();
            exporter.export(outputPath);

            if(exitCode == thresholdHaveFailedErrorCode) break;
        }
    }

    private int runCommand() throws IOException, InterruptedException {
        String command = "k6 run " + "-e K6_JSON_TIME_FORMAT=unix " + scriptPath;
        if(outputPath.endsWith(".json")) command += " --out json=" + outputPath;
        else command += " --out csv=" + outputPath;
        Process process = Runtime.getRuntime().exec(command);

        logger.log(process, loggingPath);
        return process.exitValue();
    }

    private void getApplicationConfig() {
        this.scriptPath = paths.getScript();
        this.loggingPath = paths.getLogging();
        String outputType = tests.getOutputType();
        this.outputPath = paths.getOutput(outputType);
        this.maxLoop = tests.getMaxLoops();
    }
}