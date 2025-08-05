package com.wyc.provider.impl;

import com.wyc.common.model.Order;
import com.wyc.common.service.OrderService;

import java.util.Collections;
import java.util.List;

public class OrderServiceImpl implements OrderService {
    @Override
    public Order queryOrderByOrderSn(String orderSn) {
        return new Order()
                .setId("1")
                .setOrderSn("xxx")
                .setUserId("10086");
    }

    @Override
    public List<Order> queryOrderByUserId(Long userId) {
        return Collections.singletonList(new Order().setId("2").setOrderSn("yyy").setUserId("12345"));
    }
}
