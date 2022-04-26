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
import java.util.stream.Stream;

// @Component
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
            ctx.replyToMessage("Ваш инвентарь пуст! Используйте /wish чтобы начать его наполнять!").callAsync(ctx.sender);
            return;
        }

        var itemsByStars = items.stream()
                .map(i -> new InventoryItem(genshinItems.get(i.getItemId()), i))
                .collect(Collectors.groupingBy(i -> i.item.getStars()));

        var text = new StringBuilder("<b>Ваш инвентарь:</b>\n\n");
        for (int i = 5; i > 2; i--) {
            if (!itemsByStars.containsKey(i))
                continue;
            text
                    .append(getStarsEmoji(i))
                    .append(":\n")
                    .append(formatStarSection(itemsByStars.get(i)))
                    .append("\n\n");
        }

        ctx.replyToMessage(text.toString()).callAsync(ctx.sender);


    }

    /* format every star section to make it look like:
    👤: p1, p2, p3
    ⚔️: w1, w2, w3
    this method accepts list of inventory items of the same rate
     */
    private String formatStarSection(List<InventoryItem> items) {
        var itemsByType = items.stream()
                .collect(Collectors.groupingBy(i -> i.item.getType()));
        var result = new StringBuilder();
        if (itemsByType.containsKey(Item.Type.CHARACTER)) {
            result
                    .append("👤")
                    .append(formatInventoryItemLine(itemsByType.get(Item.Type.CHARACTER)))
                    .append("\n");
        }
        if (itemsByType.containsKey(Item.Type.WEAPON)) {
            result
                    .append("⚔️")
                    .append(formatInventoryItemLine(itemsByType.get(Item.Type.WEAPON)))
                    .append("\n");
        }

        // remove trailing \n
        return result.deleteCharAt(result.length() - 1).toString();

    }

    // just join elements of the given list to the single line, separated by comma
    private String formatInventoryItemLine(List<InventoryItem> items) {
        return items
                .stream()
                .map(InventoryItem::toString)
                .collect(Collectors.joining(", "));
    }

    private String getStarsEmoji(int amount) {
        return Stream.generate(() -> "⭐️").limit(amount).collect(Collectors.joining());
    }

    private record InventoryItem(Item item, GenshinUserInventoryItem dbItem) {

        @Override
        public String toString() {
            return "%s x%d".formatted(item.getName(), dbItem.getAmount());
        }
    }

}
