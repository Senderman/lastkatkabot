package com.senderman.lastkatkabot.service;

import com.annimon.tgbotsmodule.commands.TextCommand;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatInfoService;
import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class CommandAccessManager {

    private final ChatInfoService chatInfoService;
    private final Set<String> existingCommands;

    public CommandAccessManager(
            ChatInfoService chatInfoService,
            Set<CommandExecutor> executors
    ) {
        this.chatInfoService = chatInfoService;
        this.existingCommands = executors.stream()
                .map(TextCommand::command)
                .collect(Collectors.toSet());
    }

    public void allowCommands(long chatId, List<String> commands) throws CommandsNotExistsException {
        if (commands.size() == 0)
            return;

        validateCommandList(commands);
        var chatInfo = chatInfoService.findById(chatId);
        var forbiddenCommands = chatInfo.getForbiddenCommands();
        if (forbiddenCommands == null) {
            forbiddenCommands = new HashSet<>();
        }
        commands.forEach(forbiddenCommands::remove);
        chatInfo.setForbiddenCommands(forbiddenCommands);
        chatInfoService.save(chatInfo);
    }

    public void forbidCommands(long chatId, List<String> commands) throws CommandsNotExistsException {
        if (commands.size() == 0)
            return;
        validateCommandList(commands);
        var chatInfo = chatInfoService.findById(chatId);
        var forbiddenCommands = chatInfo.getForbiddenCommands();
        if (forbiddenCommands == null) {
            forbiddenCommands = new HashSet<>();
        }
        forbiddenCommands.addAll(commands);
        chatInfo.setForbiddenCommands(forbiddenCommands);
        chatInfoService.save(chatInfo);
    }

    private void validateCommandList(List<String> commands) throws CommandsNotExistsException {
        var notExistingCommands = commands.stream()
                .filter(c -> !existingCommands.contains(c))
                .toList();
        if (!notExistingCommands.isEmpty()) {
            throw new CommandsNotExistsException(notExistingCommands);
        }
    }


    public static class CommandsNotExistsException extends Exception {

        private final List<String> commands;

        public CommandsNotExistsException(List<String> commands) {
            super("Commands " + String.join(", ", commands) + " do not exist!");
            this.commands = commands;
        }

        public List<String> getCommands() {
            return commands;
        }
    }
}
