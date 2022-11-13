package com.senderman.lastkatkabot.config;

import com.senderman.lastkatkabot.annotation.Callback;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.callback.CallbackExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

@Configuration
public class HandlerBeanProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        try {
            if (bean instanceof CommandExecutor exe)
                return postProcessCommand(exe);
            else if (bean instanceof CallbackExecutor exe)
                return postProcessCallback(exe);
            else
                return bean;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object postProcessCommand(@NotNull CommandExecutor bean) throws NoSuchFieldException, IllegalAccessException {
        var clazz = CommandExecutor.class;
        var meta = bean.getClass().getDeclaredAnnotation(Command.class);
        if (meta == null)
            return bean;

        var commandField = clazz.getDeclaredField("command");
        var descriptionField = clazz.getDeclaredField("description");
        var showInHelpField = clazz.getDeclaredField("showInHelp");
        var authorityField = clazz.getDeclaredField("authority");
        var aliasesField = clazz.getDeclaredField("aliases");
        setField(commandField, bean, meta.command());
        setField(descriptionField, bean, meta.description());
        setField(showInHelpField, bean, meta.showInHelp());
        setField(authorityField, bean, EnumSet.copyOf(Arrays.asList(meta.authority())));
        var aliases = meta.aliases().length != 0 ? Set.of(meta.aliases()) : Set.of();
        setField(aliasesField, bean, aliases);
        return bean;
    }

    private Object postProcessCallback(@NotNull CallbackExecutor bean) throws NoSuchFieldException, IllegalAccessException {
        var clazz = CallbackExecutor.class;
        var meta = bean.getClass().getDeclaredAnnotation(Callback.class);
        if (meta == null)
            return bean;

        var commandField = clazz.getDeclaredField("command");
        var authorityField = clazz.getDeclaredField("authority");
        setField(commandField, bean, meta.value());
        setField(authorityField, bean, EnumSet.copyOf(Arrays.asList(meta.authority())));
        return bean;
    }

    private void setField(Field field, Object bean, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(bean, value);
    }
}
