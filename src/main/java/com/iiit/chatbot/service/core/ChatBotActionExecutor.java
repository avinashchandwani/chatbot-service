package com.iiit.chatbot.service.core;

import java.util.List;

import com.chatbot.dao.ChatBotDAO;
import com.chatbot.entity.Order;
import com.chatbot.entity.Pair;
import com.chatbot.util.ActionType;


public class ChatBotActionExecutor {

	private ChatBotAction chatBotAction;
	private ChatBotDAO chatBotDao;

	public ChatBotActionExecutor(){
		super();
		this.chatBotAction = new ChatBotAction();
		chatBotAction.setUserId(1);
	}

	public void setEntity(String key, String value){

	}

	public boolean logChatWithUser(int userId, String userMessage, String chatBotResponse, byte helpful){
		return chatBotDao.logChatWithUser(userId, userMessage, chatBotResponse, helpful);
	}

	/**
	 * 1. Action is status, query from dB and frame the statement
	 * 2. Action is cancel, perform DB update and return the success statement to the user
	 * 3. Action is Buy/Purchase, check availability if available, perform update and return to the user
	 */
	public Pair<String,Boolean> executeAction(){
		Order order = null;
		if(chatBotAction.getAction().equals(ActionType.BUY.toString())){
			order = chatBotDao.placeOrder(chatBotAction.getItemName(), chatBotAction.getQty(), chatBotAction.getUserId());
			return responseBuy(order);
		}else if(chatBotAction.getAction().equals(ActionType.STATUS.toString())){
			List<Order> orders = chatBotDao.getAllOrderStatus(chatBotAction.getUserId());
			return responseAllStatus(orders);
		}else if(chatBotAction.getAction().equals(ActionType.CANCEL.toString())){
			order = chatBotDao.cancelOrder(chatBotAction.getOrderNumber());
			return responseCancel(order);
		}else{
			return null;// Exception case
		}
	}

	private Pair<String, Boolean> responseBuy(Order order){
		String responseString = null;
		if(order == null)
			return new Pair<String, Boolean>("Item doesnt exist in our system", false);
		if(order.isSuccess()){
			responseString = "Order Placed Successfully!\n Order Number : "
					+order.getOrderNumber()+", order status is :"+ order.getStatus() + ", and it will be delivered on " + order.getOrderDate().toString();
			return new Pair<String, Boolean>(responseString, true);
		}
		return new Pair<String, Boolean>("Some Problem occured in placing an order, please try after sometime", false);
	}

	private Pair<String,Boolean> responseCancel(Order order){
		boolean status = false;
		String responseString = null;
		if(order == null || !order.isSuccess()){
			responseString = "Order doesn't exist, please check the order number and provide us again";
			status = false;
		}
		if(order.isSuccess()){
			responseString = "Order cancellation done\n Your money will be refunded in 5 working days";
			status = true;
		}
		return new Pair<String, Boolean>(responseString, status);
	}

	public ChatBotAction getChatBotAction() {
		return chatBotAction;
	}

	private Pair<String, Boolean> responseAllStatus(List<Order> orders){
		StringBuilder response = new StringBuilder();
		boolean isSuccess = true;
		if(orders == null || orders.isEmpty()){
			return new Pair<String, Boolean>("No Active Orders exist for you currently", false);
		}
		for(Order order: orders){
			String responseString = "Order Number : "+order.getOrderNumber()+", order status is :"+ order.getStatus() + ", and it will be delivered on " + order.getOrderDate().toString();
			response.append(responseString).append("\n");
			isSuccess = isSuccess & order.isSuccess();
		}
		return new Pair<String, Boolean>(response.toString(), isSuccess);
	}

	public ChatBotDAO getChatBotDao() {
		return chatBotDao;
	}

	public void setChatBotDao(ChatBotDAO chatBotDao) {
		this.chatBotDao = chatBotDao;
	}
}


