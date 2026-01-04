package com.nemojin.sosikbot.service.user;

import com.nemojin.sosikbot.mapper.ChatUserMapper;
import com.nemojin.sosikbot.model.ChatUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatUserService {
    private final ChatUserMapper chatUserMapper;

    /// Get ChatUser List for Used to Send Message
    public List<ChatUser> getChatUserList() {
        return chatUserMapper.selectChatUserList();
    }
}
