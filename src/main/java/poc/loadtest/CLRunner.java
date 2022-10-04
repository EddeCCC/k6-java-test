package poc.loadtest;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class CLRunner {

    @EventListener(ApplicationReadyEvent.class)
    public void startLoadTest() {
        System.out.println("### LOAD TEST STARTED ###");
        String resources = this.getResourcePath();

        String script = resources + "scripts/createdScript.js";
        String output = resources + "output/output.csv";
        String config = resources + "config/config.json";

        try {
            ConfigParser.newParse(config, script);
            this.runTest(script, output);
        } catch (IOException | InterruptedException e){
            System.out.println("### LOAD TEST FAILED ###");
        }
    }

    private void runTest(String script, String output) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("k6 run " + script + " --out csv=" + output);
        this.checkIfProcessFinished(process);
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

    private String getResourcePath() {
        return this.getClass().getClassLoader()
                .getResource("")
                .getFile()
                .substring(1); //remove '/' at the beginning of the string
    }
}