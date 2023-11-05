package fr.eris.eristrade.utils.inventory;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.utils.BukkitTasks;
import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.StringUtils;
import fr.eris.eristrade.utils.item.ClickableItem;
import fr.eris.eristrade.utils.item.ItemCache;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ExtendInventory implements Listener {
    private final List<ClickableItem> items;
    //                    slot      item
    private final HashMap<Byte, ClickableItem> toolbarItems;
    private final List<UUID> currentViewers;
    private final List<Inventory> inventories;

    private final List<Runnable> onUpdateFunction;
    private BukkitTask deleteTask;

    private boolean dynamicSize;
    private byte inventorySize; // 9, 18, 27, 36, 45, 54
    private String inventoryName;

    public ExtendInventory() {
        items = new ArrayList<>();
        currentViewers = new ArrayList<>();
        inventories = new ArrayList<>();
        toolbarItems = new HashMap<>();
        onUpdateFunction = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, ErisTrade.getInstance());
    }

    public ExtendInventory addUpdateFunction(Runnable updateFunction) {
        onUpdateFunction.add(updateFunction);
        return this;
    }

    public ExtendInventory addItem(ClickableItem item) {
        items.add(item);
        return this;
    }

    public ExtendInventory removeItem(ClickableItem item) {
        items.remove(item);
        return this;
    }

    public ExtendInventory removeAll(List<ClickableItem> items) {
        this.items.removeAll(items);
        return this;
    }

    public ExtendInventory removeAll() {
        this.items.clear();
        return this;
    }

    public ExtendInventory setDynamicSize(boolean isDynamicSize) {
        this.dynamicSize = isDynamicSize;
        return this;
    }

    public ExtendInventory setInventorySize(int inventorySize) {
        this.inventorySize = InventoryUtils.getSizeFromInt(inventorySize);
        return this;
    }

    public ExtendInventory setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
        return this;
    }

    private void loadInventory() {
        List<ClickableItem> itemsClone = new ArrayList<>(items);
        inventories.clear();
        Inventory newInventory;
        byte maxPage = (byte) Math.max(Math.ceil(itemsClone.size() / 45f), 1);

        for(byte currentPage = 1; currentPage <= maxPage ; currentPage++) {
            byte inventorySize = (dynamicSize ? InventoryUtils.getSizeFromInt(itemsClone.size()) : this.inventorySize);
            newInventory = Bukkit.createInventory(null, inventorySize, ColorUtils.translate(StringUtils.fastReplace(
                    inventoryName, "%current_pages%->" + currentPage, "%max_page%->" + maxPage
            )));

            for(int index = 0 ; index < inventorySize - 9 ; index++) {
                if(itemsClone.isEmpty()) break;
                newInventory.setItem(index, itemsClone.get(0).getItem());
                itemsClone.remove(0);
            }
            setToolbar(newInventory, maxPage, currentPage);
            inventories.add(newInventory);
        }
    }

    private void setToolbar(Inventory inventory, byte maxPage, byte currentPage) {
        InventoryUtils.setSideInventory(inventory, new ItemStack[]{ItemCache.placeHolder}, new InventoryUtils.Side[]{InventoryUtils.Side.DOWN});
        // # # P # # # N # #
        if(currentPage != maxPage) inventory.setItem(inventory.getSize() - 3, ItemCache.nextPage);
        if(currentPage != 1) inventory.setItem(inventory.getSize() - 7, ItemCache.previousPage);

        for(byte slot : toolbarItems.keySet()) {
            inventory.setItem(slot, toolbarItems.get(slot).getItem());
        }
    }

    public ExtendInventory addItems(List<ClickableItem> newItems) {
        items.addAll(newItems);
        return this;
    }

    public void destroy() {
        currentViewers.forEach(uuid -> Bukkit.getPlayer(uuid).closeInventory());
        HandlerList.unregisterAll(this);
    }

    public void update(HumanEntity... newViewers) {
        final HashMap<Player, Integer> openedInventory = new HashMap<>();
        for(UUID playerUUID : currentViewers) {
            final Player player = Bukkit.getPlayer(playerUUID);
            final int currentInventory = isValidInventory(player.getOpenInventory().getTopInventory());
            openedInventory.put(player, currentInventory);
        }
        onUpdateFunction.forEach(Runnable::run);
        loadInventory();
        for(Player player : openedInventory.keySet()) {
            int indexInventory = openedInventory.get(player);
            while(indexInventory > (inventories.size() - 1)) indexInventory--;
            player.openInventory(inventories.get(indexInventory));
        }
        if(newViewers != null)
            for(HumanEntity newViewer : newViewers)
                newViewer.openInventory(this.inventories.get(0));
    }

    /**
     * @param slot the slot between 1 and 9 where we want to place the new item
     * @param item the item to place in the toolbar
     * @param force know if we don't care about everything else
     * @return the current instance
     */
    public ExtendInventory addToolbarItem(int slot, ClickableItem item, boolean force) {
        if(slot > 9 || slot < 1) return this;
        if((this.toolbarItems.containsKey(slot) || this.toolbarItems.containsValue(item)) && !force) return this;
        this.toolbarItems.put((byte) slot, item);

        for(Inventory inventory : this.inventories){
            if((inventory.getItem(slot).equals(ItemCache.nextPage) || inventory.getItem(slot).equals(ItemCache.previousPage)) && !force) return this;
            inventory.setItem((inventory.getSize() - 10 + slot), item.getItem());
        }
        return this;
    }

    /**
     * Use to know if the {@code inventory} is in the list
     * @param inventory The inventory we want to check
     * @return -1 if the inventory are not in the inventory list other is the index of the find inventory
     */
    public int isValidInventory(Inventory inventory) {
        if(inventory == null) return -1;
        for(Inventory currentInventory : inventories)
            if(currentInventory.equals(inventory)) return inventories.indexOf(currentInventory);

        return -1;
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        if(isValidInventory(event.getInventory()) == -1) return;
        if(event.getCurrentItem() == null) return;
        event.setCancelled(true);

        final ItemStack clickedItem = event.getCurrentItem();
        final int currentInventory = isValidInventory(event.getInventory());

        if(clickedItem.equals(ItemCache.nextPage)) {
            if(currentInventory >= (this.inventories.size() - 1)) return;
            event.getWhoClicked().openInventory(this.inventories.get(currentInventory + 1));
            return;
        }

        if(clickedItem.equals(ItemCache.previousPage)) {
            if(currentInventory <= 0) return;
            event.getWhoClicked().openInventory(this.inventories.get(currentInventory - 1));
            return;
        }
        if(isToolbarSlot((byte) event.getSlot(), event.getInventory())) {
            for (final ClickableItem item : this.toolbarItems.values()) {
                if(item.getItem().equals(event.getCurrentItem())) item.executeAction(event);
                break;
            }
        } else {
            for (final ClickableItem item : this.items) {
                if (item.getItem().equals(clickedItem)) {
                    item.executeAction(event);
                    break;
                }
            }
        }
    }

    public boolean isToolbarSlot(byte slot, Inventory currentInventory) {
        return slot + 1 >= currentInventory.getSize() - 8;
    }

    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {
        if(isValidInventory(event.getInventory()) == -1) return;
        currentViewers.remove(event.getPlayer().getUniqueId());
        if(currentViewers.isEmpty()) {
            this.deleteTask = BukkitTasks.asyncLater(this::destroy, 20 * 300);
        }
    }

    @EventHandler
    public void inventoryOpenEvent(InventoryOpenEvent event) {
        if(isValidInventory(event.getInventory()) == -1) return;
        if(currentViewers.contains(event.getPlayer().getUniqueId())) return;

        currentViewers.add(event.getPlayer().getUniqueId());
        if(deleteTask != null) {
            deleteTask.cancel();
            deleteTask = null;
        }
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        if(isValidInventory(event.getPlayer().getOpenInventory().getTopInventory()) == -1) return;
        currentViewers.remove(event.getPlayer().getUniqueId());
        if(currentViewers.isEmpty()) {
            this.deleteTask = BukkitTasks.asyncLater(this::destroy, 20 * 300);
        }
    }
}
