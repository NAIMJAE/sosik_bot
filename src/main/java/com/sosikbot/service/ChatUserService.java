package com.sosikbot.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import com.sosikbot.entity.ChatUser;
import com.sosikbot.mapper.ChatUserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatUserService {
    
    private final ChatUserMapper chatUserMapper;

    // INSERT New ChatUser or ChatGroup ChatId
    public String insertChatUser(String chatId) {
        ChatUser chatUser = chatUserMapper.selectChatUser(chatId);

        if (chatUser == null) {
            ChatUser newChatUser = new ChatUser(chatId, LocalDate.now());
            chatUserMapper.insertChatUser(newChatUser);
            return "🖐️ 안녕하세요 소식봇입니다. 🖐️";
        }else {
            return null;
        }
    }
    
    // SELECT Existing ChatUserList (Used to Send Message)
    public List<ChatUser> selectChatUserList() {
        return chatUserMapper.selectChatUserList();
    }

}
