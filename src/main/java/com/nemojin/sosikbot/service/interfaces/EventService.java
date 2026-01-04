package com.nemojin.sosikbot.service.interfaces;

public interface EventService<T> {
    T detectNewEvent() throws Exception;
}
