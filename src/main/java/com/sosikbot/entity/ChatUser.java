package com.sosikbot.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chatuser")
public class ChatUser {
    @Id
    private String chatId;
    private LocalDate chatDate;
}
