package fr.eris.eristrade.manager.trade.inventory.log;

import fr.eris.erisutils.utils.inventory.eris.ErisInventory;
import fr.eris.erisutils.utils.inventory.eris.ErisInventoryItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;

public class TradeLogInventory extends ErisInventory {
    public TradeLogInventory(int inventoryRowAmount, String inventoryName, Player owner) {
        super(inventoryRowAmount, inventoryName, owner);
    }

    @Override
    public void setContent() {

    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public void onOpen() {

    }
}
