package fr.eris.eristrade.manager.trade.inventory.log.history;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.trade.data.log.TradeSoftLogData;
import fr.eris.eristrade.manager.trade.language.TradeLanguage;
import fr.eris.erisutils.manager.config.data.Config;
import fr.eris.erisutils.manager.config.data.value.ConfigValue;
import fr.eris.erisutils.manager.language.data.LanguagePlaceholder;
import fr.eris.erisutils.utils.bukkit.BukkitTasks;
import fr.eris.erisutils.utils.bukkit.PlayerUtils;
import fr.eris.erisutils.utils.inventory.eris.ErisInventory;
import fr.eris.erisutils.utils.inventory.eris.ErisInventoryItem;
import fr.eris.erisutils.utils.item.ItemBuilder;
import fr.eris.erisutils.utils.item.ItemCache;
import fr.eris.erisutils.utils.item.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeLogPlayerHistoryInventory extends ErisInventory {
        private final Player target;
        private final List<UUID> targetSoftTradeIdList;
        private int inventoryOffset = 0;
        public TradeLogPlayerHistoryInventory(Player owner, Player target) {
            super(6, "&7Trade History of : " + target.getName(), owner);
            this.target = target;
            targetSoftTradeIdList = ErisTrade.getPlayerDataManager().getPlayerData(target.getUniqueId()).getPlayerTradeSoftDataID();
        }

        @Override
        public void setContent() {
            updateBorder();
            updateContent();
        }

        private void updateBorder() {
            for(int slot : new int[]{0, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 53}) {
                setItem(slot, ErisInventoryItem.create(() -> ItemCache.placeHolder));
            }

            if(inventoryOffset != 0) {
                setItem(18, ErisInventoryItem.create(() ->
                                ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemCache.ItemColor.ORANGE, false).setDisplayName(
                                        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getUpItemName().getValue()
                                ).build(),
                        (event) -> {
                            inventoryOffset = Math.max(0, Math.min(inventoryOffset - 1, targetSoftTradeIdList.size() - 4));
                            openInventory();
                        }));
            }
            if(inventoryOffset != targetSoftTradeIdList.size() - 4) {
                setItem(27, ErisInventoryItem.create(() ->
                                ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemCache.ItemColor.BLUE, false).setDisplayName(
                                        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getDownItemName().getValue()
                                ).build(),
                        (event) -> {
                            inventoryOffset = Math.max(0, Math.min(inventoryOffset + 1, targetSoftTradeIdList.size() - 4));
                            openInventory();
                        }));
            }
        }


        private void updateContent() {
            if(inventoryOffset == 0) {
                for(int slot = 0 ; slot < 9 ; slot++) {
                    setItem(slot, ErisInventoryItem.create(() -> ItemCache.placeHolder));
                }
            }
            for(int currentRow : new int[]{1, 2, 3, 4}) {
                UUID softTradeDataID = targetSoftTradeIdList.get(currentRow + inventoryOffset);
                TradeSoftLogData tradeSoftLogData = ErisTrade.getTradeManager().getSoftTradeData(softTradeDataID);
                setItem(currentRow * 9 + 3, ErisInventoryItem.create(() ->
                                new ItemBuilder(ItemUtils.getPlayerHead(tradeSoftLogData.getRequesterUUID())).setDisplayName(
                                        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getRequesterItemHeadName()
                                                .parsePlaceholders(LanguagePlaceholder.create("%requester%",
                                                        PlayerUtils.toOfflinePlayer(tradeSoftLogData.getRequesterUUID()).getName()))).build(),
                        (event) -> openTradeHeavyDataLogInventory(tradeSoftLogData)));

                setItem(currentRow * 9 + 4, ErisInventoryItem.create(() ->
                        new ItemBuilder().setMaterial(Material.PAINTING).setDisplayName(
                            ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getRequestedItemHeadName()
                            .parsePlaceholders(
                                LanguagePlaceholder.create("%requested%", PlayerUtils.toOfflinePlayer(tradeSoftLogData.getRequesterUUID()).getName()),
                                LanguagePlaceholder.create("%requester%", PlayerUtils.toOfflinePlayer(tradeSoftLogData.getRequesterUUID()).getName()),
                                LanguagePlaceholder.create("%tradeDays%", String.valueOf(tradeSoftLogData.getDateWhenTrade().getDayOfMonth())),
                                LanguagePlaceholder.create("%tradeMonths%", String.valueOf(tradeSoftLogData.getDateWhenTrade().getMonthValue())),
                                LanguagePlaceholder.create("%tradeYears%", String.valueOf(tradeSoftLogData.getDateWhenTrade().getYear())),
                                LanguagePlaceholder.create("%amountOfDaysPassed%", String.valueOf(tradeSoftLogData.getDaysPassed())),
                                LanguagePlaceholder.create("%timeTradeViewed%", String.valueOf(tradeSoftLogData.getTimeViewed()))
                            )).build(),
                        (event) -> openTradeHeavyDataLogInventory(tradeSoftLogData)));

                setItem(currentRow * 9 + 5, ErisInventoryItem.create(() ->
                                new ItemBuilder(ItemUtils.getPlayerHead(tradeSoftLogData.getRequestedPlayerUUID())).setDisplayName(
                                        ErisTrade.getLanguageManager().getLanguage(TradeLanguage.class).getRequestedItemHeadName()
                                                .parsePlaceholders(LanguagePlaceholder.create("%requested%",
                                                        PlayerUtils.toOfflinePlayer(tradeSoftLogData.getRequestedPlayerUUID()).getName()))).build(),
                        (event) -> openTradeHeavyDataLogInventory(tradeSoftLogData)));
            }

            if(inventoryOffset == Math.max(0, targetSoftTradeIdList.size() - 4)) {
                int inventorySize = getInventoryRowAmount() * 9 - 1;
                for(int slot = 0 ; slot < 9 ; slot++) {
                    setItem(inventorySize - slot, ErisInventoryItem.create(() -> ItemCache.placeHolder));
                }
            }
        }

        public void openTradeHeavyDataLogInventory(TradeSoftLogData tradeSoftLogData) {
            tradeSoftLogData.setTimeViewed(tradeSoftLogData.getTimeViewed() + 1);
            BukkitTasks.async(tradeSoftLogData::save);
        }

        @Override
        public void onClose() {
            delete();
        }

        @Override
        public void onClick(InventoryClickEvent event) {
            event.setCancelled(true);
        }


        @Override
        public void onOpen() {

        }
}
