package com.little.g.pay.web;

import com.little.g.common.ResultJson;
import com.little.g.common.enums.PayType;
import com.little.g.common.web.interceptor.HeaderParamsHolder;
import com.little.g.pay.api.ChargeService;
import com.little.g.pay.api.LittlePayService;
import com.little.g.pay.dto.OrderResult;
import com.little.g.pay.dto.PayTypeDTO;
import com.little.g.thirdpay.api.ThirdpayApi;
import com.little.g.thirdpay.dto.PayCallbackInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class LittlePayController {
    @Resource
    private LittlePayService littlePayService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private ThirdpayApi thirdpayApi;


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

    @RequestMapping(value = "/{payType}/callback")
    public ResultJson callback(@PathVariable("payType") String payType, @RequestBody String body, HttpServletRequest request){
        PayCallbackInfo callbackInfo;
        if(PayType.WEXINPAY.equals(payType)){
            callbackInfo=thirdpayApi.verifyBodyResponse(payType,body);
        }else {
            Map<String, String> params = new HashMap<String, String>();
            Map requestParams = request.getParameterMap();
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
                //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
                params.put(name, valueStr);
            }
            callbackInfo=thirdpayApi.verifyResponse(payType,params);
        }


        return null;
    }

}
