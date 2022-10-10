package poc.loadtest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import poc.loadtest.path.PathConfig;
import poc.rabbit.ConfigConsumer;
import poc.util.ProcessLogger;

import java.io.*;
import java.net.URISyntaxException;

@Component
public class CLRunner {

    @Autowired
    private ConfigParser parser;
    @Autowired
    private PathConfig paths;
    @Autowired
    private ProcessLogger logger;

    public void start(String config) {
        System.out.println("### LOAD TEST STARTED ###");

        String script = paths.getScript();
        String output = paths.getOutput();

        try {
            parser.parse(config, script);
            this.runLoadTest(script, output);
        } catch (IOException | InterruptedException e) {
            System.out.println("### LOAD TEST FAILED ###");
            System.out.println(e.getMessage());
        }
    }

    private void runLoadTest(String script, String output) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        String command = "k6 run " + script + " --out csv=" + output;
        Process process = runtime.exec(command);

        String logging = paths.getLogging();
        logger.log(process, logging);
    }
}