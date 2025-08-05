package com.wyc.common.service;

import com.wyc.common.model.Order;

import java.util.List;

public interface OrderService {

    // 通过 orderSn 查询订单信息
    Order queryOrderByOrderSn(String orderSn);

    // 通过 userId 查询订单信息
    List<Order> queryOrderByUserId(Long userId);
}
