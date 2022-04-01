package com.senderman.lastkatkabot.genshin.command;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.genshin.Item;
import com.senderman.lastkatkabot.genshin.dbservice.GenshinUserInventoryItemService;
import com.senderman.lastkatkabot.genshin.model.GenshinUserInventoryItem;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InvCommand implements CommandExecutor {

    private final GenshinUserInventoryItemService inventoryItemService;
    private final Map<String, Item> genshinItems;

    public InvCommand(
            GenshinUserInventoryItemService inventoryItemService,
            @Qualifier("genshinItems") List<Item> genshinItems
    ) {
        this.inventoryItemService = inventoryItemService;
        this.genshinItems = new HashMap<>();
        for (var item : genshinItems) {
            this.genshinItems.put(item.getId(), item);
        }
    }

    @Override
    public String command() {
        return "/inv";
    }

    @Override
    public String getDescription() {
        return "инвентарь (Genshin)";
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {
        long chatId = ctx.chatId();
        long userId = ctx.user().getId();

        var items = inventoryItemService.findByChatIdAndUserId(chatId, userId);
        if (items.size() == 0) {
            ctx.replyToMessage("Ваш инвентарь пуст! Используйте /wish чтобы начать его наполнять!");
            return;
        }

        String text = "<b>Ваш инвентарь:</b>\n\n" +
                items.stream()
                        .map(this::formatItem)
                        .collect(Collectors.joining(", "));

        ctx.replyToMessage(text).callAsync(ctx.sender);


    }

    private String formatItem(GenshinUserInventoryItem invItem) {
        var item = genshinItems.get(invItem.getItemId());
        return "%d⭐ ️<b>%s</b> (%d)".formatted(item.getStars(), item.getName(), invItem.getAmount());
    }
}
