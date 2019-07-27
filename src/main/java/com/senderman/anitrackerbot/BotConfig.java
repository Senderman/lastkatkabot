package com.senderman.anitrackerbot;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BotConfig {

    @JsonProperty
    private String token;

    @JsonProperty
    private String username;

    @JsonProperty
    private String help;

    @JsonProperty
    private String anidata;

    @JsonProperty
    private int position;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getAnidata() {
        return anidata;
    }

    public void setAnidata(String anidata) {
        this.anidata = anidata;
    }
}
