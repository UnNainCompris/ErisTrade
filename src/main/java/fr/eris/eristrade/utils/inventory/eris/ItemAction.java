package fr.eris.eristrade.utils.inventory.eris;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface ItemAction {
    void onItemClick(InventoryClickEvent event);
}
