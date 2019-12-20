package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter(CorsProperties prop) {
        //1.添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        //允许的域,一定不要写*,否则cookie就无法使用了
        prop.getAllowedOrigins().forEach(config::addAllowedOrigin);
        //是否发送cookie信息
        config.setAllowCredentials(prop.getAllowCredentials());
        //允许的请求方式
        prop.getAllowedMethods().forEach(config::addAllowedMethod);
        //允许的头信息
        config.addAllowedHeader("*");
        //配置有效期
        config.setMaxAge(prop.getMaxAge());
        //添加映射路径
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration(prop.getPath(), config);

        //返回新的CorsFilter
        return new CorsFilter(configSource);
    }
}
