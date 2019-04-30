package com.little.g.pay.api;

import com.little.g.common.ResultJson;
import com.little.g.pay.dto.PayTypeDTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public interface LittlePayService {

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
}
