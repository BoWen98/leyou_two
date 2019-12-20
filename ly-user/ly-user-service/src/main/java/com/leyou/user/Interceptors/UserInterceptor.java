package com.leyou.user.Interceptors;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.config.JwtProperties;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties prop;

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    //定义一个线程域,存放登录用户,解决线程安全问题
    private static final ThreadLocal<UserInfo> TL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            //获取cookie中的token
            String token = CookieUtils.getCookieValue(request, prop.getCookieName());
            //解析成功,证明已登录
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            //放入线程域
            TL.set(userInfo);
            return true;
        } catch (Exception e) {
            //抛出异常,证明未登录,返回401
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

    //方法执行完成后把线程中存的值删除
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TL.remove();
    }

    //提供静态方法获取线程域中的user信息
    public static UserInfo getLoginUser() {
        return TL.get();
    }
}
