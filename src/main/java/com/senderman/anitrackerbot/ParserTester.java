package com.senderman.anitrackerbot;

import java.util.Scanner;

public class ParserTester {
    public static void main(String[] args) throws Exception {
        AnimeParser parser = AnimeParsers::parseAnidub;
        var scanner = new Scanner(System.in);
        var anime = parser.parse(scanner.next());
        System.out.println("Название: " + anime.getTitle());
        System.out.println("Серий готово: " + anime.getSeries());
        System.out.println("Ссылка на картинку: " + anime.getImg());
    }
}
