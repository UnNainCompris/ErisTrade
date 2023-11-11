package fr.eris.eristrade.manager.trade.data.log;

import com.google.gson.annotations.Expose;
import fr.eris.eristrade.manager.trade.data.Trade;
import fr.eris.eristrade.manager.trade.data.TradeData;
import fr.eris.eristrade.manager.trade.data.TradeItem;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeLogData {
    @Getter @Expose private UUID tradeLogID;

    @Getter @Expose private UUID firstPlayerUUID, secondPlayerUUID;
    @Getter @Expose private double firstPlayerTradedMoney, secondPlayerTradedMoney;
    @Getter @Expose private int firstPlayerTradedExperience, secondPlayerTradedExperience;
    @Getter @Expose private List<TradeLogItem> firstPlayerTradedItem, secondPlayerTradedItem;


    public static TradeLogData buildFromTrade(Trade trade) {
        TradeLogData tradeLogData = new TradeLogData();
        tradeLogData.tradeLogID = UUID.randomUUID();
        TradeData firstPlayerData = trade.getFirstPlayer(), secondPlayerData = trade.getSecondPlayer();

        tradeLogData.firstPlayerUUID = firstPlayerData.getPlayer().getUniqueId();
        tradeLogData.secondPlayerUUID = secondPlayerData.getPlayer().getUniqueId();

        tradeLogData.firstPlayerTradedMoney = firstPlayerData.getTradedMoney();
        tradeLogData.secondPlayerTradedMoney = secondPlayerData.getTradedMoney();

        tradeLogData.firstPlayerTradedExperience = firstPlayerData.getTradedExperience();
        tradeLogData.secondPlayerTradedExperience = secondPlayerData.getTradedExperience();

        tradeLogData.firstPlayerTradedItem = tradeLogData.parseTradeItemToLogItem(firstPlayerData.getCurrentTradedItem());
        tradeLogData.secondPlayerTradedItem = tradeLogData.parseTradeItemToLogItem(secondPlayerData.getCurrentTradedItem());
        return tradeLogData;
    }

    private List<TradeLogItem> parseTradeItemToLogItem(List<TradeItem> tradeItemList) {
        List<TradeLogItem> tradeLogItems = new ArrayList<>();
        for(TradeItem tradeItem : tradeItemList) {
            tradeLogItems.add(TradeLogItem.buildFromTradeItem(tradeItem));
        }
        return tradeLogItems;
    }
}
