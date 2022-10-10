package poc.rabbit.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
    @Bean
    public TopicExchange configExchange() { return new TopicExchange(Constant.CONFIG_EXCHANGE); }

    @Bean
    public Queue configQueue() { return new Queue(Constant.CONFIG_QUEUE, false); }

    @Bean
    public Binding configBinding(@Qualifier("configQueue") Queue queue,
                                      @Qualifier("configExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(Constant.CONFIG_KEY);
    }

    @Bean
    public MessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
