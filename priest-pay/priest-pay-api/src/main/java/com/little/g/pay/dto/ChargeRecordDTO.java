package com.little.g.pay.dto;

import java.io.Serializable;

public class ChargeRecordDTO implements Serializable {

    private String tranNum;

    private Long uid;

    private Long money;

    private Byte status;

    private String description;

    private Byte payType;

    private String preorderNo;

    private String mchId;

    private String outTranNum;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;

    public String getTranNum() {
        return tranNum;
    }

    public void setTranNum(String tranNum) {
        this.tranNum = tranNum == null ? null : tranNum.trim();
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getMoney() {
        return money;
    }

    public void setMoney(Long money) {
        this.money = money;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Byte getPayType() {
        return payType;
    }

    public void setPayType(Byte payType) {
        this.payType = payType;
    }

    public String getPreorderNo() {
        return preorderNo;
    }

    public void setPreorderNo(String preorderNo) {
        this.preorderNo = preorderNo == null ? null : preorderNo.trim();
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId == null ? null : mchId.trim();
    }

    public String getOutTranNum() {
        return outTranNum;
    }

    public void setOutTranNum(String outTranNum) {
        this.outTranNum = outTranNum == null ? null : outTranNum.trim();
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "ChargeRecordDTO{" +
                "tranNum='" + tranNum + '\'' +
                ", uid=" + uid +
                ", money=" + money +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", payType=" + payType +
                ", preorderNo='" + preorderNo + '\'' +
                ", mchId='" + mchId + '\'' +
                ", outTranNum='" + outTranNum + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}