package com.queueless.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JoinQueueRequest {

    @NotBlank(message = "Customer name must not be blank")
    @Size(min = 2, max = 50, message = "Customer name must be between 2 and 50 characters")
    private String customerName;

    private String serviceType;

    public JoinQueueRequest() {
    }

    public JoinQueueRequest(String customerName, String serviceType) {
        this.customerName = customerName;
        this.serviceType = serviceType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
