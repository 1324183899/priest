package com.little.g.pay.service.impl;

import com.little.g.common.ResultJson;
import com.little.g.common.enums.PayType;
import com.little.g.common.enums.StatusEnum;
import com.little.g.common.exception.ServiceDataException;
import com.little.g.pay.PayErrorCodes;
import com.little.g.pay.api.ChargeService;
import com.little.g.pay.api.LittlePayService;
import com.little.g.pay.api.PreOrderService;
import com.little.g.pay.api.TransactionService;
import com.little.g.pay.dto.*;
import com.little.g.pay.enums.BusinessType;
import com.little.g.pay.enums.FixAccount;
import com.little.g.pay.enums.MerchantId;
import com.little.g.pay.enums.TradeType;
import com.little.g.pay.model.Preorder;
import com.little.g.pay.params.ChargeParams;
import com.little.g.thirdpay.api.ThirdpayApi;
import com.little.g.thirdpay.dto.PayCallbackInfo;
import com.little.g.thirdpay.dto.PrePayResult;
import com.little.g.thirdpay.enums.ThirdPayStatus;
import com.little.g.thirdpay.model.PayChannel;
import com.little.g.thirdpay.params.PrepayParams;
import com.little.g.thirdpay.service.impl.ThirdPayFactory;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service("littlePayService")
public class LittlePayServiceImpl implements LittlePayService {

    private static final Logger log = LoggerFactory.getLogger(LittlePayServiceImpl.class);

    static List<PayTypeDTO> payTypeList = new ArrayList<>();
    @Resource
    private ThirdPayFactory thirdPayFactory;
    @Resource
    private PreOrderService preOrderService;
    @Resource
    private ThirdpayApi thirdpayApi;
    @Resource
    private ChargeService chargeService;
    @Resource
    private TransactionService transactionService;

    @PostConstruct
    public void init(){

        payTypeList.add(new PayTypeDTO("balance","余额支付",null));

        List<PayChannel> channelList =  thirdPayFactory.getChannelList();
        if(CollectionUtils.isNotEmpty(channelList)){
            for(PayChannel channel:channelList){
                payTypeList.add(new PayTypeDTO(channel.getCode(),channel.getName(),null));
            }

        }
    }

    @Override
    public List<PayTypeDTO> typeList() {
        return payTypeList;
    }

    @Override
    public ResultJson prePay(Long uid,@NotEmpty String payType, @NotEmpty String preorderNo) {
        ResultJson result=new ResultJson();
        PreorderDTO preorderDTO = getUserPreorder(uid, preorderNo);

        PrepayParams params=new PrepayParams();
        params.setTradeno(preorderNo);
        params.setComment(preorderDTO.getSubject());
        params.setMoney(preorderDTO.getTotalFee());

        PrePayResult prePayResult=thirdpayApi.prepay(payType,params);
        result.setData(prePayResult);

        return result;
    }

    private PreorderDTO getUserPreorder(Long uid, @NotEmpty String preorderNo) {
        PreorderDTO preorderDTO=preOrderService.get(MerchantId.LittelG.getValue(),preorderNo);
        if(preorderDTO == null){
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.preorder.notexist");
        }
        //非该用户订单
        if(!Objects.equals(uid,preorderDTO.getAccountId())){
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.preorder.notexist");
        }
        return preorderDTO;
    }

    @Transactional
    @Override
    public ResultJson thirdpay(Long uid, String payType, String preorderNo) {

        PreorderDTO preorderDTO = getUserPreorder(uid, preorderNo);

        //查询确定是否有已支付订单
        ChargeRecordDTO chargeRecord=chargeService.get(uid,preorderNo);
        if(chargeRecord==null) {
            ChargeParams params = new ChargeParams();
            params.setPreorderNo(preorderNo);
            params.setMoney(preorderDTO.getTotalFee());
            params.setAccountId(preorderDTO.getAccountId());
            params.setMchId(MerchantId.LittelG.getValue());
            chargeService.create(params);
        }else {
            if(!Objects.equals(chargeRecord.getStatus(), StatusEnum.INIT)){
                ResultJson r=new ResultJson();
                r.setC(PayErrorCodes.PAY_ERROR);
                r.setM("msg.pay.status.error");
                return r;
            }
        }
        return prePay(uid,payType,preorderNo);
    }

    @Transactional
    @Override
    public ResultJson pay(@NotBlank Long uid, @NotEmpty String preorderNo) {

        ResultJson r=new ResultJson();

        PreorderDTO preorderDTO=getUserPreorder(uid,preorderNo);
        if(Objects.equals(preorderDTO.getStatus(),StatusEnum.SUCCESS.getValue())){
            return r;
        }
        if(!Objects.equals(preorderDTO.getStatus(),StatusEnum.INIT.getValue())){
            r.setC(PayErrorCodes.PAY_ERROR);
            r.setM("msg.pay.status.error");
            return r;
        }

        if(!Objects.equals(TradeType.TRANSFER.getValue(),preorderDTO.getTradeType())){
            r.setC(PayErrorCodes.PAY_ERROR);
            r.setM("msg.pay.status.error");
            return r;
        }

        Account from=FixAccount.isFixAccount(preorderDTO.getAccountId())? new com.little.g.pay.dto.FixAccount(preorderDTO.getAccountId(),false):new NormalUserAccount(preorderDTO.getAccountId());
        Account to=FixAccount.isFixAccount(preorderDTO.getOppositAccount())? new com.little.g.pay.dto.FixAccount(preorderDTO.getOppositAccount(),false):new NormalUserAccount(preorderDTO.getOppositAccount());
        BusinessType btype=BusinessType.valueOf(preorderDTO.getBtype());
        transactionService.transfer(from,to,preorderDTO.getTotalFee(),preorderDTO.getOutTradeNo(),btype,preorderDTO.getSubject());
        //支付成功更新预支付订单支付状态
        preOrderService.updateStatus(uid,preorderNo,StatusEnum.SUCCESS.getValue(), PayType.BALANCE,null);

        return r;
    }

    @Override
    public void thirdpayCallback(String payType, @NotNull PayCallbackInfo callbackInfo) {
        log.debug("receive paytype:{} callbackInfo:{}",payType,callbackInfo);
        if(!Objects.equals(ThirdPayStatus.SUCCESS,callbackInfo.getThirdPayStatus())){
            //支付成功
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.thirdpay.failed");
        }

        PreorderDTO preorderDTO= preOrderService.get(MerchantId.LittelG.getValue(),callbackInfo.getOutPayOrderId());
        if(preorderDTO == null){
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.preorder.notexist");
        }
        if(Objects.equals(preorderDTO.getStatus(),StatusEnum.SUCCESS.getValue())){
            return;
        }
        if(!Objects.equals(preorderDTO.getStatus(),StatusEnum.INIT.getValue())){
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.status.error");
        }
        if(!Objects.equals(preorderDTO.getTotalFee(),callbackInfo.getRealFee())){
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.thirdpay.money.mismatch");
        }
        preOrderService.updateStatus(preorderDTO.getAccountId(),preorderDTO.getPreOrderNo(),StatusEnum.SUCCESS.getValue(),payType,callbackInfo.getOutPayOrderId());


    }
}
