package fr.eris.eristrade.manager.trade.data;

import de.tr7zw.changeme.nbtapi.NBTItem;
import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.impl.ImplementationManager;
import fr.eris.eristrade.utils.BukkitTasks;
import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.item.ItemBuilder;
import fr.eris.eristrade.utils.item.ItemCache;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
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

import java.util.*;

public class Trade implements Listener {

    @Getter private final TradeData firstPlayer, secondPlayer;

    private boolean isTradeCanceled, isTradeFinished;
    @Getter private int tickSinceTradeAccept = 0;
    private int tradeTickCounter = 0;
    private BukkitTask tradeTask; // a task that schedule every tick from the start of the trade
    private final List<TradeItem> recentlyUpdatedItem;

    public Trade(Player firstPlayer, Player secondPlayer) {
        this.firstPlayer = new TradeData(firstPlayer, "&7Trade with: &6" + secondPlayer.getName(), this);
        this.secondPlayer = new TradeData(secondPlayer, "&7Trade with: &6" + firstPlayer.getName(), this);
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
                updateInventory();
            }
            tickSinceTradeAccept = 0;
            return;
        }
        tickSinceTradeAccept++;
        if(tickSinceTradeAccept % 20 == 0) {
            firstPlayer.getPlayer().playNote(firstPlayer.getPlayer().getLocation(),
                    Instrument.PIANO, Note.sharp(12 * (tickSinceTradeAccept / 20), Note.Tone.A));
            secondPlayer.getPlayer().playNote(secondPlayer.getPlayer().getLocation(),
                    Instrument.PIANO, Note.sharp(12 * (tickSinceTradeAccept / 20), Note.Tone.A));
            updateInventory();
        }
        if(tickSinceTradeAccept == 120) {
            if(!isPlayerHasEnoughSpace(firstPlayer.getCurrentTradedItem(), secondPlayer.getPlayer())) {
                firstPlayer.getPlayer().sendMessage(ColorUtils.translate("&c[x] &7" + secondPlayer.getPlayer().getName()
                        + " don't have enough inventory space !"));
                secondPlayer.getPlayer().sendMessage(ColorUtils.translate("&c[x] &7You don't have enough inventory space !"));
                cancelTrade();
                return;
            }

            if(!isPlayerHasEnoughSpace(secondPlayer.getCurrentTradedItem(), firstPlayer.getPlayer())) {
                firstPlayer.getPlayer().sendMessage(ColorUtils.translate("&c[x] &7You don't have enough inventory space !"));
                secondPlayer.getPlayer().sendMessage(ColorUtils.translate("&c[x] &7" + firstPlayer.getPlayer().getName()
                        + " don't have enough inventory space !"));
                cancelTrade();
                return;
            }

            finishTrade();
        }
    }

    public void updateInventory() {
        firstPlayer.getTradeInventory().openInventory();
        secondPlayer.getTradeInventory().openInventory();
    }

    public boolean isAnythingTraded() {
        return !(firstPlayer.getCurrentTradedItem().isEmpty() && secondPlayer.getCurrentTradedItem().isEmpty()
                && firstPlayer.getTradedMoney() == 0 && secondPlayer.getTradedMoney() == 0);
    }

    public static int getAmountOfItemWithClickType(TradeItem targetTradeItem, ClickType clickType) {
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

    public TradeData getOtherDataFromPlayer(Player player) {
        if(player.equals(firstPlayer.getPlayer())) return secondPlayer;
        if(player.equals(secondPlayer.getPlayer())) return firstPlayer;
        return null;
    }

    public boolean isPlayerHasEnoughSpace(List<TradeItem> itemToCheck, Player target) {
        HashMap<TradeItem, Integer> itemAmountMap = new HashMap<>();
        for(TradeItem currentTradeItem : itemToCheck) {
            itemAmountMap.put(currentTradeItem, currentTradeItem.getAmount());
        }

        for(ItemStack currentInventoryItem : target.getInventory().getContents()) {
            for (TradeItem currentKey : itemAmountMap.keySet()) {
                if(itemAmountMap.get(currentKey) <= 0) continue;
                if(currentInventoryItem == null || currentInventoryItem.getType() == Material.AIR) {
                    itemAmountMap.put(currentKey, itemAmountMap.get(currentKey) - currentKey.getItem().getMaxStackSize());
                    break; // go to the next player inventory slot
                } if(currentInventoryItem.isSimilar(currentKey.getItem())) {
                    itemAmountMap.put(currentKey, itemAmountMap.get(currentKey) -
                            (currentInventoryItem.getMaxStackSize() - currentInventoryItem.getAmount()));
                    break; // go to the next player inventory slot
                }
            }
            if(!checkIfAnyPositive(itemAmountMap)) break;
        }
        return !checkIfAnyPositive(itemAmountMap);
    }

    private boolean checkIfAnyPositive(HashMap<TradeItem, Integer> itemAmountMap) {
        boolean anyPositive = false;
        for (TradeItem currentKey : itemAmountMap.keySet()) {
            if(itemAmountMap.get(currentKey) > 0) {
                anyPositive = true;
                break;
            }
        }
        return anyPositive;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) return;
        if(event.getClickedInventory().getType() != InventoryType.PLAYER) return;
        if(!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        TradeData playerData = getDataFromPlayer(player);
        if(playerData == null) return;
        TradeData targetData = playerData.equals(firstPlayer) ? secondPlayer : firstPlayer;
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if(item == null) return;
        TradeItem tradeItemFromPlayer = getItemAsTradeItemFromInventory(item, player.getInventory());
        if(tradeItemFromPlayer == null) return;
        int amount = getAmountOfItemWithClickType(tradeItemFromPlayer, event.getClick());
        if(amount <= 0) return;
        tradeItemFromPlayer.setAmount(amount);
        if(playerData.getCurrentTradedItem().size() >= 20) return;

        List<TradeItem> currentTradedItemWithNew = new ArrayList<>(playerData.getCurrentTradedItem());
        currentTradedItemWithNew.add(tradeItemFromPlayer);
        if(!isPlayerHasEnoughSpace(currentTradedItemWithNew, targetData.getPlayer())) return; // Target don't have enough space

        if(removeItemFromTradeItemInPlayerInventory(tradeItemFromPlayer, player)) {
            if(playerData.addNewItem(item, tradeItemFromPlayer.getAmount()) != TradeData.AddItemError.NO_ERROR)
                return;
            firstPlayer.getPlayer().playSound(firstPlayer.getPlayer().getLocation(), Sound.ITEM_PICKUP, 1000, 1000);
            firstPlayer.setAcceptTrade(false);
            secondPlayer.getPlayer().playSound(secondPlayer.getPlayer().getLocation(), Sound.ITEM_PICKUP, 1000, 1000);
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
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
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
        firstPlayer.getPlayer().sendMessage(ColorUtils.translate("&a[O] &7You successfully finished the trade with "
                + secondPlayer.getPlayer().getName() + " ! &7(Check /trade log for more information)"));
        firstPlayer.getPlayer().playSound(firstPlayer.getPlayer().getLocation(), Sound.LEVEL_UP, 10000, 10000);
        secondPlayer.getPlayer().sendMessage(ColorUtils.translate("&a[O] &7You successfully finished the trade with "
                + firstPlayer.getPlayer().getName() + " ! &7(Check /trade log for more information)"));
        secondPlayer.getPlayer().playSound(secondPlayer.getPlayer().getLocation(), Sound.LEVEL_UP, 10000, 10000);
    }

    public void cancelTrade() {
        if(isTradeCanceled || isTradeFinished) return;
        isTradeCanceled = true;
        destroy();
        addTradeItemAndDropOverflow(firstPlayer.getCurrentTradedItem(), firstPlayer.getPlayer());
        addTradeItemAndDropOverflow(secondPlayer.getCurrentTradedItem(), secondPlayer.getPlayer());
        firstPlayer.getPlayer().playSound(firstPlayer.getPlayer().getLocation(), Sound.FIZZ, 100, 100);
        secondPlayer.getPlayer().playSound(secondPlayer.getPlayer().getLocation(), Sound.FIZZ, 100, 100);
        firstPlayer.getPlayer().sendMessage(ColorUtils.translate("&c[x] &7The trade with " + secondPlayer.getPlayer().getName() + " was canceled !"));
        secondPlayer.getPlayer().sendMessage(ColorUtils.translate("&c[x] &7The trade with " + firstPlayer.getPlayer().getName() + " was canceled !"));

    }

    public void destroy() {
        firstPlayer.getTradeInventory().delete();
        secondPlayer.getTradeInventory().delete();
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
