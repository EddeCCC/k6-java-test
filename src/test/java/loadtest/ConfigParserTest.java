package loadtest;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import poc.loadtest.ConfigParser;
import poc.loadtest.exception.InvalidConfigurationException;
import poc.loadtest.mapper.RequestMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigParserTest {

	@InjectMocks
	private ConfigParser parser;
	@Mock
	private RequestMapper mapper;

	private final String scriptPath = getResources() + "testScript.js";

	@ParameterizedTest
	@ValueSource(strings = {"validConfig_simple.json", "validConfig_additionalKeys.json"})
	void configIsValid(String configPath) throws IOException {
		String configText = loadConfig(configPath);

		parser.parse(configText, scriptPath);

		verify(mapper, times(1)).createScript(any(JSONObject.class));
	}

	@ParameterizedTest
	@ValueSource(strings = {"invalidConfig_noBaseURL.json", "invalidConfig_noOptions.json"})
	void configIsInvalid(String configPath) throws IOException {
		String configText = loadConfig(configPath);

		Assertions.assertThrows(InvalidConfigurationException.class, () -> {
			parser.parse(configText, scriptPath);
		});
	}

	private String loadConfig(String configPath) throws IOException {
		String resources = this.getResources();
		String absolutePath = resources + configPath;
		return Files.readString(Paths.get(absolutePath));
	}

	private String getResources() {
		String resources = new File("src/test/resources").getAbsolutePath();
		return resources + "/loadtest/configParser/";
	}
}