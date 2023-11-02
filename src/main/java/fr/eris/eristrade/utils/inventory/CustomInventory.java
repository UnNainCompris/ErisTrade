package fr.eris.eristrade.utils.inventory;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.utils.BukkitTasks;
import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.InventoryUtils;
import fr.eris.eristrade.utils.item.ClickableItem;
import fr.eris.eristrade.utils.item.ItemCache;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;

public class CustomInventory implements Listener {

    private final HashMap<Byte, ClickableItem> items;
    //                    slot      item
    private final HashMap<Byte, ClickableItem> toolbarItems;
    private final List<UUID> currentViewers;
    @Getter private Inventory inventory;

    private Consumer<CustomInventory> closeAction;
    private BukkitTask deleteTask;

    private byte inventorySize; // 9, 18, 27, 36, 45, 54
    private String inventoryName;

    public CustomInventory() {
        items = new HashMap<>();
        currentViewers = new ArrayList<>();
        toolbarItems = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, ErisTrade.getInstance());
    }

    public void clearItems() {
        items.clear();
    }

    public void clearToolbarsItems() {
        toolbarItems.clear();
    }

    public CustomInventory setItem(int slot, ClickableItem.Item item, ClickableItem.ActionOnClick actionOnClick) {
        ClickableItem clickableItem = new ClickableItem(item, actionOnClick);
        items.put((byte) slot, clickableItem);
        return this;
    }

    public CustomInventory setCloseAction(Consumer<CustomInventory> closeAction) {
        this.closeAction = closeAction;
        return this;
    }

    public CustomInventory setInventorySize(int inventoryRow) {
        this.inventorySize = InventoryUtils.getSizeFromInt(inventoryRow * 9);
        return this;
    }

    public CustomInventory setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
        return this;
    }

    private boolean loadInventory() {
        byte inventorySize = this.inventorySize;
        boolean hasInventoryChanged;
        if(inventory == null || inventory.getSize() != inventorySize || !ColorUtils.translate(inventoryName).equals(inventory.getName())) {
            inventory = Bukkit.createInventory(null, inventorySize, ColorUtils.translate(inventoryName));
            hasInventoryChanged = true;
        }
        else {
            inventory.clear();
            hasInventoryChanged = false;
        }

        if(items != null && !this.items.isEmpty())
            for(byte key : items.keySet()) {
                inventory.setItem(key, items.get(key).getItem());
            }
        setToolbar(inventory);
        return hasInventoryChanged;
    }

    private void setToolbar(Inventory inventory) {
        InventoryUtils.setSideInventory(inventory, new ItemStack[]{ItemCache.placeHolder}, new InventoryUtils.Side[]{InventoryUtils.Side.DOWN});

        for(byte slot : toolbarItems.keySet()) {
            inventory.setItem(slot - 1 + inventory.getSize() - 9, toolbarItems.get(slot).getItem());
        }
    }

    public void destroy() {
        new ArrayList<>(currentViewers).forEach(uuid -> Bukkit.getPlayer(uuid).closeInventory());
        HandlerList.unregisterAll(this);
    }

    public void update(Player... newViewers) {
        final List<Player> playerWithOpenedInventory = new ArrayList<>();
        for(UUID playerUUID : currentViewers) {
            final Player player = Bukkit.getPlayer(playerUUID);
            playerWithOpenedInventory.add(player);
        }
        boolean hasInventoryChanged = loadInventory();
        for(Player player : playerWithOpenedInventory) {
            if(hasInventoryChanged)
                player.openInventory(inventory);
            else player.updateInventory();
        }
        if(newViewers != null) {
            List<Player> newViewersCopy = new ArrayList<>(Arrays.asList(newViewers));
            for(Player currentViewers : playerWithOpenedInventory) {
                newViewersCopy.remove(currentViewers);
            }

            for (Player newViewer : newViewersCopy) {
                newViewer.openInventory(inventory);
            }
        }
    }

    /**
     * @param slot the slot between 1 and 9 where we want to place the new item
     * @param item the item to place in the toolbar
     * @param actionOnClick the action to execute when click on the item
     * @param force know if we don't care about everything else
     * @return the current instance
     */
    public CustomInventory addToolbarItem(int slot, ClickableItem.Item item, ClickableItem.ActionOnClick actionOnClick, boolean force) {
        ClickableItem clickableItem = new ClickableItem(item, actionOnClick);
        if(slot > 9 || slot < 1) return this;
        if((this.toolbarItems.containsKey((byte)slot) || this.toolbarItems.containsValue(clickableItem)) && !force) return this;
        this.toolbarItems.put((byte) slot, clickableItem);
        this.update();
        return this;
    }

    /**
     * Use to know if the {@code inventory} is in the list
     * @param inventory The inventory we want to check
     * @return false if the inventory are not in the inventory list other is the index of the find inventory
     */
    public boolean isValidInventory(Inventory inventory) {
        if(inventory == null || this.inventory == null) return false;
        return this.inventory.equals(inventory);
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        if(!isValidInventory(event.getInventory())) return;
        if(event.getCurrentItem() == null) return;
        event.setCancelled(true);
        final ItemStack clickedItem = event.getCurrentItem();

        if(isToolbarSlot((byte) event.getSlot(), event.getInventory())) {
            for (final ClickableItem item : this.toolbarItems.values()) {
                if(item.getItem().equals(event.getCurrentItem())) {
                    item.executeAction(event);
                    break;
                }
            }
        } else {
            if(this.items != null && !this.items.isEmpty()) {
                for (final ClickableItem item : this.items.values()) {
                    if (item.getItem().equals(clickedItem)) {
                        item.executeAction(event);
                        break;
                    }
                }
            }
        }
    }

    public boolean isToolbarSlot(byte slot, Inventory currentInventory) {
        return slot + 1 >= currentInventory.getSize() - 8;
    }

    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {
        if(!isValidInventory(event.getInventory())) return;
        currentViewers.remove(event.getPlayer().getUniqueId());
        if(closeAction != null) closeAction.accept(this);
        if(currentViewers.isEmpty()) {
            this.deleteTask = BukkitTasks.asyncLater(this::destroy, 20 * 300);
        }
    }

    @EventHandler
    public void inventoryOpenEvent(InventoryOpenEvent event) {
        if(!isValidInventory(event.getInventory())) return;
        if(currentViewers.contains(event.getPlayer().getUniqueId())) return;

        currentViewers.add(event.getPlayer().getUniqueId());
        if(deleteTask != null) {
            deleteTask.cancel();
            deleteTask = null;
        }
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        if(!isValidInventory(event.getPlayer().getOpenInventory().getTopInventory())) return;
        currentViewers.remove(event.getPlayer().getUniqueId());
        if(currentViewers.isEmpty()) {
            this.deleteTask = BukkitTasks.asyncLater(this::destroy, 20 * 300);
        }
    }

}
