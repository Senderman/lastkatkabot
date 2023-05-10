package com.senderman.lastkatkabot.feature.genshin.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.genshin.model.GenshinChatUser;
import com.senderman.lastkatkabot.feature.genshin.model.Item;
import com.senderman.lastkatkabot.feature.genshin.service.GenshinChatUserService;
import com.senderman.lastkatkabot.feature.genshin.service.GenshinUserInventoryItemService;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import com.senderman.lastkatkabot.util.CurrentTime;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Command
public class WishCommand implements CommandExecutor {

    private final GenshinChatUserService userService;
    private final GenshinUserInventoryItemService inventoryItemService;
    private final CurrentTime currentTime;
    private final List<Item> genshinItems;

    public WishCommand(
            GenshinChatUserService userService,
            GenshinUserInventoryItemService inventoryItemService,
            CurrentTime currentTime,
            @Named("genshinItems") List<Item> genshinItems
    ) {
        this.userService = userService;
        this.inventoryItemService = inventoryItemService;
        this.currentTime = currentTime;
        this.genshinItems = genshinItems;
    }

    @Override
    public String command() {
        return "/wish";
    }

    @Override
    public String getDescription() {
        return "genshin.wish.description";
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        if (ctx.message().isUserMessage()) {
            ctx.replyToMessage(ctx.getString("common.noUsageInPM")).callAsync(ctx.sender);
            return;
        }

        long chatId = ctx.chatId();
        long userId = ctx.user().getId();

        var genshinUser = userService.findByChatIdAndUserId(chatId, userId);
        var currentDay = Integer.parseInt(currentTime.getCurrentDay());

        if (genshinUser.getLastRollDate() == currentDay) {
            ctx.replyToMessage(ctx.getString("genshin.wish.wishedToday")).callAsync(ctx.sender);
            return;
        }

        final int rate = getRandomRate(genshinUser);
        updatePity(genshinUser, rate);

        var possibleItems = genshinItems.stream()
                .filter(i -> i.stars() == rate)
                .toList();
        var receivedItem = possibleItems.get(ThreadLocalRandom.current().nextInt(possibleItems.size()));

        genshinUser.setLastRollDate(currentDay);


        var itemId = receivedItem.id();
        var inventoryItem = inventoryItemService.findByChatIdAndUserIdAndItemId(chatId, userId, itemId);
        inventoryItem.incAmount();


        String text = getFormattedItemReceiveMessage(ctx.user(), receivedItem, ctx);
        var itemPicture = getItemPictureById(itemId);
        var answer = ctx.replyToMessageWithPhoto()
                .setCaption(text)
                .setFile(itemId + ".webp", itemPicture)
                .setParseMode(ParseMode.HTML)
                .call(ctx.sender);

        // do not save /wish results if message wasn't successfully sent
        if (answer != null) {
            inventoryItemService.save(inventoryItem);
            userService.save(genshinUser);
        }

    }

    private int getRandomRate(GenshinChatUser user) {
        if (user.getFourPity() >= 9)
            return 4;
        else if (user.getFivePity() >= 50) {
            return 5;
        }

        var random = ThreadLocalRandom.current().nextInt(1, 1000);
        if (random < 70)
            return 5;
        else if (random < 300)
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

    private String getFormattedItemReceiveMessage(User user, Item item, LocalizedMessageContext ctx) {
        return ctx.getString("genshin.wish.itemRecieve").formatted(
                item.description(),
                Html.getUserLink(user),
                item.stars(),
                item.name()
        );
    }
}
