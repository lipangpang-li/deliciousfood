package org.example.test;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    public void setUp() {
        // 手动配置Redis连接
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("116.205.106.116"); // 替换为你的Redis服务器地址
        config.setPort(6379); // 替换为你的Redis服务器端口
        config.setPassword("1994xpp0805w."); // 替换为你的Redis服务器密码（如果有）

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        stringRedisTemplate = new StringRedisTemplate(factory);
        stringRedisTemplate.afterPropertiesSet();
    }

    @Test
    public void testRedisConnection() {

        try {

            // 设置一个临时键值对
            stringRedisTemplate.opsForValue().set("testKey", "testValue");

            // 获取这个键对应的值
            String value = stringRedisTemplate.opsForValue().get("testKey");

            // 删除这个键
            stringRedisTemplate.delete("testKey");

            System.out.println("Redis connection successful! Value retrieved: " + value);
        } catch (Exception e) {
            System.err.println("Failed to connect to Redis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

