package com.wyc.provider.impl;

import com.wyc.common.model.Order;
import com.wyc.common.service.OrderService;
import com.wyc.simple.rpc.core.annotation.SimpleRpcService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@SimpleRpcService(serviceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {
    @Override
    public Order queryOrderByOrderSn(String orderSn) {
        return new Order()
                .setId("1")
                .setOrderSn("orangeeeOrderSn")
                .setUserId("orangeee");
    }

    @Override
    public List<Order> queryOrderByUserId(Long userId) {
        return Arrays.asList(new Order().setId("2").setOrderSn("appleeeOrderSn").setUserId("appleee")
                , new Order().setId("3").setOrderSn("orangeeeOrderSn").setUserId("orangeee"));
    }
}
