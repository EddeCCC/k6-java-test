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

        int exitValue = process.exitValue();
        System.out.println("Load test finished with exitValue " + exitValue);
        if(exitValue != 0) this.logError(process);
    }

    private void logError(Process process) throws IOException {
        InputStream inputStream = process.getErrorStream();
        String errorMessage = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
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