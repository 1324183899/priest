package com.little.g.thirdpay.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.little.g.common.utils.JSR303Util;
import com.little.g.common.utils.MoneyUtil;
import com.little.g.pay.PayErrorCodes;
import com.little.g.thirdpay.dto.*;
import com.little.g.common.enums.PayType;
import com.little.g.thirdpay.enums.ThirdPayStatus;
import com.little.g.thirdpay.enums.ThirdRefundStatus;
import com.little.g.thirdpay.exception.PayException;
import com.little.g.thirdpay.params.PrepayParams;
import com.little.g.thirdpay.params.QueryPayParams;
import com.little.g.thirdpay.params.QueryRefundParams;
import com.little.g.thirdpay.params.RefundParams;
import com.little.g.thirdpay.service.api.ThirdPayService;
import com.little.g.thirdpay.service.config.AlipayConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;


public class AlipayServiceImpl extends ThirdPayService {

    private static final Logger logger = LoggerFactory.getLogger(AlipayServiceImpl.class);

    AlipayClient alipayClient;

    AlipayConfig config;


    public AlipayServiceImpl(AlipayConfig config) {
        String valid=JSR303Util.validateParams(config);
        if(StringUtils.isNotEmpty(valid)){
            throw new PayException(PayErrorCodes.THIRDPAY_ERROR,valid);
        }
        this.config=config;
        alipayClient=new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", config.getAppid(), config.getPrivatekey(), "json", "UTF-8", config.getPublickey(), "RSA2");

    }


    private PayResponseInfo SUCCESS_RESPONSE = new PayResponseInfo("success");

    @Override
    public void setPayChannel(String payChannel) {
        this.payChannel= PayType.ALIPAY;
    }


