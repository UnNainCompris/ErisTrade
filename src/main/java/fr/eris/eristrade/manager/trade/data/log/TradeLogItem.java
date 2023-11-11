package fr.eris.eristrade.manager.trade.data.log;

import com.google.gson.annotations.Expose;
import fr.eris.eristrade.manager.trade.data.TradeItem;
import fr.eris.erisutils.utils.item.ItemBuilder;
import fr.eris.erisutils.utils.item.ItemUtils;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class TradeLogItem {
    @Getter @Expose private Map<String, Object> itemData;
    @Getter @Expose private int tradedAmount;

    public static TradeLogItem buildFromTradeItem(TradeItem tradeItem) {
        TradeLogItem tradeLogItem = new TradeLogItem();
        tradeLogItem.tradedAmount = tradeItem.getAmount();
        tradeLogItem.itemData = tradeItem.getItem().serialize();
        return tradeLogItem;
    }

    public ItemStack toDisplayItem() {
        ItemStack item = ItemStack.deserialize(itemData);
        String itemDisplayName = item.getItemMeta().getDisplayName();
        if(itemDisplayName == null) itemDisplayName = ItemUtils.getItemName(item);
        return new ItemBuilder(item).setDisplayName(itemDisplayName + " &7(&8" + tradedAmount + "&7)").build();
    }
}
