package com.wyc.common.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Order {

    private String id;

    private String orderSn;

    private String userId;
}
