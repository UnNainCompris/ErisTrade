package fr.eris.eristrade.manager.trade.inventory;

import de.tr7zw.changeme.nbtapi.NBTItem;
import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.impl.ImplementationManager;
import fr.eris.eristrade.manager.trade.config.TradeConfig;
import fr.eris.eristrade.manager.trade.data.Trade;
import fr.eris.eristrade.manager.trade.data.TradeData;
import fr.eris.eristrade.manager.trade.data.TradeItem;
import fr.eris.eristrade.manager.trade.language.TradeLanguage;
import fr.eris.erisutils.ErisUtils;
import fr.eris.erisutils.manager.language.data.LanguagePlaceholder;
import fr.eris.erisutils.utils.asker.number.DoubleInteractiveAsker;
import fr.eris.erisutils.utils.asker.number.IntegerInteractiveAsker;
import fr.eris.erisutils.utils.bukkit.BukkitTasks;
import fr.eris.erisutils.utils.bukkit.ColorUtils;
import fr.eris.erisutils.utils.bukkit.PlayerUtils;
import fr.eris.erisutils.utils.error.exception.ErisPluginException;
import fr.eris.erisutils.utils.inventory.eris.ErisInventory;
import fr.eris.erisutils.utils.inventory.eris.ErisInventoryItem;
import fr.eris.erisutils.utils.item.ItemBuilder;
import fr.eris.erisutils.utils.item.ItemCache;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TradeInventory extends ErisInventory {

    private final TradeData ownerTradeData;
    private final Trade targetTrade;


    public TradeInventory(String inventoryName, Player inventoryOwner, TradeData ownerTradeData, Trade targetTrade) {
        super(6, inventoryName, new TradeInventoryHolder(ownerTradeData), inventoryOwner);
        this.ownerTradeData = ownerTradeData;
        this.targetTrade = targetTrade;
    }

    @Override
    public void setContent() {
        try {
            updateOwnerTradedItem();
            updateSeparator();
            updateTradedPlayerTradedItem();
            updateToolBar();
        } catch (ErisPluginException exception) {
            exception.printStackTrace();
            targetTrade.cancelTrade();
        }
    }

    private void updateToolBar() throws ErisPluginException {
        int inventorySize = getInventoryRowAmount() * 9;
        for(int slot : new int[]{inventorySize - 9, inventorySize - 8, inventorySize - 7, inventorySize - 6, inventorySize - 5,
                inventorySize - 4, inventorySize - 3, inventorySize - 2, inventorySize - 1}) {
            setItem(slot, ErisInventoryItem.create(() -> ItemCache.placeHolder));
        }

        setItem(inventorySize - 5, ErisInventoryItem.create(() ->
                ItemBuilder.placeHolders(Material.BOOK, false).setDisplayName(
                        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTradeInstructionItemName().getValue())
                        .setLore(ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTradeInstructionItemLore().getValue().split("\n")
                ).build()));
        if(ImplementationManager.getEconomy() != null && ErisTrade.getConfigManager().getConfig(TradeConfig.class).getIsMoneyInTrade().getValue()) {
            updateOwnerTradedMoney();
            updateTradedPlayerTradedMoney();
        } if (ErisTrade.getConfigManager().getConfig(TradeConfig.class).getIsExperienceInTrade().getValue()) {
            updateOwnerTradedExperience();
            updateTradedPlayerTradedExperience();
        }
        updateOwnerTradeValidation();
        updateTradedPlayerTradeValidation();
    }

    public void updateOwnerTradeValidation() {
        setItem(getInventoryRowAmount() * 9 - 9, ErisInventoryItem.create(() -> {
            if(!targetTrade.isAnythingTraded()) {
                return ItemBuilder.placeHolders(Material.WOOL, ItemCache.ItemColor.GRAY, false)
                        .setDisplayName(ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getCannotTradeNoting().getValue()).build();
            }
            return ItemBuilder.placeHolders(Material.WOOL, (ownerTradeData.isAcceptTrade()) ? ItemCache.ItemColor.LIME : ItemCache.ItemColor.RED, false)
                    .setDisplayName((ownerTradeData.isAcceptTrade()) ?
                            ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getClickHereCancelTrade().getValue() :
                            ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getClickHereAcceptTrade().getValue()).build();
        }, (event) -> {
            if(event.getCurrentItem().getDurability() != ItemCache.ItemColor.GRAY) {
                ownerTradeData.setAcceptTrade(!ownerTradeData.isAcceptTrade());
                targetTrade.updateInventory();
            }
        }));
    }

    public void updateOwnerTradedMoney() {
        setItem(getInventoryRowAmount() * 9 - 8,
                ErisInventoryItem.create(() -> new ItemBuilder().setMaterial(Material.GOLD_NUGGET).setDisplayName(
                        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTradedMoneyItemName().parsePlaceholders(
                                LanguagePlaceholder.create("%value%", String.valueOf(ownerTradeData.getTradedMoney()))))
                                .setLore(ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getClickHereEditAmount().getValue()).build(),
                (event) -> {
                    ownerTradeData.setCanClose(true);
                    new DoubleInteractiveAsker(ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getInputMoney().getValue(), inventoryOwner,
                    (value) -> {
                        if(ownerTradeData.getTargetTrade().isTradeCanceled() || ownerTradeData.getTargetTrade().isTradeFinished()) {
                            inventoryOwner.closeInventory();
                            return;
                        }
                        if(value == null) value = 0.0d;
                        value = Math.max(0, value);
                        double maxMoneyInTrade = ErisTrade.getConfigManager().getConfig(TradeConfig.class).getMaxMoneyInTrade().getValue();
                        if(maxMoneyInTrade > 0) {
                            value = Math.min(maxMoneyInTrade, value);
                        }
                        if (ImplementationManager.getEconomy().getBalance(inventoryOwner) >= value) {
                            ownerTradeData.setAcceptTrade(false);
                            ownerTradeData.setTradedMoney(value);
                            targetTrade.updateInventory();
                        }
                        else ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getDontHaveEnoughMoney().sendMessage(inventoryOwner);
                        BukkitTasks.syncLater(() -> {
                            openInventory();
                            ownerTradeData.setCanClose(false);
                        }, 2L);
                    }, Arrays.asList(0.1d, 1d, 100d, 1000d, 10000d), Arrays.asList(0.1d, 1d, 100d, 1000d, 10000d));
                }));
    }

    public void updateOwnerTradedExperience() {
        int slot = getInventoryRowAmount() * 9 - 8;
        if(inventoryContent.get(slot).getItem().getType() != Material.STAINED_GLASS_PANE) slot++; // go to the slot next to the money
        setItem(slot,
                ErisInventoryItem.create(() -> new ItemBuilder().setMaterial(Material.EXP_BOTTLE).setDisplayName(
                        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTradedExperienceItemName().parsePlaceholders(
                        LanguagePlaceholder.create("%value%", String.valueOf(ownerTradeData.getTradedExperience()))))
                                .setLore(ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getClickHereEditAmount().getValue()).build(),
                (event) -> {
                    ownerTradeData.setCanClose(true);
                    new IntegerInteractiveAsker(ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getInputExperience().getValue(), inventoryOwner,
                        (value) -> {
                            if(ownerTradeData.getTargetTrade().isTradeCanceled() || ownerTradeData.getTargetTrade().isTradeFinished()) {
                                inventoryOwner.closeInventory();
                                return;
                            }
                            if(value == null) value = 0;
                            value = Math.max(0, value);
                            int maxExperienceInTrade = ErisTrade.getConfigManager().getConfig(TradeConfig.class).getMaxExperienceInTrade().getValue();
                            if(maxExperienceInTrade > 0) {
                                value = Math.min(maxExperienceInTrade, value);
                            }
                            if (PlayerUtils.getPlayerExp(inventoryOwner) >= value) {
                                ownerTradeData.setTradedExperience(value);
                                targetTrade.updateInventory();
                                ownerTradeData.setAcceptTrade(false);
                            }
                            else ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getDontHaveEnoughExperience().sendMessage(inventoryOwner);
                            BukkitTasks.syncLater(() -> {
                                openInventory();
                                ownerTradeData.setCanClose(false);
                            }, 2L);
                        }, Arrays.asList(1, 10, 100, 1000, 10000), Arrays.asList(1, 10, 100, 1000, 10000));
                }));
    }

    public void updateTradedPlayerTradeValidation() {
        setItem(getInventoryRowAmount() * 9 - 1, ErisInventoryItem.create(() -> {
            if(!targetTrade.isAnythingTraded()) {
                return ItemBuilder.placeHolders(Material.WOOL, ItemCache.ItemColor.GRAY, false)
                        .setDisplayName(ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getCannotTradeNoting().getValue()).build();
            }
            return ItemBuilder.placeHolders(Material.WOOL, (getTradedPlayerTradeData().isAcceptTrade()) ? ItemCache.ItemColor.LIME : ItemCache.ItemColor.RED, false)
                    .setDisplayName((getTradedPlayerTradeData().isAcceptTrade()) ?
                            ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTargetHaveAcceptTrade().parsePlaceholders(
                            LanguagePlaceholder.create("%target%", getTradedPlayer().getName())) :
                            ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTargetDontHaveAcceptTrade().parsePlaceholders(
                                    LanguagePlaceholder.create("%target%", getTradedPlayer().getName()))).build();
        }));
    }

    public void updateTradedPlayerTradedMoney() {
        setItem(getInventoryRowAmount() * 9 - 2, ErisInventoryItem.create(() -> new ItemBuilder().setMaterial(Material.GOLD_NUGGET)
                        .setDisplayName(ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTradedMoneyItemName().parsePlaceholders(
                                LanguagePlaceholder.create("%value%", String.valueOf(getTradedPlayerTradeData().getTradedMoney())))).build()));
    }

    public void updateTradedPlayerTradedExperience() {
        int slot = getInventoryRowAmount() * 9 - 2;
        if(inventoryContent.get(slot).getItem().getType() != Material.STAINED_GLASS_PANE) slot--;
        setItem(slot, ErisInventoryItem.create(() -> new ItemBuilder().setMaterial(Material.EXP_BOTTLE)
                .setDisplayName(ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getTradedExperienceItemName().parsePlaceholders(
                        LanguagePlaceholder.create("%value%", String.valueOf(getTradedPlayerTradeData().getTradedExperience())))).build()));
    }

    private void updateOwnerTradedItem() throws ErisPluginException {
        for(TradeItem tradeItem : ownerTradeData.getCurrentTradedItem()) {
            int rawItemSlot = ownerTradeData.getCurrentTradedItem().indexOf(tradeItem);
            int itemSlot = (int) (rawItemSlot % 4 + (Math.floor(rawItemSlot / 4f) * 9));
            setItem(itemSlot,
                ErisInventoryItem.create(tradeItem::buildForDisplay,
                    (event) -> {
                        if(ownerTradeData.removeItem(Trade.getAmountOfItemWithClickType(tradeItem, event.getClick()),
                            new NBTItem(event.getCurrentItem()).getUUID("eristrade.itemkey")) == TradeData.RemoveItemError.NO_ERROR) {
                                getTradedPlayer().playSound(getTradedPlayer().getLocation(), Sound.ITEM_PICKUP, 1000, 1000);
                                inventoryOwner.playSound(inventoryOwner.getLocation(), Sound.ITEM_PICKUP, 1000, 1000);
                                targetTrade.updateInventory();
                        }
                    }
                )
            );
        }
    }

    private void updateTradedPlayerTradedItem() throws ErisPluginException {
        TradeData tradedPlayerData = getTradedPlayerTradeData();
        for(TradeItem tradeItem : tradedPlayerData.getCurrentTradedItem()) {
            int rawItemSlot = tradedPlayerData.getCurrentTradedItem().indexOf(tradeItem);
            int itemSlot = (int) (5 + (rawItemSlot % 4 + (Math.floor(rawItemSlot / 4f) * 9)));
            setItem(itemSlot, ErisInventoryItem.create(tradeItem::buildForDisplay));
        }
    }

    public Player getTradedPlayer() {
        return targetTrade.getOtherDataFromPlayer(inventoryOwner).getPlayer();
    }

    public TradeData getTradedPlayerTradeData() {
        return targetTrade.getOtherDataFromPlayer(inventoryOwner);
    }

    public void updateSeparator() throws ErisPluginException {
        List<Integer> separatorSlotList = Arrays.asList(4, 13, 22, 31, 40);
        for(int slot : separatorSlotList) { // separator between the 2 trade
            final short itemColor;
            boolean glowing = false;

            int itemIndex = separatorSlotList.indexOf(slot);
            int itemState = (int)Math.ceil(((itemIndex + 1f) * 20f - targetTrade.getTickSinceTradeAccept()) / 20f);

            if(itemState <= 0) {
                itemColor = ItemCache.ItemColor.LIME;
                glowing = itemState < 0;
            }
            else itemColor = ItemCache.ItemColor.GRAY;

            ItemBuilder separator = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, itemColor, glowing);
            setItem(slot, ErisInventoryItem.create(separator::build));
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onOpen() {

    }
}
