package poc.rabbit.config;

import org.springframework.beans.factory.annotation.Value;

public class Constant {

    @Value("${rabbit.queue}")
    public final static String CONFIG_QUEUE = "get_config";
    @Value("${rabbit.exchange}")
    public final static String CONFIG_EXCHANGE = "config";
    @Value("${rabbit.key}")
    public final static String CONFIG_KEY = "get";
}
