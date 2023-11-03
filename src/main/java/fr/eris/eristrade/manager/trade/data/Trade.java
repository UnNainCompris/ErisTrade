package fr.eris.eristrade.manager.trade.data;

import de.tr7zw.changeme.nbtapi.NBTItem;
import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.impl.ImplementationManager;
import fr.eris.eristrade.utils.BukkitTasks;
import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.interactiveasker.NumberAsker;
import fr.eris.eristrade.utils.inventory.CustomInventory;
import fr.eris.eristrade.utils.item.ItemBuilder;
import fr.eris.eristrade.utils.item.ItemCache;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Trade implements Listener {

    @Getter private final TradeData firstPlayer, secondPlayer;

    private boolean isTradeCanceled, isTradeFinished;
    private int tickSinceTradeAccept = 0;
    private int tradeTickCounter = 0;
    private BukkitTask tradeTask; // a task that schedule every tick from the start of the trade
    private final List<TradeItem> recentlyUpdatedItem;

    public Trade(Player firstPlayer, Player secondPlayer) {
        this.firstPlayer = new TradeData(firstPlayer, "&7Trade with: &6" + secondPlayer.getName());
        this.secondPlayer = new TradeData(secondPlayer, "&7Trade with: &6" + firstPlayer.getName());
        this.recentlyUpdatedItem = new ArrayList<>();
        tradeTask = BukkitTasks.syncTimer(this::tradeTask, 1, 1);
        Bukkit.getServer().getPluginManager().registerEvents(this, ErisTrade.getInstance());
        updateInventory();
    }

    public void tradeTask() {
        tradeTickCounter++;
        if(isTradeCanceled || isTradeFinished) {
            tradeTask.cancel();
            return;
        }
        for(TradeItem currentItem : new ArrayList<>(recentlyUpdatedItem)) {
            if(tradeTickCounter - currentItem.getLastEditTick() > 30) {
                recentlyUpdatedItem.remove(currentItem);
                currentItem.setDisplayHasEdited(false);
                updateInventory();
                continue;
            }
            if((tradeTickCounter - currentItem.getLastEditTick()) % 5 == 0) {
                currentItem.setDisplayHasEdited(!currentItem.isDisplayHasEdited());
                updateInventory();
            }
        }

        if(!firstPlayer.isAcceptTrade() || !secondPlayer.isAcceptTrade()) {
            if(tickSinceTradeAccept != 0) {
                updateSeparator(firstPlayer);
                firstPlayer.getTradeInventory().update(firstPlayer.getPlayer());
                updateSeparator(secondPlayer);
                secondPlayer.getTradeInventory().update(secondPlayer.getPlayer());
            }
            tickSinceTradeAccept = 0;
            return;
        }
        tickSinceTradeAccept++;
        if(tickSinceTradeAccept % 20 == 0) {
            updateSeparator(firstPlayer);
            firstPlayer.getTradeInventory().update(firstPlayer.getPlayer());
            updateSeparator(secondPlayer);
            secondPlayer.getTradeInventory().update(secondPlayer.getPlayer());
        }
        if(tickSinceTradeAccept == 100) {
            finishTrade();
        }
    }

    public void updateInventory() {
        updateFirstPlayerInventory();
        updateSecondPlayerInventory();
    }

    private void updateFirstPlayerInventory() {
        firstPlayer.getTradeInventory().clearItems();
        firstPlayer.getTradeInventory().clearToolbarsItems();
        updatePlayerInventory(firstPlayer, secondPlayer);
    }

    private void updateSecondPlayerInventory() {
        secondPlayer.getTradeInventory().clearItems();
        secondPlayer.getTradeInventory().clearToolbarsItems();
        updatePlayerInventory(secondPlayer, firstPlayer);
    }

    public void updateSeparator(TradeData traderPlayerData) {
        List<Integer> separatorSlotList = Arrays.asList(4, 13, 22, 31, 40);
        for(int slot : separatorSlotList) { // separator between the 2 trade
            final short itemColor;
            if(separatorSlotList.size() - 1 - separatorSlotList.indexOf(slot) < (100 - tickSinceTradeAccept) / 20) itemColor = ItemCache.ItemColor.GRAY; // TODO: 23/10/2023
            else itemColor = ItemCache.ItemColor.LIME;

            traderPlayerData.getTradeInventory().setItem(slot,
                    () -> ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, itemColor, false).build(), null);
        }
    }

    private void updatePlayerInventory(TradeData traderPlayerData, TradeData tradedPlayerData) {
        for(TradeItem tradeItem : traderPlayerData.getCurrentTradedItem()) {
            int rawItemSlot = traderPlayerData.getCurrentTradedItem().indexOf(tradeItem);
            int itemSlot = (int) (rawItemSlot % 4 + (Math.floor(rawItemSlot / 4f) * 9));
            traderPlayerData.getTradeInventory().setItem(itemSlot, tradeItem::buildForDisplay,
                    (event) -> {
                        traderPlayerData.removeItem(getAmountOfItemWithClickType(tradeItem, event.getClick()),
                                new NBTItem(event.getCurrentItem()).getUUID("eristrade.itemkey"), this);
                        updateInventory();
                    });
        }

        updateSeparator(traderPlayerData);

        for(TradeItem tradeItem : tradedPlayerData.getCurrentTradedItem()) {
            int rawItemSlot = tradedPlayerData.getCurrentTradedItem().indexOf(tradeItem);
            int itemSlot = (int) (5 + (rawItemSlot % 4 + (Math.floor(rawItemSlot / 4f) * 9)));
            traderPlayerData.getTradeInventory().setItem(itemSlot, tradeItem::buildForDisplay, null);
        }

        traderPlayerData.getTradeInventory().addToolbarItem(1, () -> {
            if(traderPlayerData.getCurrentTradedItem().isEmpty() && tradedPlayerData.getCurrentTradedItem().isEmpty()
               && traderPlayerData.getTradedMoney() == 0 && tradedPlayerData.getTradedMoney() == 0) {
                return ItemBuilder.placeHolders(Material.WOOL, ItemCache.ItemColor.GRAY, false)
                        .setDisplayName("&7You cannot trade nothing on both side !").build();
            }
            else if(traderPlayerData.isAcceptTrade())
                return ItemBuilder.placeHolders(Material.WOOL, ItemCache.ItemColor.LIME, false)
                    .setDisplayName("&cClick here to cancel the trade !").build();
            else return ItemBuilder.placeHolders(Material.WOOL, ItemCache.ItemColor.RED, false)
                    .setDisplayName("&aClick here to accept the trade !").build();
        }, (event) -> {
            traderPlayerData.setAcceptTrade(!traderPlayerData.isAcceptTrade());
            updateInventory();
        }, true);

        traderPlayerData.getTradeInventory().addToolbarItem(9, () -> {
            if(traderPlayerData.getCurrentTradedItem().isEmpty() && tradedPlayerData.getCurrentTradedItem().isEmpty()
                    && traderPlayerData.getTradedMoney() == 0 && tradedPlayerData.getTradedMoney() == 0) {
                return ItemBuilder.placeHolders(Material.WOOL, ItemCache.ItemColor.GRAY, false)
                        .setDisplayName("&7You cannot trade nothing on both side !").build();
            }
            else if(tradedPlayerData.isAcceptTrade())
                return ItemBuilder.placeHolders(Material.WOOL, ItemCache.ItemColor.LIME, false)
                        .setDisplayName("&a" + tradedPlayerData.getPlayer().getName() + " has accept the trade !").build();
            else return ItemBuilder.placeHolders(Material.WOOL, ItemCache.ItemColor.RED, false)
                    .setDisplayName("&c" + tradedPlayerData.getPlayer().getName() + " doesn't have accept the trade !").build();
        }, null, true);

        if(ImplementationManager.getEconomy() != null) {
            traderPlayerData.getTradeInventory().addToolbarItem(2,
                    () -> new ItemBuilder().setMaterial(Material.GOLD_NUGGET).setDisplayName("&6Money: &e" + traderPlayerData.getTradedMoney()).setLore("&8Click to edit the amount !").build(),
                    (event) -> {
                        traderPlayerData.setCanClose(true);
                        new NumberAsker(traderPlayerData.getPlayer(), "Input the money you want to trade",
                                (number) -> {
                                    if (ImplementationManager.getEconomy().getBalance(traderPlayerData.getPlayer()) >= number.longValue())
                                        traderPlayerData.setTradedMoney(number.longValue());
                                    else traderPlayerData.getPlayer().sendMessage("&7You don't have enough money !");
                                });
                    }, true);

            traderPlayerData.getTradeInventory().addToolbarItem(8,
                    () -> new ItemBuilder().setMaterial(Material.GOLD_NUGGET).setDisplayName("&6Money: &e" + tradedPlayerData.getTradedMoney()).build(),
                    null, true);
        }

        traderPlayerData.getTradeInventory().update(traderPlayerData.getPlayer());
    }

    private int getAmountOfItemWithClickType(TradeItem targetTradeItem, ClickType clickType) {
        int amountToRemove = 0;
        if(clickType == ClickType.SHIFT_LEFT) amountToRemove = targetTradeItem.getAmount();
        else if(clickType == ClickType.SHIFT_RIGHT) amountToRemove = targetTradeItem.getAmount() / 2;
        else if(clickType == ClickType.RIGHT) amountToRemove = Math.min(targetTradeItem.getItem().getMaxStackSize() / 2, targetTradeItem.getAmount());
        else if(clickType == ClickType.LEFT) amountToRemove = Math.min(1, targetTradeItem.getAmount());
        else if(clickType == ClickType.MIDDLE) amountToRemove = Math.min(targetTradeItem.getItem().getMaxStackSize(), targetTradeItem.getAmount());
        return amountToRemove;
    }

    private TradeItem getItemAsTradeItemFromInventory(ItemStack item, Inventory inventory) {
        int amountFound = 0;
        for(ItemStack currentItem : inventory.getContents()) {
            if(item.isSimilar(currentItem)) amountFound += currentItem.getAmount();
        }
        if(amountFound == 0) return null;
        return new TradeItem(item, amountFound);
    }

    private boolean hasEnoughTradeItem(TradeItem tradeItem, Player player) {
        int amountFound = 0;
        if(tradeItem == null) return false;
        for(ItemStack currentItem : player.getInventory().getContents()) {
            if(tradeItem.getItem().isSimilar(currentItem)) amountFound += currentItem.getAmount();
        }
        return amountFound >= tradeItem.getAmount();
    }

    private boolean removeItemFromTradeItemInPlayerInventory(TradeItem tradeItem, Player player) {
        if(!hasEnoughTradeItem(tradeItem, player)) return false;
        int amountToRemove = tradeItem.getAmount();
        for(int currentSlot = 0 ;  currentSlot < player.getInventory().getSize() ; currentSlot++) {
            ItemStack currentItem = player.getInventory().getItem(currentSlot);
            if(tradeItem.getItem().isSimilar(currentItem)) {
                int itemAmount = currentItem.getAmount();
                if(itemAmount > amountToRemove) currentItem.setAmount(itemAmount - amountToRemove);
                else currentItem.setType(Material.AIR);
                amountToRemove -= Math.min(amountToRemove, itemAmount);
                player.getInventory().setItem(currentSlot, currentItem);
                if(amountToRemove <= 0) break;
            }
        }
        return amountToRemove <= 0;
    }

    public TradeData getDataFromPlayer(Player player) {
        if(player.equals(firstPlayer.getPlayer())) return firstPlayer;
        if(player.equals(secondPlayer.getPlayer())) return secondPlayer;
        return null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) return;
        if(event.getClickedInventory().getType() != InventoryType.PLAYER) return;
        if(!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        TradeData playerData = getDataFromPlayer(player);
        if(playerData == null) return;
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if(item == null) return;
        TradeItem tradeItemFromPlayer = getItemAsTradeItemFromInventory(item, player.getInventory());
        if(tradeItemFromPlayer == null) return;
        int amount = getAmountOfItemWithClickType(tradeItemFromPlayer, event.getClick());
        if(amount <= 0) return;
        tradeItemFromPlayer.setAmount(amount);
        if(playerData.getCurrentTradedItem().size() >= 20) return;
        if(removeItemFromTradeItemInPlayerInventory(tradeItemFromPlayer, player)) {
            if(playerData.addNewItem(item, tradeItemFromPlayer.getAmount(), this) != TradeData.AddItemError.NO_ERROR)
                return;
            firstPlayer.setAcceptTrade(false);
            secondPlayer.setAcceptTrade(false);
            updateInventory();
        }
    }

    public void putTradeItemHasEdited(TradeItem tradeItem) {
        tradeItem.setLastEditTick(tradeTickCounter);
        if(!recentlyUpdatedItem.contains(tradeItem))
            recentlyUpdatedItem.add(tradeItem);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler
    public void onPlayerProcessCommand(PlayerCommandPreprocessEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent event) {
        tradeCanceler(event.getPlayer());
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom(), to = event.getTo();
        if(from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;
        tradeCanceler(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        tradeCanceler(event.getEntity());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        tradeCanceler(event.getEntity());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        tradeCanceler(event.getPlayer());
    }

    public void tradeCanceler(Entity entity) {
        if(!(entity instanceof Player)) return;
        Player player = (Player) entity;
        if(!player.equals(firstPlayer.getPlayer()) && !player.equals(secondPlayer.getPlayer())) return;
        cancelTrade();
    }

    public void finishTrade() {
        if(isTradeCanceled || isTradeFinished) return;
        isTradeFinished = true;
        destroy();
        addTradeItemAndDropOverflow(secondPlayer.getCurrentTradedItem(), firstPlayer.getPlayer());
        addTradeItemAndDropOverflow(firstPlayer.getCurrentTradedItem(), secondPlayer.getPlayer());
    }

    public void cancelTrade() {
        if(isTradeCanceled || isTradeFinished) return;
        isTradeCanceled = true;
        destroy();
        addTradeItemAndDropOverflow(firstPlayer.getCurrentTradedItem(), firstPlayer.getPlayer());
        addTradeItemAndDropOverflow(secondPlayer.getCurrentTradedItem(), secondPlayer.getPlayer());
    }

    public void destroy() {
        firstPlayer.getTradeInventory().destroy();
        secondPlayer.getTradeInventory().destroy();
        tradeTask.cancel();
        ErisTrade.getTradeManager().removeTrade(this);
        HandlerList.unregisterAll(this);
    }

    public void addTradeItemAndDropOverflow(List<TradeItem> toGive, Player target) {
        for(TradeItem currentTradeItem : toGive) {
            for(int amountToGive = 0 ; currentTradeItem.getAmount() != 0 ; amountToGive = Math.min(currentTradeItem.getItem().getMaxStackSize(), currentTradeItem.getAmount())) {
                ItemStack currentItemStackToGive = currentTradeItem.retrieveItem(amountToGive);
                if(target.getInventory().firstEmpty() != -1)
                    target.getInventory().addItem(currentItemStackToGive);
                else target.getLocation().getWorld().dropItem(target.getLocation().clone().add(0, 1, 0), currentItemStackToGive);
            }
        }
    }
}
