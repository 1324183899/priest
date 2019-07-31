package com.little.g.pay.web.vo;

public class UserAccountVO {

    private Long uid;

    private Double money;

    private Double frozonMoney;

    private Byte status;

    private String updateTime;

    private String createTime;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public Double getFrozonMoney() {
        return frozonMoney;
    }

    public void setFrozonMoney(Double frozonMoney) {
        this.frozonMoney = frozonMoney;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
