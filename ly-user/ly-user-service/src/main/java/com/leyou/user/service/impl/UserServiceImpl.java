package com.leyou.user.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.util.CodecUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.pojo.User;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private String KEY_PREFIX="user:code:phone:";
    //校验用户数据是否唯一,用户名和手机号码
    @Override
    public Boolean checkData(String data, Integer type) {
        User user = new User();
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.BAD_REQUEST);
        }
        return userMapper.selectCount(user) == 0;
    }

    /**
     * 生成并发送手机验证码
     * @param phone
     */
    @Override
    public void sendVerifyCode(String phone) {
        String key=KEY_PREFIX+phone;
        if (!phone.matches("^1[3456789]\\d{9}$")) {
            throw new LyException(HttpStatus.BAD_REQUEST,"手机号码不正确!");
        }
        //生成随机验证码
        String code = NumberUtils.generateCode(6);
        //验证码存入redis
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

        //发送mq,给sms发短信
        Map<String, String> map = new HashMap<>();
        map.put("phone", phone);
        map.put("code", code);
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", map);
    }

    /**
     * 用户注册
     * @param user
     * @param code
     */
    @Override
    public void register(User user, String code) {
        //补充字段
        user.setId(null);
        user.setCreated(new Date());
        //取出redis中的验证码
        String key = KEY_PREFIX + user.getPhone();
        String cacheCode = redisTemplate.opsForValue().get(key);
        if (!StringUtils.equals(code, cacheCode)) {
            throw new LyException(HttpStatus.BAD_REQUEST, "验证码不正确!");
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        //对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        //写入数据库
        int count = userMapper.insertSelective(user);
        if (count != 1) {
            throw new LyException(HttpStatus.BAD_REQUEST, "参数有误");
        }
        redisTemplate.delete(key);
    }

    /**
     * 根据用户名和密码查询用户
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public User queryUser(String username, String password) {
        User u = new User();
        u.setUsername(username);
        User user = userMapper.selectOne(u);
        //校验用户名
        if (null == user) {
            return null;
        }
        //校验密码
        if (!user.getPassword().equals(CodecUtils.md5Hex(password, user.getSalt()))) {
            return null;
        }
        //用户名和密码都正确
        return user;
    }
}
