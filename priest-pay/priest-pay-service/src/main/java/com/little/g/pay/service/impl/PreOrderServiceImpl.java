package com.little.g.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.little.g.common.ResultJson;
import com.little.g.common.enums.PayType;
import com.little.g.common.enums.StatusEnum;
import com.little.g.common.exception.ServiceDataException;
import com.little.g.common.utils.HttpUtils;
import com.little.g.pay.PayErrorCodes;
import com.little.g.pay.api.ChargeService;
import com.little.g.pay.api.PreOrderService;
import com.little.g.pay.dto.PreorderDTO;
import com.little.g.pay.enums.MerchantId;
import com.little.g.pay.enums.TradeType;
import com.little.g.pay.mapper.PreorderMapper;
import com.little.g.pay.model.Preorder;
import com.little.g.pay.model.PreorderExample;
import com.little.g.pay.model.PreorderKey;
import com.little.g.pay.params.PreOrderParams;
import com.little.g.pay.utils.TransactionNumUtil;
import com.little.g.user.api.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Service("preOrderService")
public class PreOrderServiceImpl implements PreOrderService {
    @Resource
    private  PreorderMapper preorderMapper;

    @Resource
    private UserService userService;
    @Resource
    private ChargeService chargeService;


    @Transactional
    @Override
    public PreorderDTO create(@Valid PreOrderParams preOrderParams) {
        if(MerchantId.getEnum(preOrderParams.getMchId()) == null){
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.unknow.merchant");
        }

        if(userService.getUserById(preOrderParams.getAccountId()) == null || userService.getUserById(preOrderParams.getOppositAccount()) == null){
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.account.notexist");
        }

        Preorder preorder=new Preorder();
        BeanUtils.copyProperties(preOrderParams,preorder);
        preorder.setCreateTime(System.currentTimeMillis());
        preorder.setStatus(StatusEnum.INIT.getValue());
        preorder.setUpdateTime(System.currentTimeMillis());
        preorder.setPreOrderNo(TransactionNumUtil.generatePreorderNum());

        PreorderExample e=new PreorderExample();
        PreorderExample.Criteria c = e.or();
        c.andMchIdEqualTo(preOrderParams.getMchId());
        c.andOutTradeNoEqualTo(preOrderParams.getOutTradeNo());

        List<Preorder> preorderList=preorderMapper.selectByExample(e);

        if(CollectionUtils.isNotEmpty(preorderList)){
            //更新
            c.andStatusEqualTo(StatusEnum.INIT.getValue());
           if(preorderMapper.updateByExampleSelective(preorder,e)<=0){
               throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.unknow.exception");
           }

            PreorderDTO dto =new PreorderDTO();
            BeanUtils.copyProperties(preorderList.get(0),dto);
            return dto;
        }

        if(preorderMapper.insertSelective(preorder)<=0){
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.unknow.exception");
        }

        PreorderDTO dto =new PreorderDTO();

        BeanUtils.copyProperties(preorder,dto);

        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public PreorderDTO get(@NotEmpty String mchId, @NotEmpty String preorderNo) {
        if(MerchantId.getEnum(mchId) == null){
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.unknow.merchant");
        }

        PreorderKey key =new PreorderKey();
        key.setMchId(mchId);
        key.setPreOrderNo(preorderNo);

        Preorder preorder=preorderMapper.selectByPrimaryKey(key);
        if(preorder == null) return null;

        PreorderDTO dto = new PreorderDTO();
        BeanUtils.copyProperties(preorder,dto);

        return dto;
    }

    @Transactional
    @Override
    public boolean updateStatus(@NotNull Long uid, @NotEmpty String preorderNo, Byte status, @NotEmpty String payType,String thirdyPayNo) {

        PreorderExample example=new PreorderExample();
        example.or().andAccountIdEqualTo(uid)
                    .andPreOrderNoEqualTo(preorderNo)
                    .andStatusEqualTo(StatusEnum.INIT.getValue());
        List<Preorder> list = preorderMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(list)){
            throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.preorder.notexist");
        }
        Preorder preorder=list.get(0);

        preorder.setPayType(payType);
        preorder.setStatus(status);
        preorder.setUpdateTime(System.currentTimeMillis());

        boolean r=preorderMapper.updateByPrimaryKeySelective(preorder)>0;


        if(!Objects.equals(PayType.BALANCE,payType) || Objects.equals(TradeType.CHARGE.getValue(),preorder.getTradeType())){
            //先充值，后扣款
            chargeService.chargeSuccess(preorderNo,payType,thirdyPayNo);

        }

        //成功并且非充值支付
        if(Objects.equals(StatusEnum.SUCCESS,status) && !Objects.equals(TradeType.CHARGE.getValue(),preorder.getTradeType())){
            //发送通知
            if(StringUtils.isNotEmpty(preorder.getNotifyUrl())){
                //发送通知
                ResultJson result=HttpUtils.post(preorder.getNotifyUrl(),null,JSONObject.toJSONString(preorder),ResultJson.class);
                if(result != null && result.getC()==ResultJson.SUCCESSFUL){
                    return true;
                }
            }
        }
        return r;
    }
}
