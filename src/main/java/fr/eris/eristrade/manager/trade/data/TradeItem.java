package fr.eris.eristrade.manager.trade.data;

import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.ItemUtils;
import fr.eris.eristrade.utils.item.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class TradeItem {
    @Getter private final ItemStack item;
    @Getter @Setter private int amount;
    @Getter private final UUID itemKey;

    protected TradeItem(ItemStack item, int amount) {
        this.item = new ItemBuilder(item).setAmount(1).build();
        this.amount = amount;
        this.itemKey = UUID.randomUUID();
    }

    public ItemStack buildForDisplay() {
        String itemDisplayName = item.getItemMeta().getDisplayName();
        if(itemDisplayName == null) itemDisplayName = ItemUtils.getItemName(item);
        return new ItemBuilder(item).setNbtValue("eristrade.itemkey", itemKey).setDisplayName(itemDisplayName
                + ColorUtils.translate(" &7(&8" + amount + "&7)")).setAmount(1).build();
    }

    public ItemStack retrieveItem(int amount) {
        if(this.amount < amount) throw new RuntimeException("You can't get more item than there is in the trade");
        this.amount -= amount;
        return new ItemBuilder(item).setAmount(amount).build();
    }
}
