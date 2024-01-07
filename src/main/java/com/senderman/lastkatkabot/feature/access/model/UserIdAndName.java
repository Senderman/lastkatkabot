package com.senderman.lastkatkabot.feature.access.model;

public interface UserIdAndName<TId> {

    TId getUserId();

    String getName();

    void setUserId(TId userId);

    void setName(String name);

}
