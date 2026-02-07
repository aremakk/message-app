package com.example.demo.controllers;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.entity.Chat;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageDto dto) {

        Chat chat = chatRepository.findById(dto.getChatId())
                .orElseThrow();

        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow();

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(dto.getContent());

        messageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + chat.getId(),
                dto
        );
    }
}