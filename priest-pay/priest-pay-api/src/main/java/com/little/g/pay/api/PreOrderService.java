package com.little.g.pay.api;

import com.little.g.pay.dto.PreorderDTO;
import com.little.g.pay.params.PreOrderParams;

import javax.validation.Valid;

/**
 * 预支付订单服务
 */
public interface PreOrderService {
    /**
     * 创建预支付订单
     * @param preOrderParams
     * @return
     */
    PreorderDTO create(@Valid PreOrderParams preOrderParams);
}
