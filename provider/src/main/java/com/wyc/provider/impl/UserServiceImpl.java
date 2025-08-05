package com.wyc.provider.impl;

import com.wyc.common.model.User;
import com.wyc.common.service.UserService;

/**
 * 实现UserService接口
 */
public class UserServiceImpl implements UserService {

    @Override
    public User queryUserById(String id) {
        return new User()
                .setId("10086")
                .setName("orangeee")
                .setEmail("orangeee@qq.com");

    }

    @Override
    public User queryUserByUsername(String username) {
        return new User()
                .setId("12345")
                .setEmail("bigorange")
                .setEmail("bigorange@qq.com");
    }
}
