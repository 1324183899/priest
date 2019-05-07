package com.little.g.pay.enums;

import com.little.g.common.exception.ServiceDataException;
import com.little.g.pay.PayErrorCodes;

import java.util.Objects;

/**
 * Created by lengligang on 16/1/18.
 */
public enum BusinessType {
    RECHARGE((byte)1,"充值"), //充值
    STRIKE_BALANCE((byte)16, "冲帐"),
    ;

    BusinessType(Byte value, String desc){
        this.value=value;
        this.desc=desc;
    }
    public Byte value;
    public String desc;

    public Byte getValue() {
        return value;
    }
    public String getDesc(){return desc;}


    public static BusinessType valueOf(Byte value) {    //    手写的从int到enum的转换函数
        for (BusinessType type : values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        throw new ServiceDataException(PayErrorCodes.PAY_ERROR,"msg.pay.unknow.btype");
    }
}