    public PrePayResult prepay(PrepayParams prepayParams) {
        //实例化客户端
        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setSubject(config.getAppSubject());
        model.setBody(prepayParams.getComment());
        model.setOutTradeNo(prepayParams.getTradeno());
        model.setTimeoutExpress(config.getTimeout());
        model.setTotalAmount(String.valueOf(MoneyUtil.long2Double(prepayParams.getMoney())));
        model.setProductCode("QUICK_MSECURITY_PAY");
        request.setBizModel(model);
        request.setNotifyUrl(config.getNotifyurl());
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            return new PrePayResult(response.getOutTradeNo(), response.getBody(),response.getTradeNo());
        } catch (AlipayApiException e) {
            throw new PayException(e);
        }
    }


    public PayCallbackInfo queryPay(QueryPayParams params){
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(params.getPayOrderId());
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            PayCallbackInfo callback = new PayCallbackInfo();
            Date gmtPayment = response.getSendPayDate();
            callback.setOutPayOrderId(response.getTradeNo());
            callback.setKeyData(response.getBuyerLogonId());
            callback.setPayOrderId(response.getOutTradeNo());
            callback.setRealFee(MoneyUtil.double2Long(new Double(response.getTotalAmount())));
            callback.setThirdPayStatus(genThirdStatus(response.getTradeStatus()));
            if(gmtPayment != null){
                callback.setPayTime(gmtPayment.getTime());
            }

            return callback;

        } catch (AlipayApiException e) {
            throw new PayException(e);
        }

    }

    private ThirdPayStatus genThirdStatus(String status) {
        ThirdPayStatus payStatus = ThirdPayStatus.UNKNOWN;
        if ("WAIT_BUYER_PAY".equals(status)) {
            payStatus = ThirdPayStatus.WAIT_PAY;
        } else if ("TRADE_SUCCESS".equals(status)
                || "TRADE_FINISHED".equals(status)) { // 无法处理的状态
            payStatus = ThirdPayStatus.SUCCESS;
        }
        return payStatus;
    }


    public PayCallbackInfo verifyResponse(Map<String,String> params){
        if(params==null || params.size()<=0){
            throw new PayException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.notify.empty");
        }
        try {

            if(!AlipaySignature.rsaCheckV1(params, config.getPublickey(), "UTF-8","RSA2")){
                throw new PayException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.notify.invalid");
            }
            PayCallbackInfo callbackInfo = new PayCallbackInfo();

            callbackInfo.setOutPayOrderId(params.get("trade_no"));
            callbackInfo.setPayOrderId(params.get("out_trade_no"));
            callbackInfo.setKeyData(params.get("buyer_logon_id"));
            callbackInfo.setRealFee(MoneyUtil.double2Long(new Double(params.get("total_amount"))));
            if (StringUtils.isNotEmpty(params.get("gmt_payment"))) {
                try {
                    callbackInfo.setPayTime(DateUtils.parseDate(params.get("gmt_payment"), "yyyy-MM-dd HH:mm:ss").getTime());
                    callbackInfo.setFinishTime(callbackInfo.getPayTime());
                } catch (ParseException e) {
                    logger.error("parse paytime error.paytime:{}", params.get("gmt_payment"), e);
                }
            }
            String status = params.get("trade_status");
            callbackInfo.setThirdPayStatus(genThirdStatus(status));
            return callbackInfo;

        } catch (AlipayApiException e) {
            throw new PayException(e);
        }
    }




    public PreRefundResult refund(RefundParams params){
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(params.getPayOrderId()); //与预授权转支付商户订单号相同，代表对该笔交易退款
        model.setTradeNo(params.getOutPayOrderId());
        model.setRefundAmount(String.valueOf(MoneyUtil.long2Double(params.getRefundFee())));
        model.setRefundReason(params.getRefundReason());
        if(!StringUtils.isEmpty(params.getRefundId())) {
            model.setOutRequestNo(params.getRefundId());//标识一次退款请求，同一笔交易多次退款需要保证唯一，如部分退款则此参数必传。
        }

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        try {
            AlipayTradeRefundResponse response = alipayClient.execute(request);

            PreRefundResult preRefundResult = new PreRefundResult();
            if (response.isSuccess()) {
                preRefundResult.setSuccessOp(true);
                preRefundResult.setRefundId(params.getRefundId());
                preRefundResult.setOutRefundId(params.getRefundId()); //支付宝没有外部编码
            } else {
                preRefundResult.setSuccessOp(false);
                preRefundResult.setErrorCode(response.getSubCode());
                preRefundResult.setErrorMsg(response.getSubMsg());
            }
            return preRefundResult;
        } catch (AlipayApiException e) {
            throw new PayException(e);
        }

    }

    public RefundResult queryRefund(QueryRefundParams params){
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        if(StringUtils.isEmpty(params.getRefundId())){
            model.setOutRequestNo(params.getRefundId());
        }

        model.setOutTradeNo(params.getPayOrderId());
        model.setTradeNo(params.getOutPayOrderId());
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizModel(model);
        AlipayTradeFastpayRefundQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            RefundResult result = new RefundResult();
            result.setRefundId(params.getRefundId());
            result.setOutRefundId(params.getOutRefundId());
            if (response.isSuccess()) {
                result.setRefundId(response.getOutRequestNo());
                result.setOutRefundId(response.getOutRequestNo());
                String refund_amount = response.getRefundAmount();
                if (StringUtils.isNotEmpty(refund_amount)) {
                    result.setRefundFee(MoneyUtil.double2Long(new Double(refund_amount)));
                    result.setThirdRefundStatus(ThirdRefundStatus.SUCCESS);
                } else {
                    result.setThirdRefundStatus(ThirdRefundStatus.REFUNDING);
                }
            } else if ("40004".equals(response.getCode())) { //40004 业务处理失败
                result.setThirdRefundStatus(ThirdRefundStatus.FAIL);
                result.setErrorCode(response.getSubCode());
                result.setErrorMsg(response.getSubMsg());
            } else {
                result.setThirdRefundStatus(ThirdRefundStatus.REFUNDING);
            }
            return result;
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PayResponseInfo successPayResponse() {
        return SUCCESS_RESPONSE;
    }
}
