package com.example.demo.dto;

//為了接收前端的 JSON 請求，您可以定義一個 DTO
//如果這個檔案不存在，請在相同套件下創建 PaymentProcessRequest.java
public class PaymentProcessRequest {
// private String userId;
	private Long orderId;
	private String paymentMethod; // 前端傳過來的 "ECPAY"

	// Getter and Setter
// public String getUserId() {
//     return userId;
// }
//
// public void setUserId(String userId) {
//     this.userId = userId;
// }

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
}
