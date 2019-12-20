package com.leyou.user.service.impl;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.pojo.AddressDTO;
import com.leyou.user.Interceptors.UserInterceptor;
import com.leyou.user.mapper.AddressMapper;
import com.leyou.user.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<AddressDTO> queryAddress(Long id) {
        AddressDTO a = new AddressDTO();
        a.setUserId(id);
        List<AddressDTO> addressList = addressMapper.select(a);
        if (CollectionUtils.isEmpty(addressList)) {
            throw new LyException(ExceptionEnum.ADDRESS_NOT_FOUND);
        }
        return addressList;
    }

    /**
     * 根据地址id查询地址信息
     * @param id
     * @return
     */
    @Override
    public AddressDTO queryAddressById(Long id) {
        AddressDTO addressDTO = addressMapper.selectByPrimaryKey(id);
        if (null == addressDTO) {
            throw new LyException(ExceptionEnum.ADDRESS_NOT_FOUND);
        }
        return addressDTO;
    }

    /**
     * 新增收货地址
     * @param addressDTO
     * @return
     */
    @Override
    public void addAddress(AddressDTO addressDTO) {
        //获取登录用户的信息
        UserInfo loginUser = UserInterceptor.getLoginUser();
        Long userId = loginUser.getId();
        addressDTO.setUserId(userId);
        addressDTO.setIsDefault(0);
        int count = addressMapper.insert(addressDTO);
        if (count != 1) {
            throw new LyException(ExceptionEnum.ADDRESS_ADD_ERROR);
        }
    }
}
