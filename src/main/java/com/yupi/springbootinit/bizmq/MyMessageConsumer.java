package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author slx
 * @time 14:13
 */
@Component
@Slf4j
public class MyMessageConsumer {//死记硬背
    //指定监听哪个消息队列，设置消息确认方式，等同于demo中的 channel.basicConsumer
    @RabbitListener(queues = {"code_queue"},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("receiveMessage:{}", message);
        channel.basicAck(deliveryTag, false);

    }
}
