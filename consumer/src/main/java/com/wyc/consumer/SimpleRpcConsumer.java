package com.wyc.consumer;

import com.wyc.common.model.User;
import com.wyc.common.service.UserService;
import com.wyc.consumer.proxy.ServiceProxyFactory;
import com.wyc.simple.rpc.core.SimpleRpcApplication;


public class SimpleRpcConsumer {
    public static void main(String[] args) {
        // 加载序列化器
        SimpleRpcApplication.getSimpleRpcConfig();
        // 获取对应的代理对象
        boolean isHttp = false;
        UserService userService = ServiceProxyFactory.getProxy(UserService.class, isHttp);
        User user = new User();
        user.setName("orange");
        // 调用代理方法
        int number = userService.getNumber();
        User user1 = userService.getUser(user);
        // 查看结果
        System.out.println(number);
        System.out.println(user1.getName());
    }
}
