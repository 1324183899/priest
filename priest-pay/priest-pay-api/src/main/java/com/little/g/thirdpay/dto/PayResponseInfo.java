//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.little.g.thirdpay.dto;

import java.io.Serializable;
import java.util.Map;

public class PayResponseInfo implements Serializable {
    private String response;
    private int statusCode = 200;
    private Map<String, String> headers;

    public PayResponseInfo(String response) {
        this.response = response;
    }

    public PayResponseInfo(String response, int statusCode) {
        this.response = response;
        this.statusCode = statusCode;
    }

    public PayResponseInfo(String response, int statusCode, Map<String, String> headers) {
        this.response = response;
        this.statusCode = statusCode;
        this.headers = headers;
    }

    public String getResponse() {
        return this.response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String toString() {
        return "PayResponseInfo{response='" + this.response + '\'' + ", statusCode=" + this.statusCode + ", headers=" + this.headers + '}';
    }
}
