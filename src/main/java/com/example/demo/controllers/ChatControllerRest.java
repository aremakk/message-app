package com.example.demo.controllers;

import com.example.demo.entity.Chat;
import com.example.demo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatControllerRest {

    private final ChatService chatService;

    @PostMapping("/private")
    public Chat createPrivate(
            @RequestParam Long user1,
            @RequestParam Long user2
    ) {
        return chatService.createPrivateChat(user1, user2);
    }

    @PostMapping("/group")
    public Chat createGroup(
            @RequestParam String title,
            @RequestBody List<Long> users
    ) {
        return chatService.createGroupChat(title, users);
    }
}
