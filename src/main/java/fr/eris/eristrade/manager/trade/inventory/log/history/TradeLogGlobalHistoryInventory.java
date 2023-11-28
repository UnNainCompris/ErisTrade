package fr.eris.eristrade.manager.trade.inventory.log.history;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.trade.data.log.TradeSoftLogData;
import fr.eris.eristrade.manager.trade.language.TradeLanguage;
import fr.eris.erisutils.manager.config.data.Config;
import fr.eris.erisutils.manager.config.data.value.ConfigValue;
import fr.eris.erisutils.manager.language.data.LanguagePlaceholder;
import fr.eris.erisutils.utils.bukkit.BukkitTasks;
import fr.eris.erisutils.utils.bukkit.PlayerUtils;
import fr.eris.erisutils.utils.file.FileUtils;
import fr.eris.erisutils.utils.inventory.eris.ErisInventory;
import fr.eris.erisutils.utils.inventory.eris.ErisInventoryItem;
import fr.eris.erisutils.utils.item.ItemBuilder;
import fr.eris.erisutils.utils.item.ItemCache;
import fr.eris.erisutils.utils.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeLogGlobalHistoryInventory extends ErisInventory {
    private int inventoryOffset = 0;
    private final List<UUID> softTradeIdList;

    public TradeLogGlobalHistoryInventory(Player owner) {
        super(6, "&7Global trade History", owner);
        softTradeIdList = new ArrayList<>();
        for(File softTradeDataFile : FileUtils.getAllFile(FileUtils.getOrCreateFile(FileUtils.getOrCreateFile(
                FileUtils.ROOT_FOLDER, "tradelog"), "softData"), ".json")) {
            softTradeIdList.add(UUID.fromString(softTradeDataFile.getName().replace(".json", "")));
        }
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
                            ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemCache.ItemColor.ORANGE, false).setDisplayName("&6Up").build(),
                    (event) -> {
                        inventoryOffset = (int) Math.max(0, Math.min(inventoryOffset - 1, Math.floor(softTradeIdList.size() / 7f) - 4));
                        openInventory();
                    }));
        }
        if(inventoryOffset != Math.floor(softTradeIdList.size() / 7f) - 4) {
            setItem(27, ErisInventoryItem.create(() ->
                            ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemCache.ItemColor.BLUE, false).setDisplayName("&9Down").build(),
                    (event) -> {
                        inventoryOffset = (int) Math.max(0, Math.min(inventoryOffset + 1, Math.floor(softTradeIdList.size() / 7f) - 4));
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
        int rowModifier = 0;
        for(int currentRow = 0 ; currentRow < 7 ; currentRow++) {
            if(inventoryOffset == 0 && currentRow == 0) {
                currentRow++;
                rowModifier = 1;
            }
            if(inventoryOffset == Math.floor(softTradeIdList.size() / 7f) - 4 && currentRow == 6) break;
            for(int currentSlot = 0 ; currentSlot < 7 ; currentSlot++) {
                if(softTradeIdList.size() <= (currentRow - rowModifier) * 7 + currentSlot + inventoryOffset * 7) break;
                if(currentRow * 9 + currentSlot + 1 > getInventoryRowAmount() * 9 - 1) break;
                UUID currentSoftTradeId = softTradeIdList.get((currentRow - rowModifier) * 7 + currentSlot + inventoryOffset * 7);
                TradeSoftLogData tradeSoftLogData = ErisTrade.getTradeManager().getSoftTradeData(currentSoftTradeId);
                if(tradeSoftLogData == null) {
                    softTradeIdList.remove((currentRow - rowModifier) * 7 + currentSlot + inventoryOffset * 7);
                    currentSlot--;
                    continue;
                }
                if(tradeSoftLogData.isTradePublic() || inventoryOwner.hasPermission(ErisTrade.getConfiguration().permissionPrefix() + ".trade.history.bypass.privatetrade")) {
                    setItem(currentRow * 9 + currentSlot + 1, ErisInventoryItem.create(() ->
                        new ItemBuilder().setMaterial(Material.PAINTING).setDisplayName((tradeSoftLogData.isTradePublic() ? "&c!" : "") +
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
                } else {
                    softTradeIdList.remove((currentRow - rowModifier) * 7 + currentSlot + inventoryOffset * 7);
                    currentSlot--;
                }
            }
        }

        if(inventoryOffset == Math.max(0, Math.floor(softTradeIdList.size() / 7f) - 4)) {
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
