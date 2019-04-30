package com.little.g.pay.params;

import com.little.g.pay.enums.MerchantId;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

public class ChargeParams implements Serializable {
    @NotEmpty
    private String mchId = MerchantId.LittelG.getValue();

    @NotEmpty
    private String preorderNo;

    @NotEmpty
    private Long accountId;
    @Min(1)
    private Long money;

    private String tranNum;

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getPreorderNo() {
        return preorderNo;
    }

    public void setPreorderNo(String preorderNo) {
        this.preorderNo = preorderNo;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getMoney() {
        return money;
    }

    public void setMoney(Long money) {
        this.money = money;
    }

    public String getTranNum() {
        return tranNum;
    }

    public void setTranNum(String tranNum) {
        this.tranNum = tranNum;
    }
}
