package com.leyou.search;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 测试redis
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testRedis() {
        redisTemplate.opsForValue().set("key1", "value1");
        String value1 = redisTemplate.opsForValue().get("key1");
        System.out.println(value1);
    }
    //测试redis
    @Test
    public void testRedis2() {
        //存储数据,并指定剩余生命时间
        redisTemplate.opsForValue().set("key2", "value2", 5, TimeUnit.HOURS);
    }
    //测试redis
    @Test
    public void testHash() {
        BoundHashOperations<String, Object, Object> user = redisTemplate.boundHashOps("user");
        //操作hash数据
        user.put("name", "jack");
        user.put("age","21");

        //获取单个数据
        Object name = user.get("name");
        System.out.println("name = " + name);

        //获取所有数据
        Map<Object, Object> map = user.entries();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}
