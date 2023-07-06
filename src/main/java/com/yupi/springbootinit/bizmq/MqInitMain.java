package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 用于创建测试程序用到的交换机和程序（在程序启动前只执行一次）
 * @author slx
 * @time 14:31
 */
public class MqInitMain {

    public static void main(String[] args) {
        try {
            //创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            //创建连接
            Connection connection = factory.newConnection();
            //创建客户端
            Channel channel = connection.createChannel();
            //声明交换机，设置类型 这里设置为 Direct
            String EXCHANGE_NAME = "code_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            //创建队列
            String QUEUE_NAME = "code_queue";
            channel.queueDeclare(QUEUE_NAME, false, false,false,null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "my_routingKey");
        } catch (Exception e) {

        }

    }
}
