package fr.eris.eristrade.manager.trade.inventory;

import fr.eris.eristrade.manager.trade.data.TradeData;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class TradeInventoryHolder implements InventoryHolder {

    @Getter private final TradeData playerTradeData;

    public TradeInventoryHolder(TradeData playerTradeData) {
        this.playerTradeData = playerTradeData;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
