package poc.loadtest;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CLRunner {

    @EventListener(ApplicationReadyEvent.class)
    public void executeLoadTest() throws Exception {
        System.out.println("### LOAD TEST STARTED ###");
        String resources = "src/main/resources";

        String config = resources + "/config/script.json";
        String script = resources + "/scripts/createdScript.js";
        String output = resources + "/output/output.csv";
        String map = resources + "/config/map.json";

        //ConfigParser.parse(config, script);
        ConfigParser.newParse(map, script);
        runTest(script, output);
    }

    private void runTest(String script, String output) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("k6 run " + script + " --out csv=" + output);
        checkIfProcessFinished(process);
    }

    private void checkIfProcessFinished(Process process) throws InterruptedException {
        boolean continueLoop = true;
        while(continueLoop) {
            try {
                process.exitValue();
                continueLoop = false;
            } catch (Exception e) {
                Thread.sleep(3000);
                System.out.println("...");
            }
        }

        System.out.println("Load test finished with value " + process.exitValue());
    }
}