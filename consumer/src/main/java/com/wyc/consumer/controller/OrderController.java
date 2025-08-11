package com.wyc.consumer.controller;

import com.wyc.common.model.Order;
import com.wyc.common.service.OrderService;
import com.wyc.simple.rpc.core.annotation.SimpleRpcReference;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    @SimpleRpcReference
    OrderService orderService;

    @GetMapping("/query")
    public Order queryOrderByOrderSn(String orderSn) {
        return orderService.queryOrderByOrderSn(orderSn);
    }

    @GetMapping("/query_by_user_id")
    public List<Order> queryOrderByUserId(Long userId) {
        return orderService.queryOrderByUserId(userId);
    }

}
