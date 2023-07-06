package com.yupi.springbootinit.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author slx
 * @time 15:21
 */
@Component
public class BiMessageProducer {
    @Resource
    private RabbitTemplate template;

    public void sentMessage(String message) {
        template.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY, message);
    }
}
