package fr.eris.eristrade.manager.trade.inventory;

import fr.eris.eristrade.manager.trade.data.TradeData;
import fr.eris.erisutils.utils.inventory.eris.ErisInventory;
import fr.eris.erisutils.utils.inventory.eris.ErisInventoryHolder;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class TradeInventoryHolder extends ErisInventoryHolder {

    @Getter private final TradeData playerTradeData;

    public TradeInventoryHolder(TradeData playerTradeData) {
        this.playerTradeData = playerTradeData;
    }
}
