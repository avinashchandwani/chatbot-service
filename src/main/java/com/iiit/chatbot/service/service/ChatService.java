package com.iiit.chatbot.service.service;

import com.iiit.chatbot.service.entity.Constants;
import com.iiit.chatbot.service.entity.UserContextInformation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ChatService {


    @RequestMapping(value = "/query", produces = Constants.APPLICATION_JSON, method = RequestMethod.POST)
    public void chatWithUser(@RequestBody UserContextInformation contextInformation) {
        
    }
}
