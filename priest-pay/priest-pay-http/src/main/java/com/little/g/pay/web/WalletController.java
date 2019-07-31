package com.little.g.pay.web;

import com.little.g.common.ResultJson;
import com.little.g.common.dto.ListResultDTO;
import com.little.g.common.params.UidTimeQueryParam;
import com.little.g.common.utils.MoneyUtil;
import com.little.g.common.web.interceptor.HeaderParamsHolder;
import com.little.g.pay.api.TransactionService;
import com.little.g.pay.api.UserAccountService;
import com.little.g.pay.dto.TransactionRecordDTO;
import com.little.g.pay.dto.UserAccountDTO;
import com.little.g.pay.web.vo.TransactionRecordVO;
import com.little.g.pay.web.vo.UserAccountVO;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.stream.Collectors;

@RequestMapping("/wallet")
@RestController
public class WalletController {

    @Resource
    private UserAccountService userAccountService;
    @Resource
    private TransactionService transactionService;


    @RequestMapping
    public ResultJson wallet(){
        Long uid= HeaderParamsHolder.getHeader().getUid();
        ResultJson r =new ResultJson();
        UserAccountDTO userAccountDTO = userAccountService.queryUserAccount(uid);
        r.setData(convert2VO(userAccountDTO));
        return r;
    }

    @RequestMapping("/transactions")
    public ResultJson transactions(@Valid UidTimeQueryParam param){
        Long uid= HeaderParamsHolder.getHeader().getUid();
        ResultJson r =new ResultJson();
        param.setUid(uid);
        ListResultDTO<TransactionRecordDTO> result =  transactionService.list(param);
        r.setData(convert2Result(result));
        return r;
    }


    ListResultDTO<TransactionRecordVO> convert2Result(ListResultDTO<TransactionRecordDTO> result){
        ListResultDTO<TransactionRecordVO> voResult=new ListResultDTO<>();
        BeanUtils.copyProperties(result,voResult,"list");
        if(!CollectionUtils.isEmpty(result.getList())){
            voResult.setList(result.getList().stream().map(record->{
                TransactionRecordVO vo=new TransactionRecordVO();
                BeanUtils.copyProperties(record,vo);
                vo.setMoney(MoneyUtil.long2Double(record.getMoney()));
                vo.setCreateTime(DateFormatUtils.format(record.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
                return vo;
            }).collect(Collectors.toList()));
        }
        return voResult;
    }
    UserAccountVO convert2VO(UserAccountDTO dto){
        if(dto == null){
            return null;
        }
        UserAccountVO vo =new UserAccountVO();
        BeanUtils.copyProperties(dto,vo);
        vo.setFrozonMoney(MoneyUtil.long2Double(dto.getFrozonMoney()));
        vo.setMoney(MoneyUtil.long2Double(dto.getMoney()));
        vo.setCreateTime(DateFormatUtils.format(dto.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
        vo.setUpdateTime(DateFormatUtils.format(dto.getUpdateTime(),"yyyy-MM-dd HH:mm:ss"));
        return vo;
    }
}
