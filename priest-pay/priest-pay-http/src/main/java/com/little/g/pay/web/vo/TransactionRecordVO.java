package com.little.g.pay.web.vo;

import java.io.Serializable;

public class TransactionRecordVO implements Serializable {

    private String tranNum;

    private String accountId;

    private String opposite;

    private String tradeNum;

    private Double money;

    private Byte type;

    private Byte btype;

    private String comment;

    private String createTime;

    public String getTranNum() {
        return tranNum;
    }

    public void setTranNum(String tranNum) {
        this.tranNum = tranNum;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getOpposite() {
        return opposite;
    }

    public void setOpposite(String opposite) {
        this.opposite = opposite;
    }

    public String getTradeNum() {
        return tradeNum;
    }

    public void setTradeNum(String tradeNum) {
        this.tradeNum = tradeNum;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Byte getBtype() {
        return btype;
    }

    public void setBtype(Byte btype) {
        this.btype = btype;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
