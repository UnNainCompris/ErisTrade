package fr.eris.eristrade.utils.interactiveasker;

import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.inventory.CustomInventory;
import fr.eris.eristrade.utils.inventory.ExtendInventory;
import fr.eris.eristrade.utils.item.ClickableItem;
import fr.eris.eristrade.utils.manager.handler.Handler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InteractiveAsker<T> extends Handler {

    protected final Player player;
    protected CustomInventory askInventory;
    protected ExtendInventory askExtendInventory;

    protected final List<ClickableItem> clickableItems;
    protected final Consumer<T> action;
    protected T value;
    protected boolean finished, useInventory, canClose = false;

    public InteractiveAsker(Player player, String askReason, T defaultValue, String inventoryName, int inventorySize, Consumer<T> action, boolean useInventory, boolean useExtendInventory) {
        this.player = player;
        this.clickableItems = new ArrayList<>();
        this.action = action;
        this.value = defaultValue;
        register();
        this.useInventory = useInventory;
        if (useInventory) {
            this.askInventory = new CustomInventory().setInventorySize(inventorySize).setInventoryName(inventoryName);
            initInventory();
            this.askInventory.update(player);
        } else if (useExtendInventory) {
            this.askExtendInventory = new ExtendInventory();
            initInventory();
            this.askExtendInventory.update(player);
        } else {
            this.canClose = true;
            player.closeInventory();
            player.sendMessage(ColorUtils.translate(askReason));
        }
    }

    public InteractiveAsker(Player player, String askReason, T defaultValue, String inventoryName, Consumer<T> action, boolean useInventory) {
        this(player, askReason, defaultValue, inventoryName, 54, action, useInventory, false);
    }

    protected void end() {
        finished = true;
        this.action.accept(this.value);
        this.askInventory = null;
        HandlerList.unregisterAll(this);
    }

    protected void initInventory() {}

    protected void setItem(ClickableItem item, int slot) {
        clickableItems.add(item);
        askInventory.setItem(slot, item.getItemInterface(), item.getActionInterface());
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        if(!useInventory) return;
        if(!event.getInventory().equals(askInventory)) return;
        if(!event.getWhoClicked().equals(this.player) || finished) return;
        if(event.getCurrentItem() == null) return;
        event.setCancelled(true);

        final ItemStack clickedItem = event.getCurrentItem();
        for (final ClickableItem item : this.clickableItems) {
            if (item.getItem().equals(clickedItem)) {
                item.executeAction(event);
                break;
            }
        }
    }

    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {
        if(!useInventory) return;
        if(!event.getInventory().equals(askInventory)) return;
        if(!event.getPlayer().equals(this.player) || finished) return;
        if(canClose) {canClose = false; return;}
        action.accept(value);
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        if(!event.getPlayer().equals(this.player) || finished) return;
        action.accept(value);
    }
}
