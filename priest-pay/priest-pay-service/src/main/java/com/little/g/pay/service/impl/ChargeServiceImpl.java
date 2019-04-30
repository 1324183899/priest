package com.little.g.pay.service.impl;

import com.little.g.common.enums.StatusEnum;
import com.little.g.pay.api.ChargeService;
import com.little.g.pay.api.PreOrderService;
import com.little.g.pay.dto.ChargeRecordDTO;
import com.little.g.pay.dto.OrderResult;
import com.little.g.pay.dto.PreorderDTO;
import com.little.g.pay.enums.TradeType;
import com.little.g.pay.mapper.ChargeRecordMapper;
import com.little.g.pay.model.ChargeRecord;
import com.little.g.pay.model.ChargeRecordExample;
import com.little.g.pay.params.ChargeParams;
import com.little.g.pay.params.PreOrderParams;
import com.little.g.pay.utils.TransactionNumUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

@Service("chargeService")
public class ChargeServiceImpl implements ChargeService {

    @Resource
    private ChargeRecordMapper chargeRecordMapper;
    @Resource
    private PreOrderService preOrderService;


    public ChargeRecordDTO get(Long uid,String preorderNo){
        ChargeRecordExample example = new ChargeRecordExample();
        example.or().andUidEqualTo(uid)
                    .andPreorderNoEqualTo(preorderNo);
        List<ChargeRecord> list = chargeRecordMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        ChargeRecordDTO record=new ChargeRecordDTO();
        BeanUtils.copyProperties(list.get(0),record);

        return record;
    }

    @Override
    public ChargeRecordDTO create(@Null @Valid ChargeParams params) {

        ChargeRecord charge = new ChargeRecord();
        charge.setCreateTime(System.currentTimeMillis());
        charge.setMoney(params.getMoney());
        charge.setStatus(StatusEnum.INIT.getValue());
        if(StringUtils.isNotEmpty(params.getTranNum())){
            charge.setTranNum(params.getTranNum());
        }else {
            charge.setTranNum(TransactionNumUtil.generateChageNum());
        }

        charge.setUid(params.getAccountId());
        charge.setUpdateTime(System.currentTimeMillis());
        charge.setPreorderNo(params.getPreorderNo());
        charge.setMchId(params.getMchId());

        if (chargeRecordMapper.insert(charge) <= 0) return null;
        ChargeRecordDTO d=new ChargeRecordDTO();
        BeanUtils.copyProperties(charge,d);

        return d;

    }


    public OrderResult createChargeOrder(@NotNull Long uid,@NotNull @Min(1)Long money){

        String tranNum = TransactionNumUtil.generateChageNum();

        PreOrderParams params=new PreOrderParams();
        params.setAccountId(uid);
        params.setComment("用户充值");
        params.setOppositAccount(uid);
        params.setOutTradeNo(tranNum);
        params.setTotalFee(money);
        params.setTradeType(TradeType.CHARGE);

        PreorderDTO preorder=preOrderService.create(params);
        ChargeParams chargeParams=new ChargeParams();
        chargeParams.setAccountId(uid);
        chargeParams.setMoney(money);
        chargeParams.setPreorderNo(preorder.getPreOrderNo());
        chargeParams.setTranNum(tranNum);
        create(chargeParams);

        OrderResult result=new OrderResult();
        result.setPreorderNo(preorder.getPreOrderNo());
        result.setTranNo(tranNum);
        return result;

    }
}
