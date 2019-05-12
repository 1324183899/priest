package com.little.g.pay.api;

import com.little.g.common.validate.annatations.PayType;
import com.little.g.pay.dto.ChargeRecordDTO;
import com.little.g.pay.dto.OrderResult;
import com.little.g.pay.params.ChargeParams;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public interface ChargeService {

    ChargeRecordDTO get(@NotNull Long uid,@NotEmpty String preorderNo);


    ChargeRecordDTO create(@NotNull @Valid ChargeParams params);

    /**
     * 创建充值订单
     * @param uid
     * @param money
     * @return
     */
    OrderResult createChargeOrder(@NotNull Long uid, @NotNull @Min(1)Long money);

    /**
     *  更新充值成功状态
     * @param preorderNo
     * @param payType
     * @param thirdyPayNo
     * @return
     */
    ChargeRecordDTO chargeSuccess(@NotEmpty String preorderNo,@PayType String payType,@NotEmpty String thirdyPayNo);
}
