package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * @author: Justin
 */
@EnableRabbit
@Configuration
public class RabbitMqConfig {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    // RabbitMqConfig对象创建完毕后，加载方法
    @PostConstruct
    public void initRabbitTemplate() {
        // 只要消息抵达Broker ack就为true
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                String msgId = correlationData.getId();
                if(ack){
                    System.out.println("消息成功发送到broker");
                    System.out.println("消息标识是:" + msgId);
                }else {
                    System.out.println("消息被拒收的原因是:" + cause);
                }
            }
        });

        /**
         *  // 只要消息没有投递给指定的队列就触发这个失败回调
         * @param message 投递失败的消息详细信息
         * @param replyCode 回复状态码
         * @param desc 回复文本内容
         * @param exchangeName 发给那个交换机
         * @param routingKey rout键
         */
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String desc, String exchangeName, String routingKey) {
                System.out.println("消息被退回");
                System.out.println("被退回的消息是 :" + new String(message.getBody()));
                System.out.println("被退回的消息编码是 :" + replyCode);
            }
        });

    }


    @Bean
    public Queue orderDelayQueue() {
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "order-event-exchange");
        args.put("x-dead-letter-routing-key", "order.release.order");
        args.put("x-message-ttl", 60000);
        return new Queue("order.delay.queue", true, false, false, args);
    }

    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.order.queue", true, false, false, null);
    }

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false, null);
    }

    @Bean
    public Binding orderCreateBinding() {
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order", null);
    }

    @Bean
    public Binding orderReleaseBinding() {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order", null);
    }

    @Bean
    public Binding wareOrderReleaseConfirmBinding() {
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#", null);
    }

    @Bean
    public Queue secKillOrderQueue() {

        return new Queue("order.secKill.queue", true, false, false, null);
    }

    @Bean
    public Binding secKillCreateOrderBinding() {
        return new Binding("order.secKill.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.secKill", null);
    }
}
