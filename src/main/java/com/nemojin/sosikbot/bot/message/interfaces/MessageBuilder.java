package com.nemojin.sosikbot.bot.message.interfaces;

public interface MessageBuilder<T, E> {
    T buildMessage(String exchange, E eventList);
}
