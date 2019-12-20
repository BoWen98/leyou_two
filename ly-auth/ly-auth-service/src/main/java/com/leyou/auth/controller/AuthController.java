package com.leyou.auth.controller;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.properties.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    /**
     * 登录
     * @param username
     * @param password
     * @param response
     * @param request
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      HttpServletResponse response, HttpServletRequest request) {
        //登录
        String token=authService.login(username, password);
        //把token写入cookie
        CookieUtils.newBuilder().name(prop.getCookieName()).value(token)
                .httpOnly(true).request(request).response(response).build();
        //返回
        return ResponseEntity.ok().build();
    }

    /**
     * 校验token信息
     * @param token
     * @param response
     * @param request
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token,
                                           HttpServletResponse response, HttpServletRequest request) {
        try {
            //校验token
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            //登录有效,刷新生成新token
            CookieUtils.newBuilder().name(prop.getCookieName()).value(token)
                    .httpOnly(true).request(request).response(response).build();
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

    /**
     * 注销
     * @param token
     * @param response
     * @param request
     * @return
     */
    @GetMapping("loginOut")
    public ResponseEntity<Void> loginOut(@CookieValue("LY_TOKEN") String token,
                                         HttpServletResponse response, HttpServletRequest request) {
        try {

            CookieUtils.newBuilder().name(prop.getCookieName()).value(token)
                    .httpOnly(true).request(request).response(response).maxAge(0).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

}
