package com.example.demo.service;

import com.example.demo.entity.Chat;
import com.example.demo.entity.ChatParticipant;
import com.example.demo.entity.ChatType;
import com.example.demo.repository.ChatParticipantRepository;
import com.example.demo.repository.ChatRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public Chat createPrivateChat(Long user1, Long user2) {

        Chat chat = new Chat();
        chat.setType(ChatType.PRIVATE);
        chat = chatRepository.save(chat);

        addUser(chat, user1);
        addUser(chat, user2);

        return chat;
    }

    public Chat createGroupChat(String title, List<Long> users) {

        Chat chat = new Chat();
        chat.setType(ChatType.GROUP);
        chat.setTitle(title);
        chat = chatRepository.save(chat);

        Chat finalChat = chat;
        users.forEach(id -> addUser(finalChat, id));
        return chat;
    }

    private void addUser(Chat chat, Long userId) {
        ChatParticipant cp = new ChatParticipant();
        cp.setChat(chat);
        cp.setUser(userRepository.findById(userId).orElseThrow());
        participantRepository.save(cp);
    }
}