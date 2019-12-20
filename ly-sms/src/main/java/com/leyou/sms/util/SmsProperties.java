package com.leyou.sms.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ly.sms")
public class SmsProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String connectTimeOut;
    private String readTimeOut;
    private String signName;
    private String verifyTemplateCode;
}
