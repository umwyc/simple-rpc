package com.wyc.provider.impl;

import com.wyc.common.model.User;
import com.wyc.common.service.UserService;

/**
 * 实现UserService接口
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("[User Name: "  + user.getName() + " ]");
        return user;
    }
}
