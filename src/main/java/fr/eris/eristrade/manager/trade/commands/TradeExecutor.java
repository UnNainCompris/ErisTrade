package fr.eris.eristrade.manager.trade.commands;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.trade.language.TradeLanguage;
import fr.eris.erisutils.ErisUtils;
import fr.eris.erisutils.manager.commands.ErisSubCommand;
import fr.eris.erisutils.manager.commands.ErisSubCommandExecutor;
import fr.eris.erisutils.manager.commands.args.CommandArgument;
import fr.eris.erisutils.manager.commands.args.StringCommandArgument;
import fr.eris.erisutils.manager.commands.language.CommandLanguage;
import fr.eris.erisutils.manager.language.data.LanguagePlaceholder;
import fr.eris.erisutils.utils.bukkit.ColorUtils;
import fr.eris.erisutils.utils.bukkit.PlayerUtils;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class TradeExecutor extends ErisSubCommandExecutor {

    public TradeExecutor() {
        super("trade", null, ErisTrade.getConfiguration().permissionPrefix() + ".trade",
                true, false, true);
    }

    @Override
    public @NonNull List<CommandArgument<?>> registerCommandArgument() {
        return Collections.singletonList(new StringCommandArgument((args) -> PlayerUtils.getAllPlayerName(), true, false));
    }

    @Override
    public void error(CommandSender sender, CommandExecutionError errorCode, String[] argsValue, String targetedArgs,
                      CommandArgument<?> targetedCommandArguments) {
        if(errorCode == CommandExecutionError.NOT_ENOUGH_ARGS) {
            ErisUtils.getPluginLanguageManager().getLanguage(CommandLanguage.class).getMissingArgument().sendMessage(sender);
        } else if(errorCode == CommandExecutionError.INVALID_ARGS) {
            ErisUtils.getPluginLanguageManager().getLanguage(CommandLanguage.class).getInvalidArgument().sendMessage(sender);
        }
    }

    @Override
    public List<ErisSubCommand> registerSubCommands() {
        return Arrays.asList(new TradeCancelCommand());
    }

    @Override
    public void defaultExecute(CommandSender sender, List<CommandArgument<?>> args) {
        Player player = (Player) sender;
        Player target;
        if(args == null || args.isEmpty()) {
            ErisUtils.getPluginLanguageManager().getLanguage(CommandLanguage.class).getMissingArgument().sendMessage(player);
            return;
        }
        if((target = Bukkit.getPlayer(args.get(0).convert(String.class).getValue())) == null) {
            ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getTargetNotConnected().sendMessage(player,
                    LanguagePlaceholder.create("%target%", args.get(0).convert(String.class).getValue()));
            return;
        }
        if(player.equals(target)) {
            ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getCannotTradeYourself().sendMessage(player);
            return;
        } if(ErisTrade.getTradeManager().hasSendTradeRequest(player, target)) {
            ErisUtils.getPluginLanguageManager().getLanguage(TradeLanguage.class).getAlreadyInvitedToTrade().sendMessage(player,
                    LanguagePlaceholder.create("%target%", target.getName()));
            return;
        }
        ErisTrade.getTradeManager().sendTradeRequest(player, target);
    }
}
