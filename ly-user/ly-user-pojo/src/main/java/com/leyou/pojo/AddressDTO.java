package com.leyou.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_address")
public class AddressDTO {
    @Id
    private Long id;
    //    @Transient
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String state;
    private String city;
    private String district;
    private String address;
    private String alias;
    private Integer isDefault;
}
