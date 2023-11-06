package fr.eris.eristrade.manager.trade;

import fr.eris.eristrade.manager.trade.commands.TradeExecutor;
import fr.eris.eristrade.manager.trade.data.Trade;
import fr.eris.eristrade.utils.BukkitTasks;
import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.MessageBuilder;
import fr.eris.eristrade.utils.manager.Manager;
import fr.eris.eristrade.utils.storage.Tuple;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TradeManager extends Manager {

    @Getter private final HashMap<Player, Tuple<Long, Player>> tradeRequestCache = new HashMap<>();
    private final List<Trade> currentTrade = new ArrayList<>();

    private BukkitTask tradeRequestUpdaterTask;
    private long timeBeforeTradeRequestDelete = 30_000;

    public void start() {
        tradeRequestUpdaterTask = BukkitTasks.asyncTimer(this::tradeRequestUpdater, 3, 3);
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
                player.sendMessage(ColorUtils.translate("&7The trade request send to &6" + value.getSecond().getName() + " &7was canceled !"));
                value.getSecond().sendMessage(ColorUtils.translate("&7The trade request from &6" + player.getName() + " &7was canceled !"));
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
        requester = Bukkit.getPlayer(requester.getUniqueId()); // use to update the player (avoid dupe)
        requested = Bukkit.getPlayer(requested.getUniqueId()); // use to update the player (avoid dupe)
        Trade trade = new Trade(requester, requested);
        currentTrade.add(trade);
        tradeRequestCache.remove(requester);
    }

    public void sendTradeRequest(Player from, Player to) {
        if(hasSendTradeRequest(from, to)) {
            startTrade(from, to);
            return;
        } else if(hasSendTradeRequest(to, from)) {
            startTrade(to, from);
            return;
        }
        tradeRequestCache.put(from, new Tuple<>(System.currentTimeMillis(), to));
        from.sendMessage(ColorUtils.translate("&7You send a trade request to " + to.getName() + " !"));
        MessageBuilder message = MessageBuilder.builder().addText("&7You get a trade request from " + from.getName() + " ! \n\n")
                .addText("  &7|   ")
                .addClickEvent("&a&l[ACCEPT]", ClickEvent.Action.RUN_COMMAND, "/trade " + from.getName())
                .addText("    ")
                .addClickEvent("&c&l[CANCEL]", ClickEvent.Action.RUN_COMMAND, "/trade cancel " + from.getName())
                .addText("    &7|");
        message.sendMessage(to);
    }

    public boolean hasSendTradeRequest(Player player, Player target) {
        return target.equals(tradeRequestCache.getOrDefault(player, new Tuple<>(null, null)).getSecond());
    }

    public void removeTradeRequest(Player firstPlayer, Player secondPlayer) {
        Tuple<Player, Player> sortedPlayer = sortSenderRequested(firstPlayer, secondPlayer);
        Player requester = sortedPlayer.getFirst();
        Player requested = sortedPlayer.getSecond();

        if(requested != null && requester != null) {
            requester.sendMessage(ColorUtils.translate("&7The trade request you sent to &6" + requested.getName() + " &7was canceled !"));
            requested.sendMessage(ColorUtils.translate("&7The trade request from &6" + requester.getName() + " &7was canceled !"));
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
}
