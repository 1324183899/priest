package com.little.g.pay.model;

import java.io.Serializable;

public class PreorderKey implements Serializable {
    private String mchId;

    private String preOrderNo;

    private static final long serialVersionUID = 1L;

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId == null ? null : mchId.trim();
    }

    public String getPreOrderNo() {
        return preOrderNo;
    }

    public void setPreOrderNo(String preOrderNo) {
        this.preOrderNo = preOrderNo == null ? null : preOrderNo.trim();
    }
}