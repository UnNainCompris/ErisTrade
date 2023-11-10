package fr.eris.eristrade.manager.trade.inventory;

import de.tr7zw.changeme.nbtapi.NBTItem;
import fr.eris.eristrade.manager.impl.ImplementationManager;
import fr.eris.eristrade.manager.trade.data.Trade;
import fr.eris.eristrade.manager.trade.data.TradeData;
import fr.eris.eristrade.manager.trade.data.TradeItem;
import fr.eris.erisutils.utils.bukkit.ColorUtils;
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


    public TradeInventory(String inventoryName, Player owner, TradeData ownerTradeData, Trade targetTrade) {
        super(6, inventoryName, owner);
        this.ownerTradeData = ownerTradeData;
        this.targetTrade = targetTrade;
        this.getInventoryStateData().setAllowedToClose(true);
    }

    @Override
    public void update(HashMap<Integer, ErisInventoryItem> inventoryMap) {
        try {
            updateOwnerTradedItem(inventoryMap);
            updateSeparator(inventoryMap);
            updateTradedPlayerTradedItem(inventoryMap);
            updateToolBar(inventoryMap);
        } catch (ErisPluginException exception) {
            exception.printStackTrace();
            targetTrade.cancelTrade();
        }
    }

    private void updateToolBar(HashMap<Integer, ErisInventoryItem> inventoryMap) throws ErisPluginException {
        int inventorySize = getInventoryRowAmount() * 9;
        for(int slot : new int[]{inventorySize - 9, inventorySize - 8, inventorySize - 7, inventorySize - 6, inventorySize - 5,
                inventorySize - 4, inventorySize - 3, inventorySize - 2, inventorySize - 1}) {
            inventoryMap.put(slot, ErisInventoryItem.create(() -> ItemCache.placeHolder));
        }

        inventoryMap.put(inventorySize - 5, ErisInventoryItem.create(() ->
                ItemBuilder.placeHolders(Material.BOOK, false).setDisplayName("&7How to use ?").setLore(
                        "&2Right Click &7; &aShift + Right Click &7; &dMiddle Click &7; &4Left Click &7; &cShift + Left Click",
                        "&7In your inventory : ",
                        "  &7Add: &2Half Stack &7; &aHalf Total &7; &dA Stack &7; &41 Item ; &cTotal",
                        "&7In the trade : ",
                        "  &7Remove: &2Half Stack &7; &aHalf Total &7; &dA Stack &7; &41 Item ; &cTotal"
                ).build()));
        if(ImplementationManager.getEconomy() != null) {
            updateOwnerTradedMoney(inventoryMap);
            updateTradedPlayerTradedMoney(inventoryMap);
        }
        updateOwnerTradeValidation(inventoryMap);
        updateTradedPlayerTradeValidation(inventoryMap);
    }

    public void updateOwnerTradeValidation(HashMap<Integer, ErisInventoryItem> inventoryMap) {
        inventoryMap.put(getInventoryRowAmount() * 9 - 9, ErisInventoryItem.create(() -> {
            if(!targetTrade.isAnythingTraded()) {
                return ItemBuilder.placeHolders(Material.WOOL, ItemCache.ItemColor.GRAY, false)
                        .setDisplayName("&7You cannot trade nothing on both side !").build();
            }
            return ItemBuilder.placeHolders(Material.WOOL, (ownerTradeData.isAcceptTrade()) ? ItemCache.ItemColor.LIME : ItemCache.ItemColor.RED, false)
                    .setDisplayName((ownerTradeData.isAcceptTrade()) ? "&cClick here to cancel the trade !" : "&aClick here to accept the trade !").build();
        }, (event) -> {
            if(event.getCurrentItem().getDurability() != ItemCache.ItemColor.GRAY) {
                ownerTradeData.setAcceptTrade(!ownerTradeData.isAcceptTrade());
                targetTrade.updateInventory();
            }
        }));
    }

    public void updateOwnerTradedMoney(HashMap<Integer, ErisInventoryItem> inventoryMap) {
        inventoryMap.put(getInventoryRowAmount() * 9 - 8,
                ErisInventoryItem.create(() -> new ItemBuilder().setMaterial(Material.GOLD_NUGGET).setDisplayName("&6Money: &e" + ownerTradeData.getTradedMoney()).setLore("&8Click to edit the amount !").build(),
                (event) -> {
                    owner.sendMessage(ColorUtils.translate("&c&lNot finished yet !"));
                    //ownerTradeData.setCanClose(true);
                    //new NumberAsker(owner, "Input the money you want to trade",
                    //        (number) -> {
                    //            if (ImplementationManager.getEconomy().getBalance(owner) >= number.longValue())
                    //                ownerTradeData.setTradedMoney(number.longValue());
                    //            else owner.sendMessage("&7You don't have enough money !");
                    //        });
                }));
    }

    public void updateTradedPlayerTradeValidation(HashMap<Integer, ErisInventoryItem> inventoryMap) {
        inventoryMap.put(getInventoryRowAmount() * 9 - 1, ErisInventoryItem.create(() -> {
            if(!targetTrade.isAnythingTraded()) {
                return ItemBuilder.placeHolders(Material.WOOL, ItemCache.ItemColor.GRAY, false)
                        .setDisplayName("&7You cannot trade nothing on both side !").build();
            }
            return ItemBuilder.placeHolders(Material.WOOL, (getTradedPlayerTradeDate().isAcceptTrade()) ? ItemCache.ItemColor.LIME : ItemCache.ItemColor.RED, false)
                    .setDisplayName((getTradedPlayerTradeDate().isAcceptTrade()) ? "&a" + getTradedPlayer().getName() + " has accepted the trade !" :
                                        "&c" + getTradedPlayer().getName() + " doesn't have accepted the trade !").build();
        }));
    }

    public void updateTradedPlayerTradedMoney(HashMap<Integer, ErisInventoryItem> inventoryMap) {
        inventoryMap.put(getInventoryRowAmount() * 9 - 2, ErisInventoryItem.create(() -> new ItemBuilder().setMaterial(Material.GOLD_NUGGET)
                        .setDisplayName("&6Money: &e" + getTradedPlayerTradeDate().getTradedMoney()).build()));
    }

    private void updateOwnerTradedItem(HashMap<Integer, ErisInventoryItem> inventoryMap) throws ErisPluginException {
        for(TradeItem tradeItem : ownerTradeData.getCurrentTradedItem()) {
            int rawItemSlot = ownerTradeData.getCurrentTradedItem().indexOf(tradeItem);
            int itemSlot = (int) (rawItemSlot % 4 + (Math.floor(rawItemSlot / 4f) * 9));
            inventoryMap.put(itemSlot,
                ErisInventoryItem.create(tradeItem::buildForDisplay,
                    (event) -> {
                        if(ownerTradeData.removeItem(Trade.getAmountOfItemWithClickType(tradeItem, event.getClick()),
                            new NBTItem(event.getCurrentItem()).getUUID("eristrade.itemkey")) == TradeData.RemoveItemError.NO_ERROR) {
                                getTradedPlayer().playSound(getTradedPlayer().getLocation(), Sound.ITEM_PICKUP, 1000, 1000);
                                owner.playSound(owner.getLocation(), Sound.ITEM_PICKUP, 1000, 1000);
                                targetTrade.updateInventory();
                        }
                    }
                )
            );
        }
    }

    private void updateTradedPlayerTradedItem(HashMap<Integer, ErisInventoryItem> inventoryMap) throws ErisPluginException {
        TradeData tradedPlayerData = getTradedPlayerTradeDate();
        for(TradeItem tradeItem : tradedPlayerData.getCurrentTradedItem()) {
            int rawItemSlot = tradedPlayerData.getCurrentTradedItem().indexOf(tradeItem);
            int itemSlot = (int) (5 + (rawItemSlot % 4 + (Math.floor(rawItemSlot / 4f) * 9)));
            inventoryMap.put(itemSlot, ErisInventoryItem.create(tradeItem::buildForDisplay));
        }
    }

    public Player getTradedPlayer() {
        return targetTrade.getOtherDataFromPlayer(owner).getPlayer();
    }

    public TradeData getTradedPlayerTradeDate() {
        return targetTrade.getOtherDataFromPlayer(owner);
    }

    public void updateSeparator(HashMap<Integer, ErisInventoryItem> inventoryMap) throws ErisPluginException {
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

            inventoryMap.put(slot, ErisInventoryItem.create(separator::build));
        }
    }

    @Override
    public boolean onClose() {
        return true;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onInventoryNameChange() {

    }

    @Override
    public void onInventorySizeChange() {

    }

    @Override
    public boolean onPreOpen() {
        return false;
    }

    @Override
    public void onOpen() {

    }
}
