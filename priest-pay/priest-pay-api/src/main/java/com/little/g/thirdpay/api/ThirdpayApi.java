package com.little.g.thirdpay.api;

import com.little.g.common.validate.annatations.PayType;
import com.little.g.pay.PayErrorCodes;
import com.little.g.thirdpay.dto.*;
import com.little.g.thirdpay.exception.PayException;
import com.little.g.thirdpay.params.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Map;

public interface ThirdpayApi {

    /**
     * 企业付款
     */
    default  EntPayResult entPay(@PayType String payType, @Valid EntpayParams params){
        throw new PayException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.not.support");
    }

    /**
     * 支付回调
     * @param body
     * @return
     */
    PayCallbackInfo verifyBodyResponse(@PayType String payType,@NotEmpty String body);
    /**
     * 退款通知
     * @param body
     * @return
     */
    PreRefundResult refundNotify(@PayType String payType,@NotEmpty String body);
    /**
     * 预支付下单
     * @param prepayParams
     * @return
     */
    PrePayResult prepay(@PayType String payType,@Valid PrepayParams prepayParams);
    /**
     * 查询预支付订单
     * @param params
     * @return
     */
    PayCallbackInfo queryPay(@PayType String payType,@Valid QueryPayParams params);

    /**
     * 支付回调
     * @param params
     * @return
     */
    PayCallbackInfo verifyResponse(@PayType String payType,Map<String,String> params);

    /**
     * 支付退款
     * @param params
     * @return
     */
    PreRefundResult refund(@PayType String payType,@Valid RefundParams params);

    /**
     * 查询支付退款
     * @param params
     * @return
     */
    RefundResult queryRefund(@PayType String payType,@Valid QueryRefundParams params);

    /**
     * 支付成功回调
     * @param payType
     * @return
     */
    PayResponseInfo successPayResponse(@PayType String payType);

}
