package com.leyou.auth.service.impl;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.properties.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.pojo.User;
import com.leyou.user.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties prop;

    @Override
    public String login(String username, String password) {

        try {
            //远程调用user服务,校验用户名和密码
            User user = userClient.queryByUsernameAndPwd(username, password);
            //组织荷载
            UserInfo userInfo = new UserInfo(user.getId(), user.getUsername());
            //生成token并返回
            return JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }
}
