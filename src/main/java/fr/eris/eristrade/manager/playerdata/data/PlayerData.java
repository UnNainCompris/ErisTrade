package fr.eris.eristrade.manager.playerdata.data;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.trade.config.TradeConfig;
import fr.eris.eristrade.manager.trade.data.log.TradeSoftLogData;
import fr.eris.erisutils.utils.file.FileUtils;
import fr.eris.erisutils.utils.file.JsonUtils;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class PlayerData extends fr.eris.erisutils.manager.playerdata.data.PlayerData {

    @Getter private List<UUID> playerTradeSoftDataID; // Every trade id of the player (as TradeSoft to avoid too much data loading)

    public void addSoftTradeId(UUID newSoftTradeId) {
        if(playerTradeSoftDataID.size() >= ErisTrade.getConfigManager().getConfig(TradeConfig.class).getMaxLoggedTradePerPlayer().getValue()) {
            UUID lastSoftTradeId = playerTradeSoftDataID.remove(playerTradeSoftDataID.size() - 1);
            TradeSoftLogData lastSoftTradeData = ErisTrade.getTradeManager().getSoftTradeData(lastSoftTradeId);
            if(!(ErisTrade.getPlayerDataManager().getPlayerData(lastSoftTradeData.getRequestedPlayerUUID()).getPlayerTradeSoftDataID().contains(lastSoftTradeId) &&
                    ErisTrade.getPlayerDataManager().getPlayerData(lastSoftTradeData.getRequesterUUID()).getPlayerTradeSoftDataID().contains(lastSoftTradeId))) {
                JsonUtils.deleteJson(FileUtils.getOrCreateFile(FileUtils.getOrCreateFile(FileUtils.ROOT_FOLDER, "tradelog"), "softData"),
                        lastSoftTradeData.getDataFileName());
                JsonUtils.deleteJson(FileUtils.getOrCreateFile(FileUtils.getOrCreateFile(FileUtils.ROOT_FOLDER, "tradelog"), "heavyData"),
                        lastSoftTradeData.getHeavyTradeDataID().toString());
            }
        }
        playerTradeSoftDataID.add(0, newSoftTradeId);
    }

    public PlayerData(UUID playerUUID) {
        super(playerUUID);
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onSave() {

    }
}
