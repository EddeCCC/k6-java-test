package poc.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import poc.loadtest.ConfigLoader;
import poc.loadtest.path.PathConfig;
import poc.rabbit.config.Constant;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class ConfigProducer {

    @Autowired
    private ConfigLoader loader;
    @Autowired
    private PathConfig pathConfig;
    @Autowired
    private RabbitTemplate template;

    @EventListener(ApplicationReadyEvent.class)
    public String produce() throws URISyntaxException, IOException, InterruptedException {
        String server = pathConfig.getServer();
        String config = loader.loadConfig(server);

        template.convertAndSend(
                Constant.CONFIG_EXCHANGE,
                Constant.CONFIG_KEY,
                config
        );

        return "CONFIG WAS PUBLISHED";
    }
}