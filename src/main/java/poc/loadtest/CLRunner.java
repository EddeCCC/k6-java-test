package poc.loadtest;

import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import poc.config.PathConfig;
import poc.config.TestConfig;
import poc.loadtest.exception.RunnerFailedException;
import poc.opentelemetry.OTExporter;
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

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        String testType = tests.getType();
        String scriptPath = paths.getScript();
        String outputPath = paths.getOutput();

        switch (testType) {
            case "load" -> this.startLoadTest(scriptPath, outputPath, testType);
            case "spike" -> this.startSpikeTest(scriptPath, outputPath, testType);
            default -> throw new IllegalArgumentException("### UNKNOWN TEST TYPE ###");
        }
    }

    private void startLoadTest(String scriptPath, String outputPath, String testType) {
        System.out.println("### LOAD TEST STARTED ###");

        try {
            String config = loader.loadConfig();
            parser.parse(config, scriptPath, testType);
            this.runCommand(scriptPath, outputPath);
            exporter.export(outputPath);
        } catch (IOException | InterruptedException | URISyntaxException | CsvException e) {
            System.out.println("### LOAD TEST FAILED ###");
            throw new RunnerFailedException(e.getMessage());
        }
    }

    private void startSpikeTest(String scriptPath, String outputPath, String testType) {
        System.out.println("### STRESS TEST STARTED ###");
        int maxLoop = tests.getMaxLoop();
        int exitCode;

        for(int currentLoop = 0; currentLoop < maxLoop; currentLoop++) {
            try {
                String config = loader.loadConfig();
                parser.parse(config, scriptPath, testType);
                exitCode = this.runCommand(scriptPath, outputPath);
                exporter.export(outputPath);
            } catch (IOException | InterruptedException | URISyntaxException | CsvException e) {
                System.out.println("### STRESS TEST FAILED ###");
                throw new RunnerFailedException(e.getMessage());
            }
            //Not sure, if failed thresholds always return exitCode 99
            if(exitCode == 99) break;
        }
    }

    private int runCommand(String scriptPath, String outputPath) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        String command = "k6 run " + scriptPath + " --out csv=" + outputPath;
        Process process = runtime.exec(command);

        String loggingPath = paths.getLogging();
        logger.log(process, loggingPath);
        return process.exitValue();
    }
}