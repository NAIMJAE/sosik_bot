package com.nemojin.sosikbot.mapper;

import com.nemojin.sosikbot.model.ChatUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatUserMapper {
    /// [SELECT] Finds all chatUser list
    List<ChatUser> selectChatUserList();

    /// [SELECT] Finds chatUser
    ChatUser selectChatUser(@Param("chatId") String chatId);

    /// [INSERT] Insert New ChatUser
    int insertChatUser(ChatUser chatUser);
}
