package com.little.g.pay.api;

import com.little.g.common.ResultJson;
import com.little.g.common.validate.annatations.PayType;
import com.little.g.pay.dto.PayTypeDTO;
import com.little.g.thirdpay.dto.PayCallbackInfo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface LittlePayService {
    /**
     * 支付方式列表
     * @return
     */
    List<PayTypeDTO> typeList();

    /**
     * 支付参数生成
     * @param payType
     * @param preorderNo
     * @return
     */
    ResultJson prePay(@NotBlank Long uid, @NotEmpty String payType, @NotEmpty String preorderNo);

    /**
     * 使用三方支付订单
     * @param uid
     * @param payType
     * @param preorderNo
     * @return
     */
    ResultJson thirdpay(@NotBlank Long uid,@NotEmpty String payType, @NotEmpty  String preorderNo);

    /**
     * 余额支付
     * @param uid
     * @param preorderNo
     * @return
     */
    ResultJson pay(@NotBlank Long uid,@NotEmpty  String preorderNo);

    /**
     * 三方支付结果回调
     * @param payType
     * @param callbackInfo
     */
    void thirdpayCallback(@PayType String payType, @NotNull PayCallbackInfo callbackInfo);
}
