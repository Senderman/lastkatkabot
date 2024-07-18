package com.senderman.lastkatkabot.handler;

import com.annimon.tgbotsmodule.commands.CallbackQueryCommand;
import com.annimon.tgbotsmodule.commands.InlineQueryCommand;
import com.annimon.tgbotsmodule.commands.RegexCommand;
import com.annimon.tgbotsmodule.commands.TextCommand;
import com.annimon.tgbotsmodule.commands.authority.Authority;
import com.annimon.tgbotsmodule.commands.context.InlineQueryContext;
import com.annimon.tgbotsmodule.commands.context.RegexMessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.bnc.command.BncTelegramHandler;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.l10n.service.L10nService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.stream.Stream;

@Singleton
public class UpdateHandler implements com.annimon.tgbotsmodule.analytics.UpdateHandler {

    private final static String COMMAND_METER_NAME = "bot.command";
    private final static String CALLBACK_METER_NAME = "bot.callback";
    private final String botUsername;
    private final Map<String, List<TextCommand>> textCommands;
    private final List<RegexCommand> regexCommands;
    private final Map<String, List<CallbackQueryCommand>> callbackCommands;
    private final Map<String, List<InlineQueryCommand>> inlineCommands;
    private final Authority<Role> authority;
    private final MeterRegistry meterRegistry;
    private final L10nService l10nService;
    private String callbackCommandSplitPattern;

    public UpdateHandler(
            BotConfig config,
            Authority<Role> authority,
            MeterRegistry meterRegistry,
            Set<CommandExecutor> commands,
            Set<CallbackExecutor> callbacks,
            BncTelegramHandler bncTelegramHandler,
            L10nService l10nService) {
        this.authority = authority;
        this.meterRegistry = meterRegistry;
        this.botUsername = "@" + config.getUsername().toLowerCase(Locale.ENGLISH);
        this.l10nService = l10nService;
        textCommands = new HashMap<>();
        regexCommands = new ArrayList<>();
        callbackCommands = new HashMap<>();
        inlineCommands = new HashMap<>();

        splitCallbackCommandByWhitespace()
                .register(bncTelegramHandler);

        commands.forEach(this::register);
        callbacks.forEach(this::register);
    }

    private static <K, V> void addCommand(Map<K, List<V>> map, K key, V value) {
        map.computeIfAbsent(key, (k) -> new ArrayList<>()).add(value);
    }

    public UpdateHandler register(@NotNull RegexCommand command) {
        Objects.requireNonNull(command);
        regexCommands.add(command);
        return this;
    }

    public UpdateHandler register(@NotNull TextCommand command) {
        Objects.requireNonNull(command);
        Stream.concat(Stream.of(command.command()), command.aliases().stream())
                .map(this::stringToCommand)
                .forEach(key -> addCommand(textCommands, key, command));
        return this;
    }

    /**
     * Splits {@code callback.data} by whitespace ({@code "cmd args"})
     *
     * @return this
     */
    public UpdateHandler splitCallbackCommandByWhitespace() {
        return splitCallbackCommandByPattern("\\s+");
    }


    public UpdateHandler splitCallbackCommandByPattern(@NotNull String pattern) {
        this.callbackCommandSplitPattern = Objects.requireNonNull(pattern);
        return this;
    }

    @Override
    public boolean handleUpdate(@NotNull CommonAbsSender sender, @NotNull Update update) {
        if (update.hasMessage()) {
            // Text commands
            if (update.getMessage().hasText()) {
                if ((!textCommands.isEmpty()) && handleTextCommands(sender, update)) {
                    return true;
                }
                return (!regexCommands.isEmpty()) && handleRegexCommands(sender, update);
            }
        } else if (update.hasCallbackQuery()) {
            // Callback query commands
            final var data = update.getCallbackQuery().getData();
            if (data != null && !data.isEmpty()) {
                return (!callbackCommands.isEmpty()) && handleCallbackQueryCommands(sender, update);
            }
        } else if (update.hasInlineQuery()) {
            // Inline query commands
            return (!inlineCommands.isEmpty()) && handleInlineQueryCommands(sender, update);
        }
        return false;
    }

    protected boolean handleTextCommands(@NotNull CommonAbsSender sender, @NotNull Update update) {
        final var message = update.getMessage();
        final var args = message.getText().split("\\s+", 2);
        final var command = stringToCommand(args[0]);
        final var commands = Stream.ofNullable(textCommands.get(command))
                .flatMap(Collection::stream)
                .filter(cmd -> authority.hasRights(sender, update, message.getFrom(), cmd.authority()))
                .toList();
        if (commands.isEmpty()) {
            return false;
        }

        final var commandArguments = args.length >= 2 ? args[1] : "";
        final var context = new L10nMessageContext(sender, update, commandArguments, l10nService);
        for (TextCommand cmd : commands) {
            cmd.accept(context);
            meterRegistry.counter(COMMAND_METER_NAME, "command", cmd.command().replaceFirst("/", "")).increment();
        }
        return true;
    }

    protected boolean handleRegexCommands(@NotNull CommonAbsSender sender, @NotNull Update update) {
        final var message = update.getMessage();
        final var text = message.getText();
        final long count = regexCommands.stream()
                .map(cmd -> Map.entry(cmd, cmd.pattern().matcher(text)))
                .filter(e -> e.getValue().matches())
                .filter(e -> authority.hasRights(sender, update, message.getFrom(), e.getKey().authority()))
                .peek(e -> {
                    final RegexCommand command = e.getKey();
                    final var matcher = e.getValue();
                    command.accept(new RegexMessageContext(sender, update, text, matcher));
                })
                .count();
        return (count > 0);
    }

    protected boolean handleCallbackQueryCommands(@NotNull CommonAbsSender sender, @NotNull Update update) {
        final var query = update.getCallbackQuery();
        final var args = query.getData().split(callbackCommandSplitPattern, 2);
        final var command = args[0];
        final var commands = Stream.ofNullable(callbackCommands.get(command))
                .flatMap(Collection::stream)
                .filter(cmd -> authority.hasRights(sender, update, query.getFrom(), cmd.authority()))
                .toList();
        if (commands.isEmpty()) {
            return false;
        }

        final var commandArguments = args.length >= 2 ? args[1] : "";
        final var context = new L10nCallbackQueryContext(sender, update, commandArguments, l10nService);
        for (CallbackQueryCommand cmd : commands) {
            cmd.accept(context);
            meterRegistry.counter(CALLBACK_METER_NAME, "command", cmd.command()).increment();
        }
        return true;
    }

    private boolean handleInlineQueryCommands(@NotNull CommonAbsSender sender, @NotNull Update update) {
        final var inlineQuery = update.getInlineQuery();
        final var query = inlineQuery.getQuery();
        final var args = query.split("\\s+", 2);
        final var command = args[0];
        final var commands = Stream.ofNullable(inlineCommands.get(command))
                .flatMap(Collection::stream)
                .filter(cmd -> authority.hasRights(sender, update, inlineQuery.getFrom(), cmd.authority()))
                .toList();
        if (commands.isEmpty()) {
            return false;
        }

        final var commandArguments = args.length >= 2 ? args[1] : "";
        final var context = new InlineQueryContext(sender, update, commandArguments);
        for (InlineQueryCommand cmd : commands) {
            cmd.accept(context);
        }
        return true;
    }

    protected String stringToCommand(String str) {
        return str.toLowerCase(Locale.ENGLISH).replace(botUsername, "");
    }

    public UpdateHandler register(@NotNull CallbackQueryCommand command) {
        Objects.requireNonNull(command);
        addCommand(callbackCommands, command.command(), command);
        return this;
    }

}
