package com.example.demo.config;

import com.example.demo.handler.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import com.example.demo.handler.WebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer{
    
    private final WebSocketHandler webSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){

        registry.addHandler(webSocketHandler, "/chat").setAllowedOrigins("*");
        registry.addHandler(chatWebSocketHandler, "/webchat").setAllowedOrigins("*");
    }
    
}
