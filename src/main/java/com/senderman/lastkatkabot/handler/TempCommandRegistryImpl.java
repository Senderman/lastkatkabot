package com.senderman.lastkatkabot.handler;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.commands.CallbackQueryCommand;
import com.annimon.tgbotsmodule.commands.InlineQueryCommand;
import com.annimon.tgbotsmodule.commands.RegexCommand;
import com.annimon.tgbotsmodule.commands.TextCommand;
import com.annimon.tgbotsmodule.commands.authority.Authority;
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.annimon.tgbotsmodule.commands.context.InlineQueryContext;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.commands.context.RegexMessageContext;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO remove when tgbots-module 7.0 is released

public class TempCommandRegistryImpl<TRole extends Enum<TRole>> {
    private final String botUsername;
    private final ListMultimap<String, TextCommand> textCommands;
    private final List<RegexCommand> regexCommands;
    private final ListMultimap<String, CallbackQueryCommand> callbackCommands;
    private final ListMultimap<String, InlineQueryCommand> inlineCommands;
    private final Authority<TRole> authority;

    private String callbackCommandSplitPattern;

    public TempCommandRegistryImpl(@NotNull String username, @NotNull Authority<TRole> authority) {
        this.authority = authority;
        this.botUsername = "@" + username.toLowerCase(Locale.ENGLISH);
        textCommands = ArrayListMultimap.create();
        regexCommands = new ArrayList<>();
        callbackCommands = ArrayListMultimap.create();
        inlineCommands = ArrayListMultimap.create();

        callbackCommandSplitPattern = ":";
    }

    public TempCommandRegistryImpl<TRole> register(@NotNull TextCommand command) {
        Objects.requireNonNull(command);
        Stream.concat(Stream.of(command.command()), command.aliases().stream())
                .map(this::stringToCommand)
                .forEach(key -> textCommands.put(key, command));
        return this;
    }

    public TempCommandRegistryImpl<TRole> register(@NotNull RegexCommand command) {
        Objects.requireNonNull(command);
        regexCommands.add(command);
        return this;
    }

    public TempCommandRegistryImpl<TRole> register(@NotNull CallbackQueryCommand command) {
        Objects.requireNonNull(command);
        callbackCommands.put(command.command(), command);
        return this;
    }

    public TempCommandRegistryImpl<TRole> register(@NotNull InlineQueryCommand command) {
        Objects.requireNonNull(command);
        inlineCommands.put(command.command(), command);
        return this;
    }

    /**
     * Splits {@code callback.data} by whitespace ({@code "cmd:args"})
     *
     * @return this
     */
    public TempCommandRegistryImpl<TRole> splitCallbackCommandByColon() {
        return splitCallbackCommandByPattern(":");
    }

    /**
     * Splits {@code callback.data} by whitespace ({@code "cmd args"})
     *
     * @return this
     */
    public TempCommandRegistryImpl<TRole> splitCallbackCommandByWhitespace() {
        return splitCallbackCommandByPattern("\\s+");
    }

    /**
     * Treats whole {@code callback.data} as command ({@code "cmd"})
     *
     * @return this
     */
    public TempCommandRegistryImpl<TRole> doNotSplitCallbackCommands() {
        return splitCallbackCommandByPattern("$");
    }

    public TempCommandRegistryImpl<TRole> splitCallbackCommandByPattern(@NotNull String pattern) {
        this.callbackCommandSplitPattern = Objects.requireNonNull(pattern);
        return this;
    }

    public boolean handleUpdate(@NotNull Update update, BotHandler handler) {
        if (update.hasMessage()) {
            // Text commands
            if (update.getMessage().hasText()) {
                if ((!textCommands.isEmpty()) && handleTextCommands(update, handler)) {
                    return true;
                }
                if ((!regexCommands.isEmpty()) && handleRegexCommands(update, handler)) {
                    return true;
                }
            }
        } else if (update.hasCallbackQuery()) {
            // Callback query commands
            final var data = update.getCallbackQuery().getData();
            if (data != null && !data.isEmpty()) {
                if ((!callbackCommands.isEmpty()) && handleCallbackQueryCommands(update, handler)) {
                    return true;
                }
            }
        } else if (update.hasInlineQuery()) {
            // Inline query commands
            final var query = update.getInlineQuery().getQuery();
            if ((!inlineCommands.isEmpty()) && handleInlineQueryCommands(update, handler)) {
                return true;
            }
        }
        return false;
    }

    protected boolean handleTextCommands(@NotNull Update update, BotHandler handler) {
        final var message = update.getMessage();
        final var args = message.getText().split("\\s+", 2);
        final var command = stringToCommand(args[0]);
        final var commands = Stream.ofNullable(textCommands.get(command))
                .flatMap(Collection::stream)
                .filter(cmd -> authority.hasRights(update, message.getFrom(), cmd.authority()))
                .collect(Collectors.toList());
        if (commands.isEmpty()) {
            return false;
        }

        final var commandArguments = args.length >= 2 ? args[1] : "";
        final var context = new MessageContext(handler, update, commandArguments);
        for (TextCommand cmd : commands) {
            cmd.accept(context);
        }
        return true;
    }

    protected boolean handleRegexCommands(@NotNull Update update, BotHandler handler) {
        final var message = update.getMessage();
        final var text = message.getText();
        final long count = regexCommands.stream()
                .map(cmd -> Map.entry(cmd, cmd.pattern().matcher(text)))
                .filter(e -> e.getValue().find())
                .filter(e -> authority.hasRights(update, message.getFrom(), e.getKey().authority()))
                .peek(e -> {
                    final RegexCommand command = e.getKey();
                    final var matcher = e.getValue();
                    command.accept(new RegexMessageContext(handler, update, text, matcher));
                })
                .count();
        return (count > 0);
    }

    protected boolean handleCallbackQueryCommands(@NotNull Update update, BotHandler handler) {
        final var query = update.getCallbackQuery();
        final var args = query.getData().split(callbackCommandSplitPattern, 2);
        final var command = args[0];
        final var commands = Stream.ofNullable(callbackCommands.get(command))
                .flatMap(Collection::stream)
                .filter(cmd -> authority.hasRights(update, query.getFrom(), cmd.authority()))
                .collect(Collectors.toList());
        if (commands.isEmpty()) {
            return false;
        }

        final var commandArguments = args.length >= 2 ? args[1] : "";
        final var context = new CallbackQueryContext(handler, update, commandArguments);
        for (CallbackQueryCommand cmd : commands) {
            cmd.accept(context);
        }
        return true;
    }

    private boolean handleInlineQueryCommands(Update update, BotHandler handler) {
        final var inlineQuery = update.getInlineQuery();
        final var query = inlineQuery.getQuery();
        final var args = query.split("\\s+", 2);
        final var command = args[0];
        final var commands = Stream.ofNullable(inlineCommands.get(command))
                .flatMap(Collection::stream)
                .filter(cmd -> authority.hasRights(update, inlineQuery.getFrom(), cmd.authority()))
                .collect(Collectors.toList());
        if (commands.isEmpty()) {
            return false;
        }

        final var commandArguments = args.length >= 2 ? args[1] : "";
        final var context = new InlineQueryContext(handler, update, commandArguments);
        for (InlineQueryCommand cmd : commands) {
            cmd.accept(context);
        }
        return true;
    }

    protected String stringToCommand(String str) {
        return str.toLowerCase(Locale.ENGLISH).replace(botUsername, "");
    }
}
