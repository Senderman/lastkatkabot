package com.senderman.lastkatkabot.annotation;

import com.senderman.lastkatkabot.Role;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Command {

    String command();

    String description();

    boolean showInHelp() default true;

    Role[] authority() default Role.USER;

    String[] aliases() default {};

}
