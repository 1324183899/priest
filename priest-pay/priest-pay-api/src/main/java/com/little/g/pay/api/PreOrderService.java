package com.little.g.pay.api;

import com.little.g.pay.dto.PreorderDTO;
import com.little.g.pay.params.PreOrderParams;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 预支付订单服务
 */
public interface PreOrderService {
    /**
     * 创建预支付订单
     * @param preOrderParams
     * @return
     */
    PreorderDTO create(@NotNull @Valid PreOrderParams preOrderParams);

    /**
     * 获取预支付订单
     * @param mchId
     * @param preorderNo
     * @return
     */
    PreorderDTO get(@NotEmpty  String mchId,@NotEmpty String preorderNo);


    /**
     * 更新状态
     * @param uid 用户ID
     * @param preorderNo  预支付订单
     * @param status 状态值
     * @param thirdyPayNo 三方支付流水
     * @return
     */
    boolean updateStatus(@NotNull Long uid,@NotEmpty String preorderNo,Byte status,@NotEmpty String payType,String thirdyPayNo);


}
