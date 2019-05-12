package com.little.g.thirdpay.service.api;

import com.little.g.common.enums.PayType;
import com.little.g.common.exception.ServiceDataException;
import com.little.g.common.utils.ReflectionUtil;
import com.little.g.pay.PayErrorCodes;
import com.little.g.thirdpay.dto.*;
import com.little.g.thirdpay.exception.PayException;
import com.little.g.thirdpay.params.*;
import com.little.g.thirdpay.service.config.AlipayConfig;
import com.little.g.thirdpay.service.config.WxpayConfig;
import com.little.g.thirdpay.service.impl.AlipayServiceImpl;
import com.little.g.thirdpay.service.impl.WxpayServiceImpl;

import java.util.Map;

public abstract class ThirdPayService {

    protected String payChannel;

    public static ThirdPayService newThirdPayService(String code, Map<String, String> confs) {
        if(confs == null || confs.size() <=0){
            throw new ServiceDataException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.paychannel.config.null");
        }
        switch (code){
            case PayType.ALIPAY:
                AlipayConfig config=new AlipayConfig();
                ReflectionUtil.copyMapToObject(confs,config);
                return new AlipayServiceImpl(config);
            case PayType.WEXINPAY:
                WxpayConfig wxpayConfig = new WxpayConfig();
                ReflectionUtil.copyMapToObject(confs,wxpayConfig);
                return new WxpayServiceImpl(wxpayConfig);


        }
        return null;
    }



    public String getPayChannel() {
        return payChannel;
    }

    /**
     * 企业付款
     */
    public EntPayResult entPay(EntpayParams params){
        throw new PayException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.not.support");
    }

    /**
     * 支付回调
     * @param body
     * @return
     */
    public  PayCallbackInfo verifyBodyResponse(String body){
        throw new PayException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.not.support");
    }


    public PreRefundResult refundNotify(String body){
        throw new PayException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.not.support");
    }


    public abstract  void setPayChannel(String payChannel);

    /**
     * 预支付下单
     * @param prepayParams
     * @return
     */
    public abstract PrePayResult prepay(PrepayParams prepayParams);

    /**
     * 查询预支付订单
     * @param params
     * @return
     */
    public abstract PayCallbackInfo queryPay(QueryPayParams params);

    /**
     * 支付回调
     * @param params
     * @return
     */
    public abstract PayCallbackInfo verifyResponse(Map<String,String> params);

    /**
     * 支付退款
     * @param params
     * @return
     */
    public abstract PreRefundResult refund(RefundParams params);

    /**
     * 查询支付退款
     * @param params
     * @return
     */
    public abstract RefundResult queryRefund(QueryRefundParams params);


    public abstract PayResponseInfo successPayResponse();







}
