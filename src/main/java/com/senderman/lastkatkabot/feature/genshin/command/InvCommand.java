package com.senderman.lastkatkabot.feature.genshin.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.genshin.model.GenshinUserInventoryItem;
import com.senderman.lastkatkabot.feature.genshin.model.Item;
import com.senderman.lastkatkabot.feature.genshin.service.GenshinUserInventoryItemService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command
public class InvCommand implements CommandExecutor {

    private final GenshinUserInventoryItemService inventoryItemService;
    private final Map<String, Item> genshinItems;

    public InvCommand(
            GenshinUserInventoryItemService inventoryItemService,
            @Named("genshinItems") List<Item> genshinItems
    ) {
        this.inventoryItemService = inventoryItemService;
        this.genshinItems = new HashMap<>();
        for (var item : genshinItems) {
            this.genshinItems.put(item.id(), item);
        }
    }

    @Override
    public String command() {
        return "/inv";
    }

    @Override
    public String getDescription() {
        return "genshin.inv.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        if (ctx.message().isUserMessage()) {
            ctx.replyToMessage(ctx.getString("common.noUsageInPM")).callAsync(ctx.sender);
            return;
        }

        long chatId = ctx.chatId();
        long userId = ctx.user().getId();

        var items = inventoryItemService.findByChatIdAndUserId(chatId, userId);
        if (items.size() == 0) {
            ctx.replyToMessage(ctx.getString("genshin.inv.emptyInv")).callAsync(ctx.sender);
            return;
        }

        var itemsByStars = items.stream()
                .map(i -> new InventoryItem(genshinItems.get(i.getItemId()), i))
                .collect(Collectors.groupingBy(i -> i.item.stars()));

        var text = new StringBuilder("%s\n\n".formatted(ctx.getString("genshin.inv.yourInv")));
        for (int i = 5; i > 2; i--) {
            if (!itemsByStars.containsKey(i))
                continue;
            text
                    .append(getStarsEmoji(i))
                    .append(":\n")
                    .append(formatStarSection(itemsByStars.get(i)))
                    .append("\n\n");
        }

        ctx.replyToMessage(text.toString())
                .setSingleRowInlineKeyboard(ButtonBuilder
                        .callbackButton()
                        .text(ctx.getString("common.close"))
                        .payload(CloseInvCallback.NAME, userId)
                        .create())
                .callAsync(ctx.sender);
    }

    /* format every star section to make it look like:
    👤: p1, p2, p3
    ⚔️: w1, w2, w3
    this method accepts list of inventory items of the same rate
     */
    private String formatStarSection(List<InventoryItem> items) {
        var itemsByType = items.stream()
                .collect(Collectors.groupingBy(i -> i.item.type()));
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
                .sorted()
                .map(InventoryItem::toString)
                .collect(Collectors.joining(", "));
    }

    private String getStarsEmoji(int amount) {
        return Stream.generate(() -> "⭐️").limit(amount).collect(Collectors.joining());
    }

    private record InventoryItem(Item item, GenshinUserInventoryItem dbItem) implements Comparable<InventoryItem> {

        @Override
        public String toString() {
            return "%s x%d".formatted(item.name(), dbItem.getAmount());
        }

        @Override
        public int compareTo(@NotNull InvCommand.InventoryItem inventoryItem) {
            return Integer.compare(inventoryItem.dbItem.getAmount(), this.dbItem.getAmount());
        }
    }

}
