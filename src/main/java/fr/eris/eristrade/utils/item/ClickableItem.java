package fr.eris.eristrade.utils.item;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ClickableItem {

    private Item item;
    private ActionOnClick action;

    public ClickableItem(Item item, ActionOnClick action) {
        this.item = item;
        this.action = action;
    }

    public ClickableItem(Item item) {
        this(item, null);
    }

    public void changeAction(ActionOnClick newAction) {
        this.action = newAction;
    }

    public void changeItem(Item newItem) {
        this.item = newItem;
    }

    public void executeAction(InventoryClickEvent clickEvent) {
        if(action == null) return;
        action.onClickEvent(clickEvent);
    }

    public ItemStack getItem() {
        return item.getItem();
    }

    public interface ActionOnClick {
        void onClickEvent(InventoryClickEvent event);
    }

    public interface Item {
        ItemStack getItem();
    }
}
