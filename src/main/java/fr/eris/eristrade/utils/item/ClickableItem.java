package fr.eris.eristrade.utils.item;

import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ClickableItem {

    @Getter private Item itemInterface;
    @Getter private ActionOnClick actionInterface;

    public ClickableItem(Item itemInterface, ActionOnClick actionInterface) {
        this.itemInterface = itemInterface;
        this.actionInterface = actionInterface;
    }

    public ClickableItem(Item itemInterface) {
        this(itemInterface, null);
    }

    public void changeAction(ActionOnClick newAction) {
        this.actionInterface = newAction;
    }

    public void changeItem(Item newItem) {
        this.itemInterface = newItem;
    }

    public void executeAction(InventoryClickEvent clickEvent) {
        if(actionInterface == null) return;
        actionInterface.onClickEvent(clickEvent);
    }

    public ItemStack getItem() {
        return itemInterface.getItem();
    }

    public interface ActionOnClick {
        void onClickEvent(InventoryClickEvent event);
    }

    public interface Item {
        ItemStack getItem();
    }
}
