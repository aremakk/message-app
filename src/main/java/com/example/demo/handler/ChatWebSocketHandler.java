package com.example.demo.handler;

import com.example.demo.message.ChatCustomMessage;
import com.example.demo.model.ChatMessage;
import com.example.demo.service.ChatMessageService;
import com.example.demo.websocket.UserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends AbstractWebSocketHandler {

    private Map<String, UserSession> sessions = new ConcurrentHashMap<>();
    private Map<String,String> usernameToSessionIdMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
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
            usernameToSessionIdMap.put(userName,session.getId());
        }

    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        ChatCustomMessage chatCustomMessage = parseMessage(payload);

        if(chatCustomMessage != null){
            handleChatMessage(session, chatCustomMessage);
        }
    }

    private void handleChatMessage(WebSocketSession session, ChatCustomMessage message) {
        String sender = sessions.get(session.getId()).getUsername();
        String receiver = message.getReceiver();

        chatMessageService.saveMessage(sender, receiver, message.getContent());

        String sessionId = usernameToSessionIdMap.get(receiver);
        if(sessionId != null){
            UserSession userSession = sessions.get(sessionId);
            if(userSession != null){
                try{
                    userSession.getSession().sendMessage(new TextMessage(sender + ": " + message.getContent()));
                } catch (IOException e) {
                    System.err.println("error " + e.getMessage());
                }
            }
        }
    }

    private ChatCustomMessage parseMessage(String payload){
        try{
            return objectMapper.readValue(payload, ChatCustomMessage.class);
        }catch(JsonProcessingException e){
            log.info("error parse message " + e.getMessage());
            return null;
        }
    }
}
