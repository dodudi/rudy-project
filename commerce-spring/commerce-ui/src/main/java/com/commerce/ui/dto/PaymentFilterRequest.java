package com.commerce.ui.dto;

public class PaymentFilterRequest {

    private Long memberId;
    private Long orderId;
    private String status;
    private String startDate;
    private String endDate;
    private Integer minAmount;
    private Integer maxAmount;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Integer getMinAmount() { return minAmount; }
    public void setMinAmount(Integer minAmount) { this.minAmount = minAmount; }

    public Integer getMaxAmount() { return maxAmount; }
    public void setMaxAmount(Integer maxAmount) { this.maxAmount = maxAmount; }
}
