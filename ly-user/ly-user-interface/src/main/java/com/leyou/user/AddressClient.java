package com.leyou.user;

import com.leyou.pojo.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("user-service")
public interface AddressClient {

    /**
     * 根据用户id查询收货地址
     * @param id
     * @return
     */
    @GetMapping("address/list")
    List<AddressDTO> queryAddress(@RequestParam("id")Long id);

    /**
     * 根据地址id查询地址信息
     * @param id
     * @return
     */
    @GetMapping("address/{id}")
    AddressDTO queryAddressById(@PathVariable("id") Long id);
}
