package com.leyou.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
@Table(name = "tb_user")
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    @Length(min = 4,max = 32,message = "用户名只能在4-32位之间")
    private String username;

    @JsonIgnore
    @Length(min = 4,max = 32,message = "密码只能在4-32位之间")
    private String password;

    @Pattern(regexp = "^1[35678]\\d{9}$",message = "手机号格式不正确")
    private String phone;

    private Date created;

    @JsonIgnore
    private String salt;
    //说明:为了安全起见,对password和salt两个字段加jsonIgnore注解,这样序列化时这两个字段不会被返回

}
