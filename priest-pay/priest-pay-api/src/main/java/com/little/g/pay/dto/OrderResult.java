package com.little.g.pay.dto;

import java.io.Serializable;

public class OrderResult implements Serializable {
    /**
     * 预支付订单号
     */
    private String preorderNo;
    /**
     * 交易流水
     */
    private String tranNo;

    public String getPreorderNo() {
        return preorderNo;
    }

    public void setPreorderNo(String preorderNo) {
        this.preorderNo = preorderNo;
    }

    public String getTranNo() {
        return tranNo;
    }

    public void setTranNo(String tranNo) {
        this.tranNo = tranNo;
    }
}
