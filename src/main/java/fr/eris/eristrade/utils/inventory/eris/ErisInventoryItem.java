package fr.eris.eristrade.utils.inventory.eris;

import fr.eris.eristrade.utils.error.data.ErrorCode;
import fr.eris.eristrade.utils.error.data.ErrorType;
import fr.eris.eristrade.utils.error.exception.ErisPluginException;
import fr.eris.eristrade.utils.item.ItemBuilder;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ErisInventoryItem {
    @Getter private final ItemAction itemAction;
    private final ItemGetter itemGetter;
    private final UUID itemId; // use to identify the item (to avoid same item but not the action on click)
    @Getter private boolean cancelingClickEvent;

    public ErisInventoryItem setCancelingClickEvent(boolean isCancelingClickEvent) {
        this.cancelingClickEvent = isCancelingClickEvent;
        return this;
    }

    public ItemStack getItem() {
        return new ItemBuilder(itemGetter.getItem()).setNbtValue("eris.utils.inventory.item.id", itemId).build();
    }

    private ErisInventoryItem(ItemGetter itemGetter, ItemAction itemAction) {
        this.itemGetter = itemGetter;
        this.itemAction = itemAction;
        itemId = UUID.randomUUID();
    }

    public static ErisInventoryItem create(ItemGetter itemGetter, ItemAction itemAction) throws ErisPluginException {
        if(itemGetter == null) {
            throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.INVENTORY_INVALID_ITEM, "itemGetter is null !");
        }
        return new ErisInventoryItem(itemGetter, itemAction);
    }

    public static ErisInventoryItem create(ItemGetter itemGetter) throws ErisPluginException {
        return create(itemGetter, null);
    }

    public boolean equals(Object other) {
        if(!(other instanceof ErisInventoryItem)) return false;
        return ((ErisInventoryItem) other).itemId.equals(this.itemId);
    }

    public boolean isSameId(UUID itemId) {
        return this.itemId.equals(itemId);
    }

    public void callAction(InventoryClickEvent event) {
        if(this.itemAction == null) return;
        this.itemAction.onItemClick(event);
    }
}


