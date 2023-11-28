package fr.eris.eristrade.manager.trade.data.log;

import com.google.gson.annotations.Expose;
import fr.eris.eristrade.manager.trade.data.Trade;
import fr.eris.erisutils.utils.data.Data;
import fr.eris.erisutils.utils.file.FileUtils;
import fr.eris.erisutils.utils.storage.Tuple;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TradeSoftLogData extends Data {
    @Getter @Expose private UUID heavyTradeDataID;
    @Getter @Expose private UUID selfID;
    @Getter @Expose private LocalDate dateWhenTrade;
    @Getter @Expose private UUID requesterUUID, requestedPlayerUUID;
    @Getter @Setter @Expose private long timeViewed;
    @Getter @Expose private boolean isTradePublic;

    private TradeSoftLogData() {
    }

    public long getDaysPassed() {
        return ChronoUnit.DAYS.between(dateWhenTrade, LocalDate.now());
    }

    public static Tuple<TradeHeavyLogData, TradeSoftLogData> buildFromTrade(Trade trade) {
        TradeHeavyLogData tradeHeavyLogData = TradeHeavyLogData.buildFromTrade(trade);
        TradeSoftLogData tradeSoftLogData = new TradeSoftLogData();

        tradeSoftLogData.selfID = UUID.randomUUID();
        tradeSoftLogData.requesterUUID = trade.getFirstPlayer().getPlayer().getUniqueId();
        tradeSoftLogData.requestedPlayerUUID = trade.getSecondPlayer().getPlayer().getUniqueId();
        tradeSoftLogData.dateWhenTrade = LocalDate.now();

        tradeSoftLogData.heavyTradeDataID = tradeHeavyLogData.getSelfID();
        tradeSoftLogData.isTradePublic = trade.isPublic();
        tradeHeavyLogData.setTradeSoftDataID(tradeSoftLogData.selfID);

        return new Tuple<>(tradeHeavyLogData, tradeSoftLogData);
    }

    @Override
    public String getDataFileName() {
        return selfID.toString();
    }

    @Override
    public File getSaveFile() {
        return FileUtils.getOrCreateFile(FileUtils.getOrCreateFile(FileUtils.ROOT_FOLDER, "tradelog"), "softData");
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onSave() {

    }
}
