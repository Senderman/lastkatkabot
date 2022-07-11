package com.senderman.lastkatkabot.genshin.command;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.genshin.Item;
import com.senderman.lastkatkabot.genshin.dbservice.GenshinChatUserService;
import com.senderman.lastkatkabot.genshin.dbservice.GenshinUserInventoryItemService;
import com.senderman.lastkatkabot.genshin.model.GenshinChatUser;
import com.senderman.lastkatkabot.service.CurrentTime;
import com.senderman.lastkatkabot.service.fileupload.TelegramFileUploader;
import com.senderman.lastkatkabot.util.Html;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class WishCommand implements CommandExecutor {

    private final GenshinChatUserService userService;
    private final GenshinUserInventoryItemService inventoryItemService;
    private final CurrentTime currentTime;
    private final List<Item> genshinItems;
    private final TelegramFileUploader uploader;

    public WishCommand(
            GenshinChatUserService userService,
            GenshinUserInventoryItemService inventoryItemService,
            CurrentTime currentTime,
            @Qualifier("genshinItems") List<Item> genshinItems,
            TelegramFileUploader uploader
    ) {
        this.userService = userService;
        this.inventoryItemService = inventoryItemService;
        this.currentTime = currentTime;
        this.genshinItems = genshinItems;
        this.uploader = uploader;
    }

    @Override
    public String command() {
        return "/wish";
    }

    @Override
    public String getDescription() {
        return "Молитва (Genshin). Можно раз в день";
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {
        if (ctx.message().isUserMessage()) {
            ctx.replyToMessage("Команду нельзя использовать в ЛС!").callAsync(ctx.sender);
            return;
        }

        long chatId = ctx.chatId();
        long userId = ctx.user().getId();

        var genshinUser = userService.findByChatIdAndUserId(chatId, userId);
        var currentDay = Integer.parseInt(currentTime.getCurrentDay());

        if (genshinUser.getLastRollDate() == currentDay) {
            ctx.replyToMessage("Вы уже молились сегодня! Можно только раз в день!").callAsync(ctx.sender);
            return;
        }

        final int rate = getRandomRate(genshinUser);
        updatePity(genshinUser, rate);

        var possibleItems = genshinItems.stream()
                .filter(i -> i.getStars() == rate)
                .toList();
        var receivedItem = possibleItems.get(ThreadLocalRandom.current().nextInt(possibleItems.size()));

        genshinUser.setLastRollDate(currentDay);
        userService.save(genshinUser);

        var itemId = receivedItem.getId();
        var inventoryItem = inventoryItemService.findByChatIdAndUserIdAndItemId(chatId, userId, itemId);

        inventoryItem.incAmount();
        inventoryItemService.save(inventoryItem);

        String text = getFormattedItemReceiveMessage(ctx.user(), receivedItem);
        var itemPicture = getItemPictureById(itemId);
        uploader.sendPhoto(chatId, ctx.messageId(), text, itemPicture, itemId + ".webp");

    }

    private int getRandomRate(GenshinChatUser user) {
        if (user.getFourPity() >= 9)
            return 4;
        else if (user.getFivePity() >= 74) {
            return 5;
        }

        var random = ThreadLocalRandom.current().nextInt(1, 1000);
        if (random < 12)
            return 5;
        else if (random < 141)
            return 4;
        else
            return 3;
    }

    private void updatePity(GenshinChatUser user, int receivedItemStars) {
        switch (receivedItemStars) {
            case 3 -> {
                user.incFourPity();
                user.incFivePity();
            }
            case 4 -> {
                user.setFourPity(0);
                user.incFivePity();
            }
            case 5 -> {
                user.setFivePity(0);
                user.incFourPity();
            }
        }
    }

    private InputStream getItemPictureById(String id) {
        return getClass().getResourceAsStream("/genshin/images/" + id + ".webp");
    }

    private String getFormattedItemReceiveMessage(User user, Item item) {
        return "%s\n\n%s, ваша награда - %d⭐ <b>️%s</b>".formatted(
                item.getDescription(),
                Html.getUserLink(user),
                item.getStars(),
                item.getName()
        );
    }
}
