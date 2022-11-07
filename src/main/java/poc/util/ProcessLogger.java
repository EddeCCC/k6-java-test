package poc.util;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class ProcessLogger {

    public void log(Process process, String file) throws IOException, InterruptedException {
        InputStream inputStream = process.getInputStream();

        File logFile = new File(file);
        OutputStream outputStream = new FileOutputStream(logFile);
        IOUtils.copy(inputStream, outputStream);

        System.out.println("### LOGGER FINISHED ###");
        this.waitForProcess(process);

        if(process.exitValue() < 0) this.logError(process);
        else System.out.println("Load test finished with exitValue " + process.exitValue());
    }

    private void logError(Process process) throws IOException {
        InputStream inputStream = process.getErrorStream();
        String errorMessage = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("Load test failed with exitValue " + process.exitValue());
        System.out.println("Message: ");
        System.err.println(errorMessage);
    }

    private void waitForProcess(Process process) throws InterruptedException {
        while(true) {
            boolean isFinished = !process.isAlive();
            if (isFinished) break;
            Thread.sleep(3000);
            System.out.println("...");
        }
    }
}