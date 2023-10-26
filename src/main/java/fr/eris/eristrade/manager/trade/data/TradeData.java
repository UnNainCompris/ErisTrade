package fr.eris.eristrade.manager.trade.data;

import fr.eris.eristrade.utils.inventory.CustomInventory;
import fr.eris.eristrade.utils.item.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeData {
    @Getter private final Player player;
    @Getter private final CustomInventory tradeInventory;
    @Getter private final List<TradeItem> currentTradedItem;
    @Getter @Setter
    private long tradedMoney;
    @Getter @Setter private boolean canClose;
    @Getter @Setter private boolean acceptTrade;

    protected TradeData(Player player, String inventoryName) {
        this.player = player;
        this.tradeInventory = new CustomInventory().setInventoryName(inventoryName).setInventorySize(54);
        this.currentTradedItem = new ArrayList<>();
    }

    public AddItemError addNewItem(ItemStack item, int amount) {
        TradeItem tradeItem = findTradeItemByItem(item);
        if(tradeItem != null) {
            tradeItem.setAmount(tradeItem.getAmount() + amount);
            return AddItemError.NO_ERROR;
        }
        if(currentTradedItem.size() > 20) return AddItemError.TOO_MANY_ITEM;
        tradeItem = new TradeItem(item, amount);
        currentTradedItem.add(tradeItem);
        acceptTrade = false;
        return AddItemError.NO_ERROR;
    }

    public RemoveItemError removeItem(int amount, UUID itemKey) {
        if(amount <= 0) return RemoveItemError.INVALID_AMOUNT;
        TradeItem targetItem = findTradeItemByKey(itemKey);
        if(targetItem == null) return RemoveItemError.ITEM_NOT_FOUND;
        if(!checkPlayerSpace(targetItem.getItem(), amount)) return RemoveItemError.NOT_ENOUGH_PLAYER_SPACE;

        ItemBuilder itemToGive = new ItemBuilder(targetItem.getItem());
        for(int remainingItemToGive = amount ; remainingItemToGive > 0 ; remainingItemToGive -= Math.min(targetItem.getItem().getMaxStackSize(), remainingItemToGive)) {
            itemToGive.setAmount(Math.min(targetItem.getItem().getMaxStackSize(), remainingItemToGive));
            if(player.getInventory().firstEmpty() != -1)
                player.getInventory().addItem(itemToGive.build());
            else player.getLocation().getWorld().dropItem(player.getLocation().clone().add(0, 1, 0), itemToGive.build());
        }
        acceptTrade = false;
        return RemoveItemError.NO_ERROR;
    }

    public boolean checkPlayerSpace(ItemStack item, int requireItemSpace) {
        int currentFoundSpace = 0;
        for(ItemStack currentItem : player.getInventory().getContents()) {
            if(currentItem.isSimilar(item) && currentItem.getAmount() < currentItem.getMaxStackSize()) {
                currentFoundSpace += currentItem.getMaxStackSize() - currentItem.getAmount();
                if(currentFoundSpace >= requireItemSpace) return true; // avoid useless loop
            }
        }
        return false;
    }

    public TradeItem findTradeItemByKey(UUID itemKey) {
        for(TradeItem tradeItem : currentTradedItem) {
            if(tradeItem.getItemKey().equals(itemKey)) return tradeItem;
        }
        return null;
    }

    public TradeItem findTradeItemByItem(ItemStack targetItem) {
        for(TradeItem tradeItem : currentTradedItem) {
            if(tradeItem.getItem().isSimilar(targetItem)) return tradeItem;
        }
        return null;
    }

    private enum RemoveItemError {
        ITEM_NOT_FOUND,
        INVALID_AMOUNT,
        NOT_ENOUGH_PLAYER_SPACE,
        NO_ERROR;
    }

    private enum AddItemError {
        TOO_MANY_ITEM,
        NO_ERROR;
    }
}
