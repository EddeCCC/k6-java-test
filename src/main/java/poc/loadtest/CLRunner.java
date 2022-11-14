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
    private LoadIncreaser increaser;
    @Autowired
    private PathConfig paths;
    @Autowired
    private TestConfig tests;
    @Autowired
    private ProcessLogger logger;
    @Autowired
    private OTExporter exporter;

    private String testType;
    private String scriptPath;
    private String outputPath;
    private String loggingPath;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        this.getApplicationConfig();
        try {
            switch (testType) {
                case "load" -> this.startLoadTest();
                case "breakpoint" -> this.startBreakpointTest();
                default -> throw new IllegalArgumentException("### UNKNOWN TEST TYPE ###");
            }
        } catch (IOException | InterruptedException | URISyntaxException | CsvException e) {
            System.out.println("### TEST FAILED ###");
            throw new RunnerFailedException(e.getMessage());
        }
    }

    private void startLoadTest() throws URISyntaxException, IOException, InterruptedException, CsvException {
        System.out.println("### LOAD TEST STARTED ###");
        String config = loader.loadConfig();
        parser.parse(config, scriptPath);
        this.runCommand();
        exporter.export(outputPath);
    }

    private void startBreakpointTest() throws URISyntaxException, IOException, InterruptedException, CsvException {
        System.out.println("### BREAKPOINT TEST STARTED ###");
        int maxLoop = tests.getMaxLoop();
        String config = loader.loadConfig();
        //Not sure, if all failed thresholds return 99, that´s their generic errorCode
        int thresholdHaveFailedErrorCode = 99;

        for(int currentLoop = 0; currentLoop < maxLoop; currentLoop++) {
            if(currentLoop != 0) config = increaser.increase(config);
            parser.parse(config, scriptPath);
            int exitCode = this.runCommand();
            exporter.export(outputPath);

            if(exitCode == thresholdHaveFailedErrorCode) break;
        }
    }

    private int runCommand() throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        String command = "k6 run " + scriptPath + " --out csv=" + outputPath;
        Process process = runtime.exec(command);

        logger.log(process, loggingPath);
        return process.exitValue();
    }

    private void getApplicationConfig() {
        this.testType = tests.getType();
        this.scriptPath = paths.getScript();
        this.outputPath = paths.getOutput();
        this.loggingPath = paths.getLogging();
    }
}