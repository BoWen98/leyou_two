package com.leyou.user.service;

import com.leyou.pojo.AddressDTO;

import java.util.List;

public interface AddressService {
    List<AddressDTO> queryAddress(Long id);

    AddressDTO queryAddressById(Long id);

    void addAddress(AddressDTO addressDTO);
}
