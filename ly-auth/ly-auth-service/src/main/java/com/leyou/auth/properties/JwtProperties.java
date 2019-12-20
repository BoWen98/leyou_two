package com.leyou.auth.properties;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties("ly.jwt")
public class JwtProperties {
    //公钥地址
    private String publicFilePath;
    //私钥地址
    private String privateFilePath;
    //公钥
    private PublicKey publicKey;
    //私钥
    private PrivateKey privateKey;
    //有效时长
    private int expire;
    //cookie名称
    private String cookieName;

    //类一加载时就初始化公钥和私钥
    @PostConstruct
    public void init() {
        try {
            this.publicKey = RsaUtils.getPublicKey(publicFilePath);
            this.privateKey = RsaUtils.getPrivateKey(privateFilePath);
        } catch (Exception e) {
            log.error("【授权中心】加载公钥和私钥失败.", e);
            throw new RuntimeException("加载公钥和私钥失败.", e);
        }
    }
}
