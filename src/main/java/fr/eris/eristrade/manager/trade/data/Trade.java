package fr.eris.eristrade.manager.trade.data;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.impl.ImplementationManager;
import fr.eris.eristrade.manager.trade.config.TradeConfig;
import fr.eris.eristrade.manager.trade.inventory.TradeInventoryHolder;
import fr.eris.eristrade.manager.trade.language.TradeLanguage;
import fr.eris.erisutils.manager.language.data.LanguagePlaceholder;
import fr.eris.erisutils.utils.bukkit.BukkitTasks;
import fr.eris.erisutils.utils.bukkit.ColorUtils;
import fr.eris.erisutils.utils.bukkit.PlayerUtils;
import fr.eris.erisutils.utils.storage.Tuple;
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

    @Getter private final TradeData firstPlayer, secondPlayer; // firstPlayer is also the requester of the trade

    @Getter private boolean isTradeCanceled, isTradeFinished;
    @Getter private int tickSinceTradeAccept = 0;
    private int tradeTickCounter = 0;
    private BukkitTask tradeTask; // a task that schedule every tick from the start of the trade
    private final List<TradeItem> recentlyUpdatedItem;

    public Trade(Player firstPlayer, Player secondPlayer) {
        this.firstPlayer = new TradeData(firstPlayer, ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTradeInventoryName()
                .parsePlaceholders(LanguagePlaceholder.create("%target%", secondPlayer.getName())), this);
        this.secondPlayer = new TradeData(secondPlayer, ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTradeInventoryName()
                .parsePlaceholders(LanguagePlaceholder.create("%target%", firstPlayer.getName())), this);
        this.recentlyUpdatedItem = new ArrayList<>();
        tradeTask = BukkitTasks.syncTimer(this::tradeTask, 1, 1);
        updateInventory();
        this.firstPlayer.getTradeInventory().openInventory();
        this.secondPlayer.getTradeInventory().openInventory();
        Bukkit.getServer().getPluginManager().registerEvents(this, ErisTrade.getInstance());
    }

    public void tradeTask() {
        tradeTickCounter++;
        if(isTradeCanceled || isTradeFinished) {
            tradeTask.cancel();
            return;
        }
        boolean shouldUpdate = false;
        for(TradeItem currentItem : new ArrayList<>(recentlyUpdatedItem)) {
            if(tradeTickCounter - currentItem.getLastEditTick() > 30) {
                recentlyUpdatedItem.remove(currentItem);
                currentItem.setDisplayHasEdited(false);
                shouldUpdate = true;
                continue;
            }
            if((tradeTickCounter - currentItem.getLastEditTick()) % 5 == 0) {
                currentItem.setDisplayHasEdited(!currentItem.isDisplayHasEdited());
                shouldUpdate = true;
            }
        }

        if(shouldUpdate) updateInventory();


        if(!firstPlayer.isAcceptTrade() || !secondPlayer.isAcceptTrade()) {
            tickSinceTradeAccept = 0;
            return;
        }
        tickSinceTradeAccept++;
        if(tickSinceTradeAccept % 20 == 0) {
            firstPlayer.getPlayer().playSound(firstPlayer.getPlayer().getLocation(), Sound.NOTE_PLING, 1000, 1000);
            firstPlayer.getPlayer().playSound(firstPlayer.getPlayer().getLocation(), Sound.NOTE_PLING, 1000, 1000);
            updateInventory();
        }
        if(tickSinceTradeAccept == 120) {
            if(!isPlayerHasEnoughSpace(firstPlayer.getCurrentTradedItem(), secondPlayer.getPlayer())) {
                ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTargetDontHaveEnoughInventorySpace().sendMessage(firstPlayer.getPlayer(),
                        LanguagePlaceholder.create("%target%", secondPlayer.getPlayer().getName()));
                ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getSelfDontHaveEnoughInventorySpace().sendMessage(secondPlayer.getPlayer());
                cancelTrade();
                return;
            }

            if(!isPlayerHasEnoughSpace(secondPlayer.getCurrentTradedItem(), firstPlayer.getPlayer())) {
                ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getSelfDontHaveEnoughInventorySpace().sendMessage(firstPlayer.getPlayer());
                ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTargetDontHaveEnoughInventorySpace().sendMessage(secondPlayer.getPlayer(),
                        LanguagePlaceholder.create("%target%", firstPlayer.getPlayer().getName()));
                cancelTrade();
                return;
            }

            finishTrade();
        }
    }

    public void updateInventory() {
        if (isTradeCanceled || isTradeFinished) return;
        if(!firstPlayer.isAcceptTrade() || !secondPlayer.isAcceptTrade()) tickSinceTradeAccept = 0;
        firstPlayer.getTradeInventory().openInventory();
        secondPlayer.getTradeInventory().openInventory();
    }

    public boolean isAnythingTraded() {
        return !(firstPlayer.getCurrentTradedItem().isEmpty() && secondPlayer.getCurrentTradedItem().isEmpty()
                && firstPlayer.getTradedMoney() == 0 && secondPlayer.getTradedMoney() == 0
                && firstPlayer.getTradedExperience() == 0 && secondPlayer.getTradedExperience() == 0);
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
        if(event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof TradeInventoryHolder) {
            TradeInventoryHolder tradeInventoryHolder = (TradeInventoryHolder) event.getInventory().getHolder();
            Trade trade = tradeInventoryHolder.getPlayerTradeData().getTargetTrade();
            if(trade.isTradeCanceled) {
                trade.cancelTrade();
            }
            event.setCancelled(true);
            if(trade.getFirstPlayer().getPlayer().getOpenInventory() == null || trade.getSecondPlayer().getPlayer().getOpenInventory() == null) {
                trade.cancelTrade();
                trade.getFirstPlayer().getPlayer().closeInventory();
                trade.getSecondPlayer().getPlayer().closeInventory();
            }
        }
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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerProcessCommand(PlayerCommandPreprocessEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent event) {
        if(getDataFromPlayer((Player) event.getPlayer()) != null)
            if(getDataFromPlayer((Player) event.getPlayer()).isCanClose()) return;
        tradeCanceler(event.getPlayer());
    }
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom(), to = event.getTo();
        if(from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;
        tradeCanceler(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        tradeCanceler(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        tradeCanceler(event.getEntity());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        tradeCanceler(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
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
        if(((!hasEnoughMoneyForTrade(firstPlayer) || !hasEnoughMoneyForTrade(secondPlayer)) && ImplementationManager.getEconomy() != null) ||
                !hasEnoughExperienceForTrade(firstPlayer) || !hasEnoughExperienceForTrade(secondPlayer)) {
            cancelTrade();
            return;
        }
        if(isTradeCanceled || isTradeFinished) return;
        isTradeFinished = true;
        destroy();
        if(ImplementationManager.getEconomy() != null && ErisTrade.getConfigManager().getConfig(TradeConfig.class).getIsMoneyInTrade().getValue()) {
            sendAndRemoveMoney(firstPlayer, secondPlayer);
            sendAndRemoveMoney(secondPlayer, firstPlayer);
        } if (ErisTrade.getConfigManager().getConfig(TradeConfig.class).getIsExperienceInTrade().getValue()) {
            sendAndRemoveExperience(firstPlayer, secondPlayer);
            sendAndRemoveExperience(secondPlayer, firstPlayer);
        }
        addTradeItemAndDropOverflow(secondPlayer.getCurrentTradedItem(), firstPlayer.getPlayer());
        addTradeItemAndDropOverflow(firstPlayer.getCurrentTradedItem(), secondPlayer.getPlayer());
        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getFinishTrade().sendMessage(firstPlayer.getPlayer(),
                LanguagePlaceholder.create("%target%", secondPlayer.getPlayer().getName()));
        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getFinishTrade().sendMessage(secondPlayer.getPlayer(),
                LanguagePlaceholder.create("%target%", firstPlayer.getPlayer().getName()));

        boolean isExp = ErisTrade.getConfigManager().getConfig(TradeConfig.class).getIsExperienceInTrade().getValue(),
                isMoney = ImplementationManager.getEconomy() != null && ErisTrade.getConfigManager().getConfig(TradeConfig.class).getIsMoneyInTrade().getValue();

        if(firstPlayer.getTradedExperience() > 0 && firstPlayer.getTradedMoney() > 0) sendReceiveExperienceAndMoney(secondPlayer, firstPlayer);
        else if(firstPlayer.getTradedExperience() > 0) sendReceiveExperience(secondPlayer, firstPlayer);
        else if(firstPlayer.getTradedMoney() > 0) sendReceiveMoney(secondPlayer, firstPlayer);

        if(secondPlayer.getTradedExperience() > 0 && secondPlayer.getTradedMoney() > 0) sendReceiveExperienceAndMoney(firstPlayer, secondPlayer);
        else if(secondPlayer.getTradedExperience() > 0) sendReceiveExperience(firstPlayer, secondPlayer);
        else if(secondPlayer.getTradedMoney() > 0) sendReceiveMoney(firstPlayer, secondPlayer);


        firstPlayer.getPlayer().playSound(firstPlayer.getPlayer().getLocation(), Sound.LEVEL_UP, 10000, 10000);
        secondPlayer.getPlayer().playSound(secondPlayer.getPlayer().getLocation(), Sound.LEVEL_UP, 10000, 10000);
    }

    public void sendReceiveExperience(TradeData messageReceiver, TradeData otherPlayer) {
        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getFinishTradeReceiveExperience().sendMessage(messageReceiver.getPlayer(),
                LanguagePlaceholder.create("%target%", otherPlayer.getPlayer().getName()),
                LanguagePlaceholder.create("%experience%", String.valueOf(otherPlayer.getTradedExperience())));

        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getFinishTradeSendExperience().sendMessage(otherPlayer.getPlayer(),
                LanguagePlaceholder.create("%target%", messageReceiver.getPlayer().getName()),
                LanguagePlaceholder.create("%experience%", String.valueOf(otherPlayer.getTradedExperience())));
    }

    public void sendReceiveMoney(TradeData moneyReceiver, TradeData otherPlayer) {
        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getFinishTradeReceiveMoney().sendMessage(moneyReceiver.getPlayer(),
                LanguagePlaceholder.create("%target%", otherPlayer.getPlayer().getName()),
                LanguagePlaceholder.create("%money%", String.valueOf(otherPlayer.getTradedMoney())));

        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getFinishTradeSendMoney().sendMessage(otherPlayer.getPlayer(),
                LanguagePlaceholder.create("%target%", moneyReceiver.getPlayer().getName()),
                LanguagePlaceholder.create("%money%", String.valueOf(otherPlayer.getTradedMoney())));
    }

    public void sendReceiveExperienceAndMoney(TradeData bothReceiver, TradeData otherPlayer) {
        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getFinishTradeReceiveMoneyAndExperience().sendMessage(bothReceiver.getPlayer(),
                LanguagePlaceholder.create("%target%", otherPlayer.getPlayer().getName()),
                LanguagePlaceholder.create("%experience%", String.valueOf(otherPlayer.getTradedExperience())),
                LanguagePlaceholder.create("%money%", String.valueOf(otherPlayer.getTradedMoney())));

        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getFinishTradeSendMoneyAndExperience().sendMessage(otherPlayer.getPlayer(),
                LanguagePlaceholder.create("%target%", bothReceiver.getPlayer().getName()),
                LanguagePlaceholder.create("%experience%", String.valueOf(otherPlayer.getTradedExperience())),
                LanguagePlaceholder.create("%money%", String.valueOf(otherPlayer.getTradedMoney())));
    }

    public void cancelTrade() {
        if(isTradeCanceled || isTradeFinished) return;
        isTradeCanceled = true;
        destroy();
        addTradeItemAndDropOverflow(firstPlayer.getCurrentTradedItem(), firstPlayer.getPlayer());
        addTradeItemAndDropOverflow(secondPlayer.getCurrentTradedItem(), secondPlayer.getPlayer());
        firstPlayer.getPlayer().playSound(firstPlayer.getPlayer().getLocation(), Sound.FIZZ, 100, 100);
        secondPlayer.getPlayer().playSound(secondPlayer.getPlayer().getLocation(), Sound.FIZZ, 100, 100);
        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getCurrentTradeIsCanceled().sendMessage(firstPlayer.getPlayer(),
                LanguagePlaceholder.create("%target%", secondPlayer.getPlayer().getName()));
        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getCurrentTradeIsCanceled().sendMessage(secondPlayer.getPlayer(),
                LanguagePlaceholder.create("%target%", firstPlayer.getPlayer().getName()));
    }

    public void sendAndRemoveExperience(TradeData sender, TradeData receiver) {
        if(hasEnoughExperienceForTrade(sender)) {
            int newExp = PlayerUtils.getPlayerExp(sender.getPlayer()) - sender.getTradedExperience();
            sender.getPlayer().setExp(0);
            sender.getPlayer().setLevel(0);
            sender.getPlayer().giveExp(newExp);
            receiver.getPlayer().giveExp(sender.getTradedExperience());
        }
    }

    public boolean hasEnoughExperienceForTrade(TradeData target) {
        return PlayerUtils.getPlayerExp(target.getPlayer()) >= target.getTradedExperience();
    }

    public void sendAndRemoveMoney(TradeData sender, TradeData receiver) {
        if(hasEnoughMoneyForTrade(sender)) {
            ImplementationManager.getEconomy().withdrawPlayer(sender.getPlayer(), sender.getTradedMoney());
            ImplementationManager.getEconomy().depositPlayer(receiver.getPlayer(), sender.getTradedMoney());
        }
    }

    public boolean hasEnoughMoneyForTrade(TradeData target) {
        return ImplementationManager.getEconomy() != null && ImplementationManager.getEconomy().getBalance(target.getPlayer()) >= target.getTradedMoney();
    }

    public void destroy() {
        tradeTask.cancel();
        firstPlayer.getTradeInventory().closeInventory();
        secondPlayer.getTradeInventory().closeInventory();
        firstPlayer.getTradeInventory().delete();
        secondPlayer.getTradeInventory().delete();
        BukkitTasks.asyncLater(() -> {
            ErisTrade.getTradeManager().removeTrade(this);
            HandlerList.unregisterAll(this);
            firstPlayer.getTradeInventory().closeInventory();
            secondPlayer.getTradeInventory().closeInventory();
            firstPlayer.getTradeInventory().delete();
            secondPlayer.getTradeInventory().delete();
        }, 10);

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
