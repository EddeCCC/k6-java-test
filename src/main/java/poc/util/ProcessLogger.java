package poc.util;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * ProcessLogger writes the console output of a process into a text file
 * If an error occurs, the error message will be written into the console
 */
@Component
public class ProcessLogger {

    private final Logger logger = Logger.getGlobal();

    public void log(Process process, String file) throws IOException, InterruptedException {
        InputStream inputStream = process.getInputStream();

        File logFile = new File(file);
        OutputStream outputStream = new FileOutputStream(logFile);
        IOUtils.copy(inputStream, outputStream);
        this.waitForProcess(process);

        int exitValue = process.exitValue();
        logger.info("LOAD TEST FINISHED WITH EXIT VALUE " + exitValue);
        if(exitValue != 0) this.logError(process);
    }

    private void logError(Process process) throws IOException {
        InputStream errorStream = process.getErrorStream();
        String errorMessage = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
        logger.warning(errorMessage);
    }

    private void waitForProcess(Process process) throws InterruptedException {
        while(process.isAlive()) {
            Thread.sleep(2000);
            System.out.println("...");
        }
    }
}