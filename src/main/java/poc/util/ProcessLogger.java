package poc.util;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poc.loadtest.path.PathConfig;

import java.io.*;

@Component
public class ProcessLogger {

    public void log(Process process, String file) throws IOException, InterruptedException {
        InputStream inputStream = process.getInputStream();

        File logFile = new File(file);
        OutputStream outputStream = new FileOutputStream(logFile);
        IOUtils.copy(inputStream, outputStream);

        System.out.println("### LOGGER FINISHED ###");
        this.waitForProcess(process);
        System.out.println("Load test finished with value " + process.exitValue());
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