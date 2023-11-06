package fr.eris.eristrade.utils.inventory.eris;

import de.tr7zw.changeme.nbtapi.NBTItem;
import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.error.data.ErrorCode;
import fr.eris.eristrade.utils.error.data.ErrorType;
import fr.eris.eristrade.utils.error.exception.ErisPluginException;
import fr.eris.eristrade.utils.item.NBTUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

// Going to act as a super class
public abstract class ErisInventory implements Listener {

    @Getter private int inventoryRowAmount;
    @Getter private String inventoryName;

    @Getter public final Player owner;
    private final HashMap<Integer, ErisInventoryItem> inventoryContent = new HashMap<>();

    public Inventory inventory;
    @Getter private final InventoryStateData inventoryStateData;

    public ErisInventory(int inventoryRowAmount, String inventoryName, Player owner) {
        this.inventoryRowAmount = inventoryRowAmount;
        this.inventoryName = inventoryName;
        this.owner = owner;
        this.inventoryStateData = new InventoryStateData();
        inventory = Bukkit.createInventory(null, inventoryRowAmount * 9, ColorUtils.translate(inventoryName));
        Bukkit.getPluginManager().registerEvents(this, ErisTrade.getInstance());
    }

    public final void changeInventoryName(String newInventoryName) {
        this.inventoryName = newInventoryName;
        forceInventoryUpdate();
        onInventoryNameChange();
    }

    public final void changeInventorySize(int newInventoryRow) {
        this.inventoryRowAmount = newInventoryRow;
        try {
            inventoryChecker();
        } catch (ErisPluginException exception) {
            System.out.println("Try to change item map before changing the inventory size");
            exception.printStackTrace();
            return;
        }
        forceInventoryUpdate();
        onInventorySizeChange();
    }

    /**
        Use this to close and reopen the inventory of all the current viewers after updating the inventory (Name change)
     */
    public void forceInventoryUpdate() {
        owner.closeInventory();
        //inventory = null;
        openInventory();
    }

    public void openInventory() {
        if(onPreOpen()) return;
        inventoryContent.clear();
        update(inventoryContent);
        try {
            inventoryChecker();
        } catch (ErisPluginException exception) {
            exception.printStackTrace();
            closeInventory();
            return;
        }
        updateInventory();
        if(owner.getOpenInventory() == null) {
            forceOnlyOpen();
        }
        else {
            owner.updateInventory();
        }
    }

    public void forceOnlyOpen() {
        owner.openInventory(inventory);
        onOpen();
    }

    private void updateInventory() {
        if(inventory == null || (inventory.getSize() != inventoryRowAmount * 9 ||
                !ColorUtils.strip(ColorUtils.translate(inventoryName)).equals(ColorUtils.strip(inventory.getTitle())))) {
            inventory = Bukkit.createInventory(null, inventoryRowAmount * 9, ColorUtils.translate(inventoryName));
            if(owner.getOpenInventory() != null) {
                owner.closeInventory();
            }
        }
        inventory.clear();
        for(Integer itemSlot : inventoryContent.keySet()) {
            inventory.setItem(itemSlot, inventoryContent.get(itemSlot).getItem());
        }
    }

    public final void inventoryChecker() throws ErisPluginException {
        if(inventoryRowAmount > 6 || inventoryRowAmount < 1)
            throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.INVENTORY_INVALID_INVENTORY_SIZE, this.toString());

        int maxSlot = inventoryRowAmount * 9 - 1;
        for(Integer itemSlot : inventoryContent.keySet()) {
            if(itemSlot < 0 || itemSlot > maxSlot)
                throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.INVENTORY_INVALID_INVENTORY_ITEM_SLOT, this.toString());
        }
    }

    public final void closeInventory() {
        inventoryStateData.allowedToClose = true;
        owner.closeInventory();
    }

    public final void delete() {
        closeInventory();
        HandlerList.unregisterAll(this);
    }

    public String toString() {
        return "Name: " + inventoryName + " ; Row: " + inventoryRowAmount + " ; Content: " + inventoryContent;
    }

    @EventHandler
    public final void onInventoryClickEvent(InventoryClickEvent event) {
        if(this.inventory == null) return;
        if(!this.inventory.equals(event.getInventory())) return;
        onClick(event);
        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null || clickedItem.getType() == Material.AIR) return;
        ErisInventoryItem erisItem = findErisItemByItemStack(clickedItem);
        if(erisItem == null) return;
        if(erisItem.isCancelingClickEvent()) event.setCancelled(true);
        erisItem.callAction(event);
    }

    public ErisInventoryItem findErisItemByItemStack(ItemStack itemStack) {
        NBTItem nbtItem = NBTUtils.toNBT(itemStack);
        if(!nbtItem.hasTag("eris.utils.inventory.item.id")) return null;
        return findErisItemById(nbtItem.getUUID("eris.utils.inventory.item.id"));
    }

    public ErisInventoryItem findErisItemById(UUID itemId) {
        for(Integer slotId : inventoryContent.keySet()) {
            if(inventoryContent.get(slotId).isSameId(itemId)) return inventoryContent.get(slotId);
        }
        return null;
    }

    @EventHandler
    public final void onInventoryCloseEvent(InventoryCloseEvent event) {
        if(!inventoryStateData.allowedToClose) {
            owner.openInventory(inventory);
        }
        // Do not compact both if block because we don't want the onClose function get called if we already don't allow the close
        if(!onClose()) {
            owner.openInventory(inventory);
        }
    }

    @EventHandler
    public final void onPlayerQuitEvent(PlayerQuitEvent event) {
        if(!event.getPlayer().equals(owner)) return;
        closeInventory();
    }

    public abstract void update(HashMap<Integer, ErisInventoryItem> inventoryMap);
    public abstract boolean onClose();
    public abstract void onClick(InventoryClickEvent event);
    public abstract void onInventoryNameChange();
    public abstract void onInventorySizeChange();
    public abstract boolean onPreOpen();
    public abstract void onOpen();

    public class InventoryStateData {
        @Setter @Getter private boolean allowedToClose;
    }

}
