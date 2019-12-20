package com.leyou.user.controller;

import com.leyou.pojo.AddressDTO;
import com.leyou.user.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/address/list")
    public ResponseEntity<List<AddressDTO>> queryAddress(@RequestParam("id") Long id) {
        return ResponseEntity.ok().body(addressService.queryAddress(id));
    }

    /**
     * 根据地址id查询地址信息
     *
     * @param id
     * @return
     */
    @GetMapping("/address/{id}")
    public ResponseEntity<AddressDTO> queryAddressById(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(addressService.queryAddressById(id));
    }

    /**
     * 新增收货地址
     *
     * @param addressDTO
     * @return
     */
    @PostMapping("/address")
    public ResponseEntity<Void> addAddress(@RequestBody AddressDTO addressDTO) {
        addressService.addAddress(addressDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
