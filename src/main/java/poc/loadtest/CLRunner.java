package poc.loadtest;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CLRunner {

    @EventListener(ApplicationReadyEvent.class)
    public void afterPropertiesSet() throws Exception {
        System.out.println("### LOAD TEST STARTED ###");
        String resources = "src/main/resources";
        String url = resources + "/scripts/lightTest.js";
        String output = resources + "/output/output.csv";

        executeTest(url, output);
    }

    private void executeTest(String url, String output) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("k6 run " + url + " --out csv=" + output);
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