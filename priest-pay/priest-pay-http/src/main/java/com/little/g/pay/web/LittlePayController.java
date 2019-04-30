package com.little.g.pay.web;

import com.little.g.common.ResultJson;
import com.little.g.common.web.interceptor.HeaderParamsHolder;
import com.little.g.pay.api.ChargeService;
import com.little.g.pay.api.LittlePayService;
import com.little.g.pay.dto.OrderResult;
import com.little.g.pay.dto.PayTypeDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/pay")
@RestController
public class LittlePayController {
    @Resource
    private LittlePayService littlePayService;
    @Resource
    private ChargeService chargeService;


    @RequestMapping("/list")
    public ResultJson list(){
        List<PayTypeDTO> typeList=littlePayService.typeList();
        ResultJson r=new ResultJson();
        r.setData(typeList);
        return r;
    }

    @RequestMapping
    public ResultJson pay(@RequestParam String preorderNo){
        Long uid=HeaderParamsHolder.getHeader().getUid();
        return littlePayService.pay(uid,preorderNo);
    }

    @RequestMapping("/charge")
    public ResultJson charge(@RequestParam Long money){
        Long uid=HeaderParamsHolder.getHeader().getUid();
        OrderResult r=chargeService.createChargeOrder(uid,money);

        ResultJson result=new ResultJson();
        result.setData(r);
        return result;

    }
    @RequestMapping("/{payType}/thirdpay")
    public ResultJson thirdpay(@PathVariable("payType") String payType,@RequestParam String preorderNo){
        Long uid=HeaderParamsHolder.getHeader().getUid();
        return littlePayService.thirdpay(uid,payType,preorderNo);
    }

    /**
     * 获取支付参数
     * @param payType
     * @param preorderNo
     * @return
     */
    @RequestMapping("/{payType}/params")
    public ResultJson params(@PathVariable("payType") String payType,@RequestParam String preorderNo){
        Long uid=HeaderParamsHolder.getHeader().getUid();
        return littlePayService.prePay(uid,payType,preorderNo);
    }
}
