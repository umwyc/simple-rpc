package com.wyc.consumer.controller;

import com.wyc.common.model.User;
import com.wyc.common.service.UserService;
import com.wyc.simple.rpc.core.annotation.SimpleRpcReference;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    @SimpleRpcReference
    private UserService userService;

    @RequestMapping("/query")
    public User queryUserById(String id) {
        return userService.queryUserById(id);
    }

    @RequestMapping("/query_by_username")
    public User queryUserByUsername(String username) {
        return userService.queryUserByUsername(username);
    }

}
