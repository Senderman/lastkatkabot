package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Component
public class WhereUser implements CommandExecutor {

    private final ChatUserService chatUsers;
    private final ExecutorService threadPool;

    public WhereUser(ChatUserService chatUsers, ExecutorService threadPool) {
        this.chatUsers = chatUsers;
        this.threadPool = threadPool;
    }

    @Override
    public String getDescription() {
        return "в каких чатах сидит юзер. реплай или " + getTrigger() + " userId";
    }

    @Override
    public String getTrigger() {
        return "/wru";
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void execute(Message message, CommonAbsSender telegram) {
        int userId;
        try {
            var args = message.getText().split("\\s+");
            userId = args.length > 1 ? Integer.parseInt(args[1]) : message.getReplyToMessage().getFrom().getId();
        } catch (NumberFormatException e) {
            ApiRequests.answerMessage(message, "Id юзера - это число!").callAsync(telegram);
            return;
        } catch (NullPointerException e) {
            ApiRequests.answerMessage(message, "Введите Id юзера, либо используйте реплай").callAsync(telegram);
            return;
        }
        threadPool.execute(() -> {
            var chatNames = chatUsers.findByUserId(userId)
                    .stream()
                    .map(chat -> getChatNameOrNull(chat.getChatId(), telegram))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n"));

            ApiRequests.answerMessage(message, "🕵️‍♂ Юзер замечен в следующих чатах:\n\n️" + chatNames).callAsync(telegram);
        });
    }

    private String getChatNameOrNull(long chatId, CommonAbsSender telegram) {
        var chat = Methods.getChat(chatId).call(telegram);
        return chat != null ? chat.getTitle() : null;
    }
}
