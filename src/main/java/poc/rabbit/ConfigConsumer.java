package poc.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import poc.loadtest.CLRunner;
import poc.rabbit.config.Constant;

@Component
public class ConfigConsumer {

    @Autowired
    private CLRunner runner;

    @RabbitListener(queues = Constant.CONFIG_QUEUE)
    public void consume(@Payload String config) {
        runner.start(config);
    }
}