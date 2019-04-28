package com.little.g.thirdpay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.entpay.EntPayRequest;
import com.github.binarywang.wxpay.bean.entpay.EntPayResult;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayAppOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.little.g.common.enums.PayType;
import com.little.g.common.utils.CommonUtils;
import com.little.g.common.utils.JSR303Util;
import com.little.g.pay.PayErrorCodes;
import com.little.g.thirdpay.dto.PayCallbackInfo;
import com.little.g.thirdpay.dto.PrePayResult;
import com.little.g.thirdpay.dto.PreRefundResult;
import com.little.g.thirdpay.dto.RefundResult;
import com.little.g.thirdpay.enums.ThirdPayStatus;
import com.little.g.thirdpay.enums.ThirdRefundStatus;
import com.little.g.thirdpay.exception.PayException;
import com.little.g.thirdpay.params.*;
import com.little.g.thirdpay.service.api.ThirdPayService;
import com.little.g.thirdpay.service.config.WxpayConfig;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class WxpayServiceImpl extends ThirdPayService {


    private static final Logger logger = LoggerFactory.getLogger(WxpayServiceImpl.class);

    private WxpayConfig config;

    WxPayService wxPayService=new WxPayServiceImpl();

    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public WxpayServiceImpl(WxpayConfig config) {
        String valid=JSR303Util.validateParams(config);
        if(StringUtils.isNotEmpty(valid)){
            throw new PayException(PayErrorCodes.THIRDPAY_ERROR,valid);
        }
        WxPayConfig payConfig=new WxPayConfig();
        BeanUtils.copyProperties(config,payConfig);
        wxPayService.setConfig(payConfig);
        this.config=config;

    }

    @Override
    public void setPayChannel(String payChannel) {
        this.payChannel= PayType.WEXINPAY;
    }

    @Override
    public PrePayResult prepay(PrepayParams prepayParams) {

        WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
        orderRequest.setBody(prepayParams.getComment());
        orderRequest.setOutTradeNo(prepayParams.getTradeno());
        orderRequest.setTotalFee(prepayParams.getMoney().intValue());
        orderRequest.setOpenid(prepayParams.getOpenid());
        orderRequest.setSpbillCreateIp(CommonUtils.getLocalIp());
        orderRequest.setNotifyUrl(config.getNotifyUrl());
        orderRequest.setTradeType(config.getTradeType());
        Calendar c=Calendar.getInstance();
        orderRequest.setTimeStart(DateFormatUtils.format(c.getTime(),"yyyyMMddHHmmss"));
        //设置订单超时时间
        c.add(Calendar.HOUR,1);
        orderRequest.setTimeExpire(DateFormatUtils.format(c.getTime(),"yyyyMMddHHmmss"));
        try {
            WxPayAppOrderResult unifiOrder=wxPayService.createOrder(orderRequest);
            return new PrePayResult(prepayParams.getTradeno(),JSONObject.toJSONString(unifiOrder),unifiOrder.getPrepayId());
        } catch (WxPayException e) {
            throw new PayException(e);
        }
    }

    @Override
    public PayCallbackInfo queryPay(QueryPayParams params) {

        try {
            WxPayOrderQueryResult result=wxPayService.queryOrder(params.getOutPayOrderId(),params.getPayOrderId());
            PayCallbackInfo callback = getPayCallbackInfo(result.getOpenid(), result.getIsSubscribe(), result.getBankType(), result.getOutTradeNo(), result.getTransactionId(), result.getTotalFee(), result.getTradeState(), result.getErrCode(), result.getTimeEnd());
            return callback;
        } catch (WxPayException e) {
            throw new PayException(e);
        }
    }

    private PayCallbackInfo getPayCallbackInfo(String openid, String isSubscribe, String bankType, String outTradeNo, String transactionId, Integer totalFee, String tradeState, String errCode, String timeEnd) {
        PayCallbackInfo callback = new PayCallbackInfo();
        callback.setKeyData(openid + ":" + isSubscribe + ":" + bankType);
        callback.setPayOrderId(outTradeNo);
        callback.setOutPayOrderId(transactionId);
        if (totalFee != null) {
            callback.setRealFee(totalFee.longValue());
        }
        callback.setThirdPayStatus(genPayStatus(tradeState));
        callback.setFailCode(errCode);
        try {
            if(timeEnd != null) {
                callback.setPayTime(dateFormat.parse(timeEnd).getTime());
            }
        } catch (ParseException e) {
            logger.error("parseTimeError,timeEnd:{}", timeEnd, e);
        }
        return callback;
    }


    private ThirdPayStatus genPayStatus(String tradeState) {
        ThirdPayStatus status = ThirdPayStatus.UNKNOWN;
        if (StringUtils.isEmpty(tradeState)) {
            return status;
        }
        if (StringUtils.equals("SUCCESS", tradeState)) {
            status = ThirdPayStatus.SUCCESS;
        } else if (StringUtils.equals("REFUND", tradeState)) {
            status = ThirdPayStatus.REFUND;
        } else if (StringUtils.equals("NOTPAY", tradeState)) {
            status = ThirdPayStatus.WAIT_PAY;
        } else if (StringUtils.equals("CLOSED", tradeState)) {
            status = ThirdPayStatus.CANCEL;
        } else if (StringUtils.equals("REVOKED", tradeState)) {
            status = ThirdPayStatus.CANCEL;
        } else if (StringUtils.equals("USERPAYING", tradeState)) {
            status = ThirdPayStatus.PAYING;
        } else if (StringUtils.equals("PAYERROR", tradeState)) {
            status = ThirdPayStatus.FAIL;
        }
        return status;
    }

    @Override
    public PayCallbackInfo verifyResponse(Map<String, String> params) {
        return null;
    }

    @Override
    public PayCallbackInfo verifyBodyResponse(String body) {
        if(StringUtils.isEmpty(body)){
            throw new PayException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.notify.empty");
        }
        try {
            final WxPayOrderNotifyResult result = wxPayService.parseOrderNotifyResult(body);
            PayCallbackInfo callback = getPayCallbackInfo(result.getOpenid(), result.getIsSubscribe(), result.getBankType(), result.getOutTradeNo(), result.getTransactionId(), result.getTotalFee(), result.getResultCode(), result.getErrCode(), result.getTimeEnd());
            return callback;
        } catch (WxPayException e) {
            throw new PayException(e);
        }
    }

    @Override
    public PreRefundResult refund(RefundParams params) {
        WxPayRefundRequest request=new WxPayRefundRequest();
        request.setNotifyUrl(config.getRefundUrl());
        request.setOutRefundNo(params.getRefundId());
        request.setOutTradeNo(params.getPayOrderId());
        request.setRefundDesc(params.getRefundReason());
        request.setRefundFee(params.getRefundFee().intValue());
        request.setTotalFee(params.getPayTotalFee().intValue());
        request.setTransactionId(params.getOutPayOrderId());
        try {
            WxPayRefundResult refundReponse=wxPayService.refund(request);

            PreRefundResult result = new PreRefundResult();
            result.setRefundId(refundReponse.getRefundId());
            result.setOutRefundId(refundReponse.getRefundId());
            if (StringUtils.equals("SUCCESS", refundReponse.getReturnCode())) {
                if (StringUtils.equals("SUCCESS", refundReponse.getResultCode())) {
                    result.setSuccessOp(true);
                } else {
                    result.setSuccessOp(false);
                    result.setErrorCode(refundReponse.getErrCode());
                    result.setErrorMsg(refundReponse.getErrCodeDes());
                }
            } else {
                result.setSuccessOp(false);
                result.setErrorCode(refundReponse.getReturnCode());
                result.setErrorMsg(refundReponse.getReturnMsg());
            }

            return result;

        } catch (WxPayException e) {
            throw new PayException(e);
        }
    }

    @Override
    public RefundResult queryRefund(QueryRefundParams params) {
        try {
            WxPayRefundQueryResult queryRefundResponse=this.wxPayService.refundQuery(params.getOutPayOrderId(), params.getPayOrderId(), params.getOutRefundId(), params.getRefundId());

            List<WxPayRefundQueryResult.RefundRecord> refundRecords=queryRefundResponse.getRefundRecords();
            if(CollectionUtils.isEmpty(refundRecords)){
                throw new PayException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.empty.refund");
            }
            //TODO:暂时只实现单条逻辑
            WxPayRefundQueryResult.RefundRecord record=refundRecords.get(0);


            RefundResult refundResult = new RefundResult();
            refundResult.setRefundId(record.getRefundId());
            refundResult.setOutRefundId(record.getOutRefundNo());
            refundResult.setRefundFee(record.getRefundFee().longValue());
            if (StringUtils.equals("SUCCESS", queryRefundResponse.getReturnCode())) {
                refundResult.setErrorCode(queryRefundResponse.getErrCode());
                refundResult.setErrorMsg(queryRefundResponse.getErrCodeDes());
            } else {
                refundResult.setErrorCode(queryRefundResponse.getReturnCode());
                refundResult.setErrorMsg(queryRefundResponse.getReturnMsg());
            }
            String status = record.getRefundStatus();
            if (StringUtils.equals("PROCESSING", status)) {
                refundResult.setThirdRefundStatus(ThirdRefundStatus.REFUNDING);
            } else if (StringUtils.equals("SUCCESS", status)) {
                refundResult.setThirdRefundStatus(ThirdRefundStatus.SUCCESS);
            } else if (StringUtils.equals("FAIL", status)) {
                refundResult.setThirdRefundStatus(ThirdRefundStatus.FAIL);
            } else if (StringUtils.equals("CHANGE", status)) {
                refundResult.setThirdRefundStatus(ThirdRefundStatus.TURN_TO_USER);
            } else if (StringUtils.equals("NOTSURE", status)) {
                refundResult.setThirdRefundStatus(ThirdRefundStatus.TURN_TO_USER); //不确定状态,转人工处理
            }
            return refundResult;

        } catch (WxPayException e) {
            throw new PayException(e);
        }
    }


    @Override
    public PreRefundResult refundNotify(String body) {
        if(StringUtils.isEmpty(body)){
            throw new PayException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.notify.empty");
        }

        try {
            final WxPayRefundNotifyResult wxpayNotify = this.wxPayService.parseRefundNotifyResult(body);
            WxPayRefundNotifyResult.ReqInfo reqInfo=wxpayNotify.getReqInfo();
            PreRefundResult result = new PreRefundResult();
            result.setRefundId(result.getRefundId());
            result.setOutRefundId(result.getRefundId());
            if (StringUtils.equals("SUCCESS", wxpayNotify.getReturnCode())) {
                if (StringUtils.equals("SUCCESS", reqInfo.getRefundStatus())) {
                    result.setSuccessOp(true);
                } else {
                    result.setSuccessOp(false);
                    result.setErrorCode(wxpayNotify.getErrCode());
                    result.setErrorMsg(wxpayNotify.getErrCodeDes());
                }
            } else {
                result.setSuccessOp(false);
                result.setErrorCode(wxpayNotify.getReturnCode());
                result.setErrorMsg(wxpayNotify.getReturnMsg());
            }

            return result;
        } catch (WxPayException e) {
            throw new PayException(e);
        }

    }

    @Override
    public com.little.g.thirdpay.dto.EntPayResult entPay(EntpayParams params) {

        EntPayRequest request=new EntPayRequest();
        request.setAmount(params.getAmount().intValue());
        request.setDescription(params.getComment());
        request.setPartnerTradeNo(params.getTradeNo());
        request.setOpenid(params.getOpenid());
        request.setCheckName("NO_CHECK");
        request.setAmount(params.getAmount().intValue());
        request.setSpbillCreateIp(CommonUtils.getLocalIp());
        try {
            EntPayResult result=this.wxPayService.getEntPayService().entPay(request);

            com.little.g.thirdpay.dto.EntPayResult entPayResult=new com.little.g.thirdpay.dto.EntPayResult();
            entPayResult.setOutTradeNo(result.getPaymentNo());
            entPayResult.setTradeNo(result.getPartnerTradeNo());
            entPayResult.setPaymentTime(result.getPaymentTime());
            return entPayResult;
        } catch (WxPayException e) {
            throw new PayException(e);
        }

    }
}
