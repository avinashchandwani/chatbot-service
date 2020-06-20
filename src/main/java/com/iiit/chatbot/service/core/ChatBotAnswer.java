package com.iiit.chatbot.service.core;


import com.chatbot.dao.ChatBotDAO;
import com.chatbot.entity.Pair;
import com.chatbot.entity.StatementInfo;
import com.chatbot.entity.UserContextInformation;
import com.chatbot.util.ActionType;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;

public class ChatBotAnswer {


	private UserContextInformation userContextInfo = null;
	private OpenNLPModelFactory factory = null;
	private String userQuery = null;
	private String generatedResponse = null;
	private ChatBotActionExecutor executor = null;
	private byte helpful = 0;

	public ChatBotAnswer(){
		super();
	}

	public ChatBotAnswer(UserContextInformation info, OpenNLPModelFactory factory, ChatBotDAO chatBotDAO){
		this.userContextInfo = info;
		this.factory = factory;
		this.userQuery = info.getMessage();
		executor = new ChatBotActionExecutor();
		executor.setChatBotDao(chatBotDAO);
	}

	public UserContextInformation respondUser(){
		StatementInfo statementInfo = parseStatement();
		processStatement(statementInfo);
		generatedResponse = generateResponse();
		userContextInfo.setMessage(generatedResponse);
		return userContextInfo;
	}


	private StatementInfo parseStatement(){
		StatementInfo info = new StatementInfo();
    	info.setStatementType(getStatementType(userQuery));
    	info.setStatementSubtype(getQuestionType(userQuery));
    	findActionsInStatement(userQuery, info);
    	findActionsInStatement(userQuery, info);
		findItemInStatement(userQuery, info);
		findItemInStatement(userQuery, info);
    	return info;
	}

	private void processStatement(StatementInfo statementInfo){
		String type = statementInfo.getStatementSubtype();
		if(type.equals("WHNP")){
			// What Question is there, and need to filter further
			if(statementInfo.getKnownActionWords().contains("status") && statementInfo.getKnownActionWords().contains("order")){
				userContextInfo.setPreviousContext(ActionType.STATUS.toString());
				System.out.println("User asking for status of his order");
				executor.getChatBotAction().setAction(userContextInfo.getPreviousContext());
				executor.getChatBotAction().setUserId(userContextInfo.getUserId());
				Pair<String, Boolean> response =  executor.executeAction();
				generatedResponse = response.getFirstValue();
				if(response.getSecondValue()){
					clearContexts();
				}
			}
			// else cases for cancel an order and place an order for the item
		}else if(type.equals("S") || type.equals("INTJ") || type.equals("NP")){
			if(statementInfo.getKnownActionWords().contains("buy")){
				userContextInfo.setPreviousContext(ActionType.BUY.toString());
				if(statementInfo.getKnownItem() == null){
					generatedResponse = "Please provide item name";
				}else{
					executor.getChatBotAction().setItemName(statementInfo.getKnownItem());
					executor.getChatBotAction().setAction(userContextInfo.getPreviousContext());
					Pair<String, Boolean> response = executor.executeAction();
					if(response.getSecondValue()){
						clearContexts();
					}
					generatedResponse = executor.executeAction().getFirstValue();
				}
			}else if(statementInfo.getKnownActionWords().contains("cancel")){
				  generatedResponse = "Please provide order number";
				  userContextInfo.setPreviousContext(ActionType.CANCEL.toString());
			}

		}else{
			String previousUserContext = userContextInfo.getPreviousContext();
			if(previousUserContext == null){
				System.out.println("Previous Context is null");
			}else if(previousUserContext.equals(ActionType.BUY.toString())){
				  String itemName = userContextInfo.getMessage();
				  System.out.println(itemName);
				  executor.getChatBotAction().setItemName(statementInfo.getKnownItem());
				  executor.getChatBotAction().setAction(userContextInfo.getPreviousContext());
				  Pair<String, Boolean> response = executor.executeAction();
					if(response.getSecondValue()){
						clearContexts();
					}
					generatedResponse = response.getFirstValue();
			}else if(previousUserContext.equals(ActionType.CANCEL.toString())){
				  String orderNumber = userContextInfo.getMessage();
				  System.out.println(orderNumber);
				  executor.getChatBotAction().setAction(previousUserContext);
				  executor.getChatBotAction().setOrderNumber(orderNumber);
				  Pair<String, Boolean> response = executor.executeAction();
				  if(response.getSecondValue()){
						clearContexts();
					}
					generatedResponse = response.getFirstValue();

			}else{
			  setGeneratedResponse("Sorry i did not understand");
			}
		}
	}

	private String generateResponse(){
		executor.logChatWithUser(userContextInfo.getUserId(), userQuery, generatedResponse, helpful);
		return (generatedResponse==null)?"Sorry i didn't understand":generatedResponse;
	}

	private void findActionsInStatement(String query, StatementInfo info){
		for (String sentence : factory.segmentSentences(query)) {
            String[] tokens = factory.tokenizeSentence(sentence.toLowerCase());
            Span[] spans = factory.findAction(tokens);
            double[] spanProbs = factory.findActionProb(spans);
            int counter = 0;
            for (Span span : spans) {
                String knownWord = null;
                for (int i = span.getStart(); i < span.getEnd(); i++) {
                	knownWord = tokens[i];
                }
                if(spanProbs[counter] > 0.5) {
                	info.getKnownActionWords().add(knownWord);
                }
                counter++;
            }
        }
	}

	private void findItemInStatement(String query, StatementInfo info){
		for (String sentence : factory.segmentSentences(query)) {
            String[] tokens = factory.tokenizeSentence(sentence.toLowerCase());
            Span[] spans = factory.findItem(tokens);
            double[] spanProbs = factory.findItemProb(spans);
            int counter = 0;
            for (Span span : spans) {
                String knownWord = null;
                for (int i = span.getStart(); i < span.getEnd(); i++) {
                	knownWord = tokens[i];
                }
                if(spanProbs[counter] > 0.5) {
                	info.setKnownItem(knownWord);
                }
                counter++;
            }
        }
	}
	private String getQuestionType(String sentence){
		Parse topParses[] = ParserTool.parseLine(sentence, factory.getStatementParser(), 1);
		return topParses[0].getChildren()[0].getChildren()[0].getType();
	}

	private String getStatementType(String sentence){
		try {
			  Parse topParses[] = ParserTool.parseLine(sentence, factory.getStatementParser(), 1);
			  return topParses[0].getChildren()[0].getType();
			}catch(Exception ex){
				return null;
			}
	}

	private void clearContexts(){
		userContextInfo.setPreviousContext(null);
		helpful = (byte)1;
	}

	public String getGeneratedResponse() {
		return generatedResponse;
	}

	public void setGeneratedResponse(String generatedResponse) {
		this.generatedResponse = generatedResponse;
	}
}
