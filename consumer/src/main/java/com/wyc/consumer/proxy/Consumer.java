package com.wyc.consumer.proxy;

import com.wyc.common.service.OrderService;
import com.wyc.common.service.UserService;
import com.wyc.consumer.proxy.factory.ServiceProxyFactory;
import com.wyc.simple.rpc.core.SimpleRpcApplication;

public class Consumer {

    public static void main(String[] args) {
        // 初始化 rpc 框架
        SimpleRpcApplication.init();

        //
        boolean isHttp = true;

        // 获取代理对象并调用方法
        UserService userServiceProxy = ServiceProxyFactory.getProxy(UserService.class, isHttp);
        OrderService orderServiceProxy = ServiceProxyFactory.getProxy(OrderService.class, isHttp);
        System.out.println(userServiceProxy.queryUserById("111"));
        System.out.println(userServiceProxy.queryUserByUsername("orangeee"));
        System.out.println(orderServiceProxy.queryOrderByOrderSn("12345"));
        System.out.println(orderServiceProxy.queryOrderByUserId(1L));
    }

}
