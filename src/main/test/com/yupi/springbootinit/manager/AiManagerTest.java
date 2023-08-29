package com.slx.bibackend.manager;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author slx
 * @time 17:20
 */
//@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        val answer = aiManager.doChat(1659171950288818178L,"分析需求:\n" +
                "分析网站的用户增长趋势\n" +
                "原始数据:\n" +
                "日期,用户数\n" +
                "1日,10\n" +
                "2日,20\n" +
                "3日,30");
        System.out.println(answer);
    }
}