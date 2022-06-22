package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DivorceCallback implements CallbackExecutor {

    private final UserStatsService users;

    public DivorceCallback(UserStatsService users) {
        this.users = users;
    }

    @Override
    public String command() {
        return Callbacks.DIVORCE;
    }

    @Override
    public void accept(@NotNull CallbackQueryContext ctx) {
        var userId = ctx.user().getId();

        if (!userId.equals(Long.parseLong(ctx.argument(1)))) {
            ctx.answerAsAlert("Это не вам!").callAsync(ctx.sender);
            return;
        }

        if (ctx.argument(0).equals("a"))
            acceptDivorce(ctx);
        else
            declineDivorce(ctx);


    }

    private void acceptDivorce(CallbackQueryContext ctx) {
        var userId = ctx.user().getId();
        var userStats = users.findById(userId);
        var loverId = userStats.getLoverId();

        if (loverId == null || !loverId.equals(Long.parseLong(ctx.argument(2)))) {
            ctx.answerAsAlert("У вас сменилась/пропала пара, данная кнопка не актуальна!").callAsync(ctx.sender);
            Methods.deleteMessage(ctx.message().getChatId(), ctx.message().getMessageId()).callAsync(ctx.sender);
            return;
        }

        var loverStats = users.findById(loverId);
        userStats.setLoverId(null);
        loverStats.setLoverId(null);
        users.saveAll(List.of(userStats, loverStats));

        ctx.answerAsAlert("Вы расстались со своей половинкой!").callAsync(ctx.sender);
        ctx.editMessage("\uD83D\uDE1E Вы расстались со своей половинкой!").callAsync(ctx.sender);
        Methods.sendMessage(loverId, "Ваша половинка решила с вами расстаться :(").callAsync(ctx.sender);
    }

    private void declineDivorce(CallbackQueryContext ctx) {
        ctx.answer("Вы отменили развод!").callAsync(ctx.sender);
        ctx.editMessage("\uD83D\uDE04 Развод отменен, расходимся!").callAsync(ctx.sender);
    }
}
