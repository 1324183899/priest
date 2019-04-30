package com.little.g.pay.service.impl;

import com.little.g.common.enums.StatusEnum;
import com.little.g.common.exception.ServiceDataException;
import com.little.g.pay.PayErrorCodes;
import com.little.g.pay.api.PreOrderService;
import com.little.g.pay.dto.PreorderDTO;
import com.little.g.pay.enums.MerchantId;
import com.little.g.pay.mapper.PreorderMapper;
import com.little.g.pay.model.Preorder;
import com.little.g.pay.model.PreorderExample;
import com.little.g.pay.model.PreorderKey;
import com.little.g.pay.params.PreOrderParams;
import com.little.g.pay.utils.TransactionNumUtil;
import com.little.g.user.api.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Service("preOrderService")
public class PreOrderServiceImpl implements PreOrderService {
    @Resource
    private  PreorderMapper preorderMapper;

    @Resource
    private UserService userService;


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



}
