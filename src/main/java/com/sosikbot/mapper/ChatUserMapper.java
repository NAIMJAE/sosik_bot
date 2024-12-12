package com.sosikbot.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sosikbot.entity.ChatUser;


@Mapper
public interface ChatUserMapper {
    // INSERT New ChatUser or ChatGroup ChatId
    void insertChatUser(ChatUser chatUser);

    // SELECT Existing ChatUser
    ChatUser selectChatUser(@Param("chatId") String chatId);

    // SELECT Existing ChatUserList (Used to Send Message)
    List<ChatUser> selectChatUserList();
}
