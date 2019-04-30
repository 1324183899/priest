package com.little.g.pay.enums;

import java.util.Objects;

/**
 * Created by lengligang on 16/7/13.
 */
public enum MerchantId {
    LittelG("10000", "小G科技");

    MerchantId(String value, String desc) {
        this.value = value;
        this.desc=desc;
    }

    public String value;
    public String desc;


    public String getValue() {
        return value;
    }
    public String getDesc(){return desc;}

    public static MerchantId getEnum(String value) {    //    手写的从int到enum的转换函数
        for (MerchantId type : values()) {
            if (Objects.equals(type.getValue(), value)) {
                return type;
            }
        }
        return null;
    }
}