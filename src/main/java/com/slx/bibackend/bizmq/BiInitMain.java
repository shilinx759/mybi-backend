package com.slx.bibackend.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.redisson.client.ChannelName;

/**
 * 用于创建项目消息队列，项目执行前先运行一次
 * @author slx
 * @time 15:22
 */
public class BiInitMain {
    public static void main(String[] args) {
        try {
            //创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            //创还能连接
            Connection connection = factory.newConnection();
            //创建channel
            Channel channel = connection.createChannel();
            //创建交换机
            channel.exchangeDeclare(BiMqConstant.BI_EXCHANGE_NAME, "direct");
            //创建队列
            channel.queueDeclare(BiMqConstant.BI_QUEUE_NAME, true, false, false, null);
            //绑定交换机和队列
            channel.queueBind(BiMqConstant.BI_QUEUE_NAME, BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY);
        } catch (Exception e) {

        }
    }

}
