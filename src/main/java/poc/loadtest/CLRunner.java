package poc.loadtest;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CLRunner {

    @EventListener(ApplicationReadyEvent.class)
    public void startLoadTest() throws IOException, InterruptedException {
        System.out.println("### LOAD TEST STARTED ###");
        String resources = "src/main/resources";

        String script = resources + "/scripts/createdScript.js";
        String output = resources + "/output/output.csv";
        String config = resources + "/config/config.json";

        try {
            ConfigParser.newParse(config, script);
            runTest(script, output);
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
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