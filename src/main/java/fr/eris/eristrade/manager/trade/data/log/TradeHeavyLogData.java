package fr.eris.eristrade.manager.trade.data.log;

import com.google.gson.annotations.Expose;
import fr.eris.eristrade.manager.trade.data.Trade;
import fr.eris.eristrade.manager.trade.data.TradeData;
import fr.eris.eristrade.manager.trade.data.TradeItem;
import fr.eris.erisutils.utils.data.Data;
import fr.eris.erisutils.utils.file.FileUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeHeavyLogData extends Data {
    @Getter @Expose private UUID selfID;
    @Setter @Getter @Expose private UUID tradeSoftDataID;

    @Getter @Expose private UUID requesterUUID, requestedPlayerUUID;
    @Getter @Expose private double requesterTradedMoney, requestedPlayerTradedMoney;
    @Getter @Expose private int requesterTradedExperience, requestedPlayerTradedExperience;
    @Getter @Expose private List<TradeLogItem> requesterTradedItem, requestedPlayerTradedItem;

    @Expose private LocalDate dateWhenTrade;
    
    public static TradeHeavyLogData buildFromTrade(Trade trade) {
        TradeHeavyLogData tradeHeavyLogData = new TradeHeavyLogData();
        tradeHeavyLogData.selfID = UUID.randomUUID();
        TradeData requesterData = trade.getFirstPlayer(), requestedPlayerData = trade.getSecondPlayer();

        tradeHeavyLogData.requesterUUID = requesterData.getPlayer().getUniqueId();
        tradeHeavyLogData.requestedPlayerUUID = requestedPlayerData.getPlayer().getUniqueId();

        tradeHeavyLogData.requesterTradedMoney = requesterData.getTradedMoney();
        tradeHeavyLogData.requestedPlayerTradedMoney = requestedPlayerData.getTradedMoney();

        tradeHeavyLogData.requesterTradedExperience = requesterData.getTradedExperience();
        tradeHeavyLogData.requestedPlayerTradedExperience = requestedPlayerData.getTradedExperience();

        tradeHeavyLogData.requesterTradedItem = tradeHeavyLogData.parseTradeItemToLogItem(requesterData.getCurrentTradedItem());
        tradeHeavyLogData.requestedPlayerTradedItem = tradeHeavyLogData.parseTradeItemToLogItem(requestedPlayerData.getCurrentTradedItem());
        
        tradeHeavyLogData.dateWhenTrade = LocalDate.now();
        return tradeHeavyLogData;
    }

    private List<TradeLogItem> parseTradeItemToLogItem(List<TradeItem> tradeItemList) {
        List<TradeLogItem> tradeLogItems = new ArrayList<>();
        for(TradeItem tradeItem : tradeItemList) {
            tradeLogItems.add(TradeLogItem.buildFromTradeItem(tradeItem));
        }
        return tradeLogItems;
    }

    @Override
    public String getDataFileName() {
        return selfID.toString();
    }

    @Override
    public File getSaveFile() {
        return FileUtils.getOrCreateFile(FileUtils.getOrCreateFile(FileUtils.ROOT_FOLDER, "tradelog"), "heavyData");
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onSave() {

    }
}
