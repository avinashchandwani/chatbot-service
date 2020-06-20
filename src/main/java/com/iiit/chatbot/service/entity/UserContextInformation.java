package com.iiit.chatbot.service.entity;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="UserContextInformation")
public class UserContextInformation implements Serializable {

	/**
	 *
	 */
	/**
	 *
	 */
	private static final long serialVersionUID = 3164313986571176372L;
	private String previousContext = null;
	private boolean isClear = false;
	private int userId;
	private String message = null;

	public UserContextInformation(){
		super();
		//userId = 5;
		/*message = "How may i help you";
		previousContext = null;*/
	}

	public UserContextInformation(int userId){
		super();
		this.userId = userId;
		message = "Hi there, How may i help you?";
	}

	public void clearUserContext(){
		isClear = true;
	}

	@XmlElement(name="previouscontext")
	public String getPreviousContext() {
		return previousContext;
	}

	public void setPreviousContext(String previousContext) {
		this.previousContext = previousContext;
	}

	@XmlElement(name="isclear")
	public boolean isClear() {
		return isClear;
	}

	public void setClear(boolean isClear) {
		this.isClear = isClear;
	}

	@XmlElement(name="userid")
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@XmlElement(name="message")
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String toString(){
		return getUserId()+"," + getMessage() + "," + getPreviousContext();
	}
}

