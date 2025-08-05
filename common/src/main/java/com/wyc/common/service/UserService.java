package com.wyc.common.service;

import com.wyc.common.model.User;

public interface UserService {

    // 通过 id 获取用户信息
    User queryUserById(String id);

    // 通过 username 获取用户信息
    User queryUserByUsername(String username);

}
