package com.yupi.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

/**
 * @author slx
 * @time 16:53
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    //读取里面的配置
    private Integer database;

    private String host;

    private Integer port;

    private String password;

    @Bean
    public RedissonClient redissonClient() throws IOException {
        // 1. Create config object
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://"+host+":"+port);
        //我的redis没有密码
//                .setPassword(password);

        //初始化一个RedissonClient
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }


}
