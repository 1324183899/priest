package com.little.g.pay.enums;

import java.util.Objects;

/**
 * Created by lengligang on 16/1/18.
 */
public enum TradeType {

    CHARGE("CHARGE","充值"),
    TRANSFER("TRANSFER","转账"),
    FROZE("FROZE","冻结");

    TradeType(String value, String desc){
        this.value=value;this.desc=desc;
    }


    public String value;
    public String desc;

    public String getDesc() {
        return desc;
    }

    public String getValue() {
        return value;
    }

    public static TradeType getEnum(String value) {    //    手写的从int到enum的转换函数
        for (TradeType type : values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        return null;
    }
}
