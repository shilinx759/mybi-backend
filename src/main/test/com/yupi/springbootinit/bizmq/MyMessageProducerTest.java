package com.yupi.springbootinit.bizmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author slx
 * @time 14:57
 */
@SpringBootTest
class MyMessageProducerTest {
    @Resource
    private MyMessageProducer myMessageProducer;
    @Test
    void sendMessage() {
        myMessageProducer.sendMessage("code_exchange","my_routingKey","你好呀,嘻嘻嘻");
    }
}