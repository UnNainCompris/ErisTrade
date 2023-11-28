package fr.eris.eristrade.manager.trade;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.trade.commands.TradeExecutor;
import fr.eris.eristrade.manager.trade.config.TradeConfig;
import fr.eris.eristrade.manager.trade.data.Trade;
import fr.eris.eristrade.manager.trade.data.log.TradeHeavyLogData;
import fr.eris.eristrade.manager.trade.data.log.TradeSoftLogData;
import fr.eris.eristrade.manager.trade.language.TradeLanguage;
import fr.eris.erisutils.ErisUtils;
import fr.eris.erisutils.manager.language.data.LanguagePlaceholder;
import fr.eris.erisutils.utils.MessageBuilder;
import fr.eris.erisutils.utils.bukkit.BukkitTasks;
import fr.eris.erisutils.utils.error.exception.ErisPluginException;
import fr.eris.erisutils.utils.file.FileUtils;
import fr.eris.erisutils.utils.file.JsonUtils;
import fr.eris.erisutils.utils.manager.Manager;
import fr.eris.erisutils.utils.storage.Tuple;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TradeManager extends Manager {

    @Getter private final HashMap<Player, Tuple<Long, Player>> tradeRequestCache = new HashMap<>();
    @Getter private final HashMap<UUID, TradeSoftLogData> softTradeDataCache = new HashMap<>();
    @Getter private final HashMap<UUID, TradeHeavyLogData> heavyTradeDataCache = new HashMap<>();
    private final List<Trade> currentTrade = new ArrayList<>();

    private BukkitTask tradeRequestUpdaterTask;
    private long timeBeforeTradeRequestDelete = 30_000;

    private BukkitTask tradeCacheEmptier;

    public void emptyCache() {
        softTradeDataCache.clear();
        heavyTradeDataCache.clear();
    }

    public void start() {
        tradeRequestUpdaterTask = BukkitTasks.asyncTimer(this::tradeRequestUpdater, 3, 3);
        tradeCacheEmptier = BukkitTasks.asyncTimer(this::emptyCache, 36000, 36000); // 30 min
        try {
            ErisTrade.getConfigManager().loadConfig(TradeConfig.class);
        } catch (ErisPluginException erisPluginException) {
            erisPluginException.printStackTrace();
        }
        new TradeExecutor();
    }

    public void stop() {
        cancelAllTrade();
    }

    public void tradeRequestUpdater() {
        checkPlayerTrade();
        for(Player player : tradeRequestCache.keySet()) {
            Tuple<Long, Player> value = tradeRequestCache.get(player);
            if(System.currentTimeMillis() - value.getFirst() > timeBeforeTradeRequestDelete) {
                ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getSentTradeRequestCanceled().sendMessage(
                        player, LanguagePlaceholder.create("%target%", value.getSecond().getName()));
                ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getReceivedTradeRequestCanceled().sendMessage(
                        value.getSecond(), LanguagePlaceholder.create("%requester%", player.getName()));
                tradeRequestCache.remove(player);
            }
        }
    }

    public void cancelAllTrade() {
        for(Trade trade : new ArrayList<>(currentTrade)) {
            trade.cancelTrade();
            currentTrade.remove(trade);
        }
        tradeRequestCache.clear();
    }

    public void checkPlayerTrade() {
        List<Player> playerInTrade = new ArrayList<>();
        for(Trade trade : new ArrayList<>(currentTrade)) {
            if(playerInTrade.contains(trade.getFirstPlayer().getPlayer()) || playerInTrade.contains(trade.getSecondPlayer().getPlayer())) {
                trade.cancelTrade();
                currentTrade.remove(trade);
                return;
            }

            playerInTrade.add(trade.getFirstPlayer().getPlayer());
            playerInTrade.add(trade.getSecondPlayer().getPlayer());
        }
    }

    public void startTrade(Player requester, Player requested) {
        if(!checkPlayerWorld(requester, requested) || !checkPlayerDistance(requester, requested)) return;
        requester = Bukkit.getPlayer(requester.getUniqueId()); // use to update the player (avoid dupe)
        requested = Bukkit.getPlayer(requested.getUniqueId()); // use to update the player (avoid dupe)
        Trade trade = new Trade(requester, requested);
        currentTrade.add(trade);
        tradeRequestCache.remove(requester);
    }

    private boolean checkPlayerDistance(Player requester, Player requested) {
        boolean canTradeInDifferentWorld = ErisTrade.getConfigManager().getConfig(TradeConfig.class).getAllowDifferentWorldTrading().getValue();
        double maxDistance = ErisTrade.getConfigManager().getConfig(TradeConfig.class).getDistanceBetweenPlayer().getValue();

        if(requester.getWorld().equals(requested.getWorld())) {
            if(maxDistance == -1) return true;
            if(requester.getLocation().distance(requested.getLocation()) > maxDistance) {
                ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getPlayerTooFarAway().sendMessage(
                        requester, LanguagePlaceholder.create("%target%", requested.getName()));
                removeTradeRequest(requester, requested, false);
                return false;
            }
        }
        if(!requester.getWorld().equals(requested.getWorld()) && !canTradeInDifferentWorld) {
            if(maxDistance == -1) return true;
            ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getNotInSameWorld().sendMessage(
                    requester, LanguagePlaceholder.create("%target%", requested.getName()));
            removeTradeRequest(requester, requested, false);
            return false;
        }

        return true;
    }

    private boolean checkPlayerWorld(Player requester, Player requested) {
        boolean canTradeInDifferentWorld = ErisTrade.getConfigManager().getConfig(TradeConfig.class).getAllowDifferentWorldTrading().getValue();
        String disabledWorld = ErisTrade.getConfigManager().getConfig(TradeConfig.class).getDisabledInWorld().getValue();

        if(!requester.getWorld().equals(requested.getWorld()) && !canTradeInDifferentWorld) {
            ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getNotInSameWorld().sendMessage(
                    requester, LanguagePlaceholder.create("%target%", requested.getName()));
            removeTradeRequest(requester, requested, false);
            return false;
        }
        List<String> splitDisabledWorldName = Arrays.asList(disabledWorld.split(";"));
        if(splitDisabledWorldName.contains(requester.getWorld().getName())) {
            ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getInvalidSelfWorld().sendMessage(requester);
            removeTradeRequest(requester, requested, false);
            return false;
        } else if(splitDisabledWorldName.contains(requested.getWorld().getName())) {
            ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getInvalidTargetWorld().sendMessage(
                    requester, LanguagePlaceholder.create("%target%", requested.getName()));
            removeTradeRequest(requester, requested, false);
            return false;
        }

        return true;
    }

    public void sendTradeRequest(Player from, Player to) {
        if(hasSendTradeRequest(from, to)) {
            startTrade(from, to);
            return;
        } else if(hasSendTradeRequest(to, from)) {
            startTrade(to, from);
            return;
        }
        if(!checkPlayerWorld(from, to) || !checkPlayerDistance(from, to)) return;
        tradeRequestCache.put(from, new Tuple<>(System.currentTimeMillis(), to));
        ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getSendTradeRequest().sendMessage(
                from, LanguagePlaceholder.create("%target%", to.getName()));
        MessageBuilder message = MessageBuilder.builder().addText(ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class)
                        .getReceiveTradeRequest().parsePlaceholders(LanguagePlaceholder.create("%requester%", from.getName())) +  "\n\n")
                .addText("  &7|   ")
                .addClickEvent(ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getAcceptTradeRequestButton().getValue(), ClickEvent.Action.RUN_COMMAND, "/trade " + from.getName())
                .addText("    ")
                .addClickEvent(ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getCancelTradeRequestButton().getValue(), ClickEvent.Action.RUN_COMMAND, "/trade cancel " + from.getName())
                .addText("    &7|");
        message.sendMessage(to);
    }

    public boolean hasSendTradeRequest(Player player, Player target) {
        return target.equals(tradeRequestCache.getOrDefault(player, new Tuple<>(null, null)).getSecond());
    }

    public void removeTradeRequest(Player firstPlayer, Player secondPlayer, boolean silent) {
        Tuple<Player, Player> sortedPlayer = sortSenderRequested(firstPlayer, secondPlayer);
        Player requester = sortedPlayer.getFirst();
        Player requested = sortedPlayer.getSecond();

        if(requested != null && requester != null) {
            if(!silent) {
                ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getSentTradeRequestCanceled().sendMessage(
                        requester, LanguagePlaceholder.create("%target%", requested.getName()));
                ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getReceivedTradeRequestCanceled().sendMessage(
                        requested, LanguagePlaceholder.create("%requester%", requester.getName()));
            }
            tradeRequestCache.remove(requester);
        }
    }

    public Tuple<Player, Player> sortSenderRequested(Player firstPlayer, Player secondPlayer) {
        Tuple<Player, Player> sortedPlayer = new Tuple<>(null, null);
        if(hasSendTradeRequest(firstPlayer, secondPlayer)) {
            sortedPlayer.setFirst(firstPlayer);
            sortedPlayer.setSecond(secondPlayer);
        } else if(hasSendTradeRequest(secondPlayer, firstPlayer)) {
            sortedPlayer.setFirst(secondPlayer);
            sortedPlayer.setSecond(firstPlayer);
        }
        return sortedPlayer;
    }

    public void removeTrade(Trade trade) {
        currentTrade.remove(trade);
    }

    public void logTrade(Trade tradeToLog) {
        Tuple<TradeHeavyLogData, TradeSoftLogData> dataTuple = TradeSoftLogData.buildFromTrade(tradeToLog);
        TradeHeavyLogData tradeHeavyLogData = dataTuple.getFirst();
        TradeSoftLogData tradeSoftLogData = dataTuple.getSecond();
        tradeHeavyLogData.save();
        tradeSoftLogData.save();

        ErisTrade.getPlayerDataManager().getPlayerData(tradeToLog.getFirstPlayer().getPlayer().getUniqueId()).addSoftTradeId(tradeSoftLogData.getSelfID());
        ErisTrade.getPlayerDataManager().getPlayerData(tradeToLog.getSecondPlayer().getPlayer().getUniqueId()).addSoftTradeId(tradeSoftLogData.getSelfID());
    }

    public TradeSoftLogData getSoftTradeData(UUID softTradeDataId) {
        if(softTradeDataCache.containsKey(softTradeDataId))
            return softTradeDataCache.get(softTradeDataId);
        if(!JsonUtils.isExist(FileUtils.getOrCreateFile(FileUtils.getOrCreateFile(FileUtils.ROOT_FOLDER, "tradelog"),
                "softData"), softTradeDataId.toString())) return null;
        TradeSoftLogData tradeSoftLogData = JsonUtils.getData(JsonUtils.getOrCreateJson(FileUtils.getOrCreateFile(
                        FileUtils.getOrCreateFile(FileUtils.ROOT_FOLDER, "tradelog"), "softData"), softTradeDataId.toString()),
                TradeSoftLogData.class);
        if(tradeSoftLogData.getDaysPassed() >= ErisTrade.getConfigManager().getConfig(TradeConfig.class).getAutoDeleteTradeLog().getValue()) {
            ErisTrade.getPlayerDataManager().getPlayerData(tradeSoftLogData.getRequesterUUID()).getPlayerTradeSoftDataID().remove(softTradeDataId);
            ErisTrade.getPlayerDataManager().getPlayerData(tradeSoftLogData.getRequestedPlayerUUID()).getPlayerTradeSoftDataID().remove(softTradeDataId);
            JsonUtils.deleteJson(FileUtils.getOrCreateFile(FileUtils.getOrCreateFile(FileUtils.ROOT_FOLDER, "tradelog"), "softData"),
                    tradeSoftLogData.getSelfID().toString());
            JsonUtils.deleteJson(FileUtils.getOrCreateFile(FileUtils.getOrCreateFile(FileUtils.ROOT_FOLDER, "tradelog"), "heavyData"),
                    tradeSoftLogData.getHeavyTradeDataID().toString());
            return null;
        }
        softTradeDataCache.put(softTradeDataId, tradeSoftLogData);
        return tradeSoftLogData;
    }

    public TradeHeavyLogData getHeavyTradeData(UUID heavyTradeDataId) {
        if(heavyTradeDataCache.containsKey(heavyTradeDataId))
            return heavyTradeDataCache.get(heavyTradeDataId);
        if(!JsonUtils.isExist(FileUtils.getOrCreateFile(FileUtils.getOrCreateFile(FileUtils.ROOT_FOLDER, "tradelog"),
                "heavyData"), heavyTradeDataId.toString())) return null;
        TradeHeavyLogData tradeHeavyLogData = JsonUtils.getData(JsonUtils.getOrCreateJson(FileUtils.getOrCreateFile(
                FileUtils.getOrCreateFile(FileUtils.ROOT_FOLDER, "tradelog"), "heavyData"), heavyTradeDataId.toString()),
                TradeHeavyLogData.class);
        heavyTradeDataCache.put(heavyTradeDataId, tradeHeavyLogData);
        return tradeHeavyLogData;
    }
}
