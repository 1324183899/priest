package com.little.g.pay.service.impl;

import com.little.g.common.ResultJson;
import com.little.g.common.enums.StatusEnum;
import com.little.g.common.exception.ServiceDataException;
import com.little.g.pay.PayErrorCodes;
import com.little.g.pay.api.ChargeService;
import com.little.g.pay.api.LittlePayService;
import com.little.g.pay.api.PreOrderService;
import com.little.g.pay.dto.ChargeRecordDTO;
import com.little.g.pay.dto.PayTypeDTO;
import com.little.g.pay.dto.PreorderDTO;
import com.little.g.pay.enums.MerchantId;
import com.little.g.pay.params.ChargeParams;
import com.little.g.thirdpay.api.ThirdpayApi;
import com.little.g.thirdpay.dto.PrePayResult;
import com.little.g.thirdpay.model.PayChannel;
import com.little.g.thirdpay.params.PrepayParams;
import com.little.g.thirdpay.service.impl.ThirdPayFactory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service("littlePayService")
public class LittlePayServiceImpl implements LittlePayService {

    static List<PayTypeDTO> payTypeList = new ArrayList<>();
    @Resource
    private ThirdPayFactory thirdPayFactory;
    @Resource
    private PreOrderService preOrderService;
    @Resource
    private ThirdpayApi thirdpayApi;
    @Resource
    private ChargeService chargeService;

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

    @Override
    public ResultJson pay(@NotBlank Long uid, @NotEmpty String preorderNo) {

        /**
         * TODO: 余额支付，需采用回调
         */


        return null;
    }
}
