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
import java.util.logging.Logger;

@Component
public class CLRunner {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @Autowired
    private PathConfig paths;
    @Autowired
    private TestConfig tests;
    @Autowired
    private ConfigLoader loader;
    @Autowired
    private ConfigParser parser;
    @Autowired
    private ProcessLogger processLogger;
    @Autowired
    private OTExporter exporter;
    @Autowired
    private LoadIncreaser increaser;

    private String scriptPath;
    private String outputPath;
    private String loggingPath;
    private boolean isBreakpointEnabled;
    private int maxLoop;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        logger.info("### LOAD TEST STARTED ###");
        this.getApplicationConfig();
        try {
            this.startLoadTest();
        } catch (Exception e) {
            logger.severe("### TEST FAILED ###");
            e.printStackTrace();
            throw new RunnerFailedException(e.getMessage());
        }
    }

    private void startLoadTest() throws URISyntaxException, IOException, InterruptedException, CsvException {
        //Not sure, if all failed thresholds return 99, that´s their generic errorCode
        int thresholdHaveFailedErrorCode = 99;

        String config = loader.loadConfig();
        for(int currentLoop = 0; currentLoop < maxLoop; currentLoop++) {
            parser.parse(config, scriptPath);
            logger.info("### CONFIG WAS PARSED INTO SCRIPT ###");
            int exitCode = this.runCommand();
            exporter.export(outputPath);

            if(isBreakpointEnabled) config = increaser.increaseLoad(config);
            if(exitCode == thresholdHaveFailedErrorCode) break;
        }
    }

    private int runCommand() throws IOException, InterruptedException {
        String command = "k6 run " + scriptPath;
        if(outputPath.endsWith(".json")) command += " --out json=" + outputPath;
        else command += " --out csv=" + outputPath;
        Process process = Runtime.getRuntime().exec(command);

        processLogger.log(process, loggingPath);
        return process.exitValue();
    }

    private void getApplicationConfig() {
        this.scriptPath = paths.getScript();
        this.loggingPath = paths.getLogging();
        String outputType = tests.getOutputType();
        this.outputPath = paths.getOutput(outputType);
        this.isBreakpointEnabled = tests.getBreakpointConfig();
        this.maxLoop = tests.getMaxLoops();
    }
}