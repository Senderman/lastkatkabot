package com.senderman;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name();
    String desc();
    boolean showInHelp() default true;
    boolean forAllAdmins() default false;
    boolean forPremium() default false;
    boolean forMainAdmin() default false;
}
