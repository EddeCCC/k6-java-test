package loadtest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import poc.loadtest.exception.UnknownRequestTypeException;
import poc.loadtest.mapper.HttpMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@ExtendWith(MockitoExtension.class)
public class HttpMapperTest {

    @Spy
    @InjectMocks
    private HttpMapper mapper;

    @ParameterizedTest
    @ValueSource(strings = {"validRequest_lowerCaseType.json", "validRequest_upperCaseType.json"})
    void knownRequestType(String filePath) throws JSONException, IOException {
        JSONObject request = this.loadRequest(filePath);

        mapper.map(request, 0);
        Assertions.assertDoesNotThrow(() -> UnknownRequestTypeException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalidRequest_unknownType.json", "invalidRequest_emptyType.json"})
    void unknownRequestType(String filePath) throws JSONException, IOException {
        JSONObject request = this.loadRequest(filePath);

        Assertions.assertThrows(UnknownRequestTypeException.class, () -> {
            mapper.map(request, 0);
        });
    }

    private JSONObject loadRequest(String configPath) throws IOException, JSONException {
        String resources = this.getResources();
        String absolutePath = resources + configPath;
        String configText = Files.readString(Paths.get(absolutePath));
        return new JSONObject(configText);
    }

    private String getResources() {
        String resources = new File("src/test/resources").getAbsolutePath();
        return resources + "/loadtest/httpMapper/";
    }
}
