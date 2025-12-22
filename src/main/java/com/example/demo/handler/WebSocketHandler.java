package com.example.demo.handler;

import com.example.demo.message.CustomMessage;
import com.example.demo.websocket.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.WebSocketSession; 
import org.springframework.web.socket.TextMessage; 
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends AbstractWebSocketHandler{

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        log.info("Connected");
//        System.out.println(session);
        String userName = null;
        log.info(session.getUri().getQuery());
        String query = session.getUri().getQuery();
        if(query != null){
            String[] queryParams = query.split("&");
            for(String param : queryParams){
                String[] keyValue = param.split("=");
                if(keyValue[0].equals("username") && keyValue.length > 1){
                    userName = keyValue[1];
                    break;
                }
            }
        }

        if(userName != null){
            sessions.put(session.getId(), new UserSession(session, userName));
            sendMessageToAll("User : " + userName + " joined");
        }

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        String request = sessions.get(session.getId()).getUsername() + " : " + message.getPayload();
        sendMessageToAll( request);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

        String payload = message.getPayload().toString();
        CustomMessage parsedMessage = parsedMessage(payload);

        if(parsedMessage != null){
            switch(parsedMessage.getType()){
                case "CHAT" -> handleChatMessage(session, parsedMessage);
                case "COMMAND" -> handleCommand(session, parsedMessage);
            }
        }

    }

    private void handleChatMessage(WebSocketSession session, CustomMessage message) {
        String userName = sessions.get(session.getId()).getUsername();
        sendMessageToAll(userName + " : " + message.getContent());
    }

    private void handleCommand(WebSocketSession session, CustomMessage message) {
        switch(message.getContent()){
            case "disconnect" -> {
                try{
                    sessions.remove(session.getId());
                    session.close();
//                    sendMessage(session, "Disconnected" + message.getContent());
                }catch (IOException e){
                    System.err.println("Error: " + e.getMessage());
                }
            }
            case "listUsers" -> {
                String activeUsers = getActiveUsers();
                sendMessage(session," Activee users: " + activeUsers);
            }
        }
    }

    private void sendMessage(WebSocketSession session, String message) {
        try{
            session.sendMessage(new TextMessage(message));
        }catch (IOException e){
            System.err.println("Error: " + e.getMessage());
        }
    }

    private String getActiveUsers(){
        return sessions.values().stream()
                .map(UserSession::getUsername)
                .collect(Collectors.joining(", "));
    }


    private CustomMessage parsedMessage(String payload){
        try{
            return objectMapper.readValue(payload, CustomMessage.class);
        }catch (JsonProcessingException e){
            log.info("Error parsing message" + e.getMessage());
            return null;
        }
    }

    private void sendMessageToAll(String message){
        for(UserSession userSession : sessions.values()){
            try{
                userSession.getSession().sendMessage(new TextMessage(message));
            }catch(Exception e){
                System.err.println("Error sending message to all: " + message);
            }
        }
    }

}