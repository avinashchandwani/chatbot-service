package com.iiit.chatbot.service.core;

import java.util.Date;

public class ChatBotAction {

	private String orderNumber;
	private String action;
	private String itemName;
	private int userId;
	private int qty;
	private Date expectedDeliveryDate;
	private String status;
	private Date orderPlacementDate;

	public ChatBotAction(){
		super();
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public Date getExpectedDeliveryDate() {
		return expectedDeliveryDate;
	}

	public void setExpectedDeliveryDate(Date expectedDeliveryDate) {
		this.expectedDeliveryDate = expectedDeliveryDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Date getOrderPlacementDate() {
		return orderPlacementDate;
	}

	public void setOrderPlacementDate(Date orderPlacementDate) {
		this.orderPlacementDate = orderPlacementDate;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

}
