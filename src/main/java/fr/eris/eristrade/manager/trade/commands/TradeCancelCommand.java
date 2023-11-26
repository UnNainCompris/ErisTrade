package fr.eris.eristrade.manager.trade.commands;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.trade.language.TradeLanguage;
import fr.eris.erisutils.ErisUtils;
import fr.eris.erisutils.manager.commands.ErisCommand;
import fr.eris.erisutils.manager.commands.ErisSubCommand;
import fr.eris.erisutils.manager.commands.args.CommandArgument;
import fr.eris.erisutils.manager.commands.args.PlayerCommandArgument;
import fr.eris.erisutils.manager.commands.language.CommandLanguage;
import fr.eris.erisutils.manager.language.data.LanguagePlaceholder;
import fr.eris.erisutils.utils.storage.Tuple;
import lombok.NonNull;
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
            ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getTargetDoesntSendRequest().sendMessage(player,
                    LanguagePlaceholder.create("%target%", target.getName()));
            return;
        }
        ErisTrade.getTradeManager().removeTradeRequest(player, target, false);
    }

    @Override
    public void error(CommandSender sender, ErisCommand.CommandExecutionError errorCode, String[] argsValue, String targetedArgs, CommandArgument<?> targetedCommandArguments) {
        if(errorCode == ErisCommand.CommandExecutionError.INVALID_ARGS) {
            if (targetedCommandArguments instanceof PlayerCommandArgument) {
                ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getTargetDoesntSendRequest().sendMessage(sender,
                        LanguagePlaceholder.create("%target%", targetedArgs));
                return;
            }
        } if(errorCode == ErisCommand.CommandExecutionError.NOT_ENOUGH_ARGS) {
            if (targetedCommandArguments instanceof PlayerCommandArgument) {
                ErisUtils.getPluginLanguageManager().getLanguage(CommandLanguage.class).getMissingArgument().sendMessage(sender);
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
