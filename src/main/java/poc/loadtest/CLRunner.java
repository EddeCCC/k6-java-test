package poc.loadtest;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

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
            System.out.println(e.getMessage());
        }
    }

    private void runTest(String script, String output) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        String command = "k6 run " + script + " --out csv=" + output;
        Process process = runtime.exec(command);

        this.logTest(process);
        this.checkIfProcessFinished(process);
    }

    //Will be more useful, when logTest() is in an extra Thread
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

    //Could use an extra Thread
    private void logTest(Process process) throws IOException {
        InputStream inputStream = process.getInputStream();

        String resources = this.getResourcePath();
        String logging = resources + "output/logging.txt";
        File log = new File(logging);

        OutputStream outputStream = new FileOutputStream(log);
        IOUtils.copy(inputStream, outputStream);
    }

    private String getResourcePath() {
        return this.getClass().getClassLoader()
                .getResource("")
                .getFile()
                .substring(1); //remove '/' at the beginning of the string
    }
}