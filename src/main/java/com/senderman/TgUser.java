package com.senderman;

public class TgUser {

    private final int id;
    protected String name;

    public TgUser(int id, String name) {
        this.id = id;
        this.name = name.replace("<", "&lt;").replace(">", "&gt;");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return String.format("<a href=\"tg://user?id=%1$d\">%2$s</a>", id, name);
    }
}