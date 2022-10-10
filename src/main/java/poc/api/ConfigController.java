package poc.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import poc.loadtest.path.PathConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    PathConfig pathConfig;

    @GetMapping
    public ResponseEntity<String> getConfig() throws IOException {
        String config = pathConfig.getConfig();
        String configText = Files.readString(Paths.get(config));

        return new ResponseEntity<>(configText, HttpStatus.OK);
    }
}