package com.iiit.chatbot.service.entity;

import java.util.HashSet;
import java.util.Set;

public class StatementInfo {

	private String statementType;
	private String statementSubtype;

	private Set<String> knownActionWords;
	private String knownItem;
	private String orderNumber;

	public StatementInfo(){
		super();
		setStatementType(null);
		setStatementSubtype(null);
		setKnownActionWords(new HashSet<String>());
	}

	public String getStatementType() {
		return statementType;
	}

	public void setStatementType(String statementType) {
		this.statementType = statementType;
	}

	public Set<String> getKnownActionWords() {
		return knownActionWords;
	}

	public void setKnownActionWords(Set<String> knownWords) {
		this.knownActionWords = knownWords;
	}

	public boolean knownActionWordsFound(){
		return knownActionWords.size()!=0;
	}

	public String getKnownItem() {
		return knownItem;
	}

	public void setKnownItem(String knownItem) {
		this.knownItem = knownItem;
	}
	public String getStatementSubtype() {
		return statementSubtype;
	}

	public void setStatementSubtype(String statementSubtype) {
		this.statementSubtype = statementSubtype;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
}
