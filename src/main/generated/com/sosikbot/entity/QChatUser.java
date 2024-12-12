package com.sosikbot.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChatUser is a Querydsl query type for ChatUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChatUser extends EntityPathBase<ChatUser> {

    private static final long serialVersionUID = 46706971L;

    public static final QChatUser chatUser = new QChatUser("chatUser");

    public final DatePath<java.time.LocalDate> chatDate = createDate("chatDate", java.time.LocalDate.class);

    public final StringPath chatId = createString("chatId");

    public QChatUser(String variable) {
        super(ChatUser.class, forVariable(variable));
    }

    public QChatUser(Path<? extends ChatUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChatUser(PathMetadata metadata) {
        super(ChatUser.class, metadata);
    }

}

