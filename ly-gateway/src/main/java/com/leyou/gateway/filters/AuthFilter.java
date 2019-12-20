package com.leyou.gateway.filters;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@Slf4j
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties prop;

    @Autowired
    private FilterProperties filterProp;
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        //在处理请求表单数据之前执行
        return FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER - 1;
    }

    /**
     * 过滤是否生效,只过滤在白名单之外的请求
     * @return
     * @throws ZuulException
     */
    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取请求路径
        //uri代表请求地址'/'之间的部分,比如 http://www.leyou.com/search.html?key=小米手机  uri=search.html.
        String path = request.getRequestURI();
        //判断是否允许放行
        boolean flag = isAllowPath(path);
        //如果需要放行,这里应该返回false;反之,返回true;
        return !flag;
    }

    private boolean isAllowPath(String path) {
        //获取白名单
        List<String> allowPaths = filterProp.getAllowPaths();
        //遍历
        for (String allowPath : allowPaths) {
            //判断当前请求路径,是否以白名单的路径开头
            if (path.startsWith(allowPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 具体的拦截方法
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取用户cookie中的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            //校验token是否正确
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        } catch (Exception e) {
            //如果解析异常,证明登录过期或者没有登录
            //false代表拦截
            ctx.setSendZuulResponse(false);
            //返回状态码401
            ctx.setResponseStatusCode(401);
            //记录日志
            log.error("非法访问,未登录,地址:{}", request.getRemoteHost(), e);

        }
        return null;
    }
}
