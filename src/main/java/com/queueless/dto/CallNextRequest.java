package com.queueless.dto;

public class CallNextRequest {

    private String counterName;

    public CallNextRequest() {
    }

    public CallNextRequest(String counterName) {
        this.counterName = counterName;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }
}
