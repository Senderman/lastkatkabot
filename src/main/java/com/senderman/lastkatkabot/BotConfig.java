package com.senderman.lastkatkabot;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class BotConfig {

    @JsonProperty(required = true)
    private String token;
    
    @JsonProperty(required = true)
    private String username;

    @JsonProperty
    private int position;

    @JsonProperty
    private int mainAdmin;

    @JsonProperty
    private long lastvegan;
    
    @JsonProperty
    private long tourgroup;

    @JsonProperty
    private String tourchannel;

    @JsonProperty
    private String tourgroupname;

    @JsonProperty
    private Set<String> wwBots;

    @JsonProperty
    private Set<String> veganWarsCommands;

    @JsonProperty
    private String bncphoto;

    @JsonProperty
    private String leavesticker;

    @JsonProperty
    private String higif;

    @JsonProperty
    private String help;

    @JsonProperty
    private String adminHelp;

    @JsonProperty
    private String mainAdminHelp;

    @JsonProperty
    private String setupHelp;

    @JsonProperty
    private String[] loveStrings;

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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getMainAdmin() {
        return mainAdmin;
    }

    public void setMainAdmin(int mainAdmin) {
        this.mainAdmin = mainAdmin;
    }

    public long getLastvegan() {
        return lastvegan;
    }

    public void setLastvegan(long lastvegan) {
        this.lastvegan = lastvegan;
    }

    public long getTourgroup() {
        return tourgroup;
    }

    public void setTourgroup(long tourgroup) {
        this.tourgroup = tourgroup;
    }

    public String getTourgroupname() {
        return tourgroupname;
    }

    public void setTourgroupname(String tourgroupname) {
        this.tourgroupname = tourgroupname;
    }

    public String getTourchannel() {
        return tourchannel;
    }

    public void setTourchannel(String tourchannel) {
        this.tourchannel = tourchannel;
    }

    public Set<String> getWwBots() {
        return wwBots;
    }

    public void setWwBots(Set<String> wwBots) {
        this.wwBots = wwBots;
    }

    public Set<String> getVeganWarsCommands() {
        return veganWarsCommands;
    }

    public void setVeganWarsCommands(Set<String> veganWarsCommands) {
        this.veganWarsCommands = veganWarsCommands;
    }

    public String getBncphoto() {
        return bncphoto;
    }

    public void setBncphoto(String bncphoto) {
        this.bncphoto = bncphoto;
    }

    public String getLeavesticker() {
        return leavesticker;
    }

    public void setLeavesticker(String leavesticker) {
        this.leavesticker = leavesticker;
    }

    public String getHigif() {
        return higif;
    }

    public void setHigif(String higif) {
        this.higif = higif;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getAdminHelp() {
        return adminHelp;
    }

    public void setAdminHelp(String adminHelp) {
        this.adminHelp = adminHelp;
    }

    public String getMainAdminHelp() {
        return mainAdminHelp;
    }

    public void setMainAdminHelp(String mainAdminHelp) {
        this.mainAdminHelp = mainAdminHelp;
    }

    public String getSetupHelp() {
        return setupHelp;
    }

    public void setSetupHelp(String setupHelp) {
        this.setupHelp = setupHelp;
    }

    public String[] getLoveStrings() {
        return loveStrings;
    }

    public void setLoveStrings(String[] loveStrings) {
        this.loveStrings = loveStrings;
    }
}
