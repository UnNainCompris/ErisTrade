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
    public void update(HashMap<Integer, ErisInventoryItem> hashMap) {

    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public void onClick(InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public void onInventoryNameChange() {

    }

    @Override
    public void onInventorySizeChange() {

    }

    @Override
    public boolean onPreOpen() {
        return false;
    }

    @Override
    public void onOpen() {

    }
}
