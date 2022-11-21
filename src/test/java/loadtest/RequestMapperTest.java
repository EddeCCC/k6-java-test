package loadtest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import poc.loadtest.mapper.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestMapperTest {

    @Spy
    @InjectMocks
    private RequestMapper requestMapper;
    @Mock
    private HttpMapper httpMapper;
    @Mock
    private ChecksMapper checksMapper;
    @Mock
    private ParamsMapper paramsMapper;
    @Mock
    private PayloadMapper payloadMapper;

    @ParameterizedTest
    @ValueSource(strings = {"validConfig_simple.json", "validConfig_additionalKeys.json", "validConfig_advanced.json"})
    void requestIsValid(String configPath) throws JSONException, IOException {
        JSONObject config = this.loadConfig(configPath);

        requestMapper.createScript(config);

        verify(requestMapper, atLeastOnce()).map(any(JSONObject.class), anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalidConfig_noPath.json", "invalidConfig_noType.json", "invalidConfig_noRequests.json"})
    void requestIsInvalid(String configPath) throws JSONException, IOException {
        JSONObject config = this.loadConfig(configPath);

        requestMapper.createScript(config);

        verify(requestMapper, never()).map(any(JSONObject.class), anyInt());
    }

    private JSONObject loadConfig(String configPath) throws IOException, JSONException {
        String resources = this.getResources();
        String absolutePath = resources + configPath;
        String configText = Files.readString(Paths.get(absolutePath));
        return new JSONObject(configText);
    }

    private String getResources() {
        String resources = new File("src/test/resources").getAbsolutePath();
        return resources + "/loadtest/requestMapper/";
    }
}