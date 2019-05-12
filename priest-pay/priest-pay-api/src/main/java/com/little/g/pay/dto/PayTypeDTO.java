package com.little.g.pay.dto;

import java.io.Serializable;

public class PayTypeDTO implements Serializable {

    public PayTypeDTO(String typeName, String comment, String thumbnail) {
        this.typeName = typeName;
        this.comment = comment;
        this.thumbnail = thumbnail;
    }

    public PayTypeDTO() {

    }

    /**
     * 支付名称
     */
    private String typeName;
    /**
     * 支付描述
     */
    private String comment;
    /**
     * 缩略图
     */
    private String thumbnail;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        return "PayTypeDTO{" +
                "typeName='" + typeName + '\'' +
                ", comment='" + comment + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                '}';
    }
}
