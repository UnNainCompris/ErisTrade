package fr.eris.eristrade.manager.trade.commands;

import fr.eris.eristrade.ErisTrade;
import fr.eris.erisutils.manager.commands.ErisCommand;
import fr.eris.erisutils.manager.commands.ErisSubCommand;
import fr.eris.erisutils.manager.commands.args.CommandArgument;
import fr.eris.erisutils.manager.commands.args.PlayerCommandArgument;
import fr.eris.erisutils.utils.bukkit.ColorUtils;
import fr.eris.erisutils.utils.storage.Tuple;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TradeCancelCommand extends ErisSubCommand {
    public TradeCancelCommand() {
        super("cancel", null, ErisTrade.getConfiguration().permissionPrefix() + ".trade.cancel",
                true, false);
    }

    @Override
    public void execute(CommandSender sender, List<CommandArgument<?>> args) {
        Player player = (Player) sender;
        Player target = args.get(0).convert(Player.class).getValue();
        if(!ErisTrade.getTradeManager().hasSendTradeRequest(player, target) && !ErisTrade.getTradeManager().hasSendTradeRequest(target, player)) {
            sender.sendMessage(ColorUtils.translate("&c[x] &7" + target.getName() + " &7doesn't send you (or you sent to him) a trade request !!"));
            return;
        }
        ErisTrade.getTradeManager().removeTradeRequest(player, target);
    }

    @Override
    public void error(CommandSender sender, ErisCommand.CommandExecutionError errorCode, String[] argsValue, String targetedArgs, CommandArgument<?> targetedCommandArguments) {
        if(errorCode == ErisCommand.CommandExecutionError.INVALID_ARGS) {
            if (targetedCommandArguments instanceof PlayerCommandArgument) {
                sender.sendMessage(ColorUtils.translate("&c[x] &7" + targetedArgs + " &7doesn't send you (or you sent to him) a trade request !"));
                return;
            }
        } if(errorCode == ErisCommand.CommandExecutionError.NOT_ENOUGH_ARGS) {
            if (targetedCommandArguments instanceof PlayerCommandArgument) {
                sender.sendMessage(ColorUtils.translate("&c[x] &7Missing argument !"));
                return;
            }
        }
    }

    @Override
    public @NonNull List<CommandArgument<?>> registerCommandArgument() {
        return Collections.singletonList(new PlayerCommandArgument((args) -> getAllPossibleCancelableTrade((CommandSender) args[0]), true, false));
    }

    public List<String> getAllPossibleCancelableTrade(CommandSender sender) {
        List<String> possibilityList = new ArrayList<>();
        if(!(sender instanceof Player)) return possibilityList;
        Player player = (Player) sender;
        HashMap<Player, Tuple<Long, Player>> tradeRequestCache = ErisTrade.getTradeManager().getTradeRequestCache();
        if(tradeRequestCache.containsKey(player))
            possibilityList.add(tradeRequestCache.get(player).getSecond().getName());

        for(Player key : tradeRequestCache.keySet()) {
            if(tradeRequestCache.get(key).getSecond().equals(player))
                possibilityList.add(key.getName());
        }

        return possibilityList;
    }
}
