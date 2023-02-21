package com.senderman.lastkatkabot;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller
public class TestController {

    @Get("/")
    public String kek(){
        return "kek";
    }

}
