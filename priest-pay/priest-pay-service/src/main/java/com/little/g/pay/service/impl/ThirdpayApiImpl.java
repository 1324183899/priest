package com.little.g.pay.service.impl;

import com.little.g.thirdpay.api.ThirdpayApi;
import com.little.g.thirdpay.dto.*;
import com.little.g.thirdpay.params.PrepayParams;
import com.little.g.thirdpay.params.QueryPayParams;
import com.little.g.thirdpay.params.QueryRefundParams;
import com.little.g.thirdpay.params.RefundParams;
import com.little.g.thirdpay.service.impl.ThirdPayFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Map;
@Service("thirdpayApi")
public class ThirdpayApiImpl implements ThirdpayApi {

    @Resource
    private ThirdPayFactory thirdPayFactory;

    @Override
    public PayCallbackInfo verifyBodyResponse(String payType, @NotEmpty String body) {
        return thirdPayFactory.getThirdPayService(payType).verifyBodyResponse(body);
    }

    @Override
    public PreRefundResult refundNotify(String payType, @NotEmpty String body) {
        return null;
    }

    @Override
    public PrePayResult prepay(String payType, @Valid PrepayParams prepayParams) {
        return thirdPayFactory.getThirdPayService(payType).prepay(prepayParams);
    }

    @Override
    public PayCallbackInfo queryPay(String payType, @Valid QueryPayParams params) {
        return thirdPayFactory.getThirdPayService(payType).queryPay(params);
    }

    @Override
    public PayCallbackInfo verifyResponse(String payType, Map<String, String> params) {
        return thirdPayFactory.getThirdPayService(payType).verifyResponse(params);
    }

    @Override
    public PreRefundResult refund(String payType, @Valid RefundParams params) {
        return thirdPayFactory.getThirdPayService(payType).refund(params);
    }

    @Override
    public RefundResult queryRefund(String payType, @Valid QueryRefundParams params) {
        return thirdPayFactory.getThirdPayService(payType).queryRefund(params);
    }

    @Override
    public PayResponseInfo successPayResponse(String payType) {
        return thirdPayFactory.getThirdPayService(payType).successPayResponse();
    }
}
