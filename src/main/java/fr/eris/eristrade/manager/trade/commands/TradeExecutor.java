package fr.eris.eristrade.manager.trade.commands;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.commands.ErisSubCommand;
import fr.eris.eristrade.manager.commands.ErisSubCommandExecutor;
import fr.eris.eristrade.manager.commands.args.CommandArgument;
import fr.eris.eristrade.manager.commands.args.PlayerCommandArgument;
import fr.eris.eristrade.manager.commands.args.StringCommandArgument;
import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.PlayerUtils;
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
            sender.sendMessage(ColorUtils.translate("&c[x] &7Missing argument !"));
            return;
        } else if(errorCode == CommandExecutionError.INVALID_ARGS) {
            sender.sendMessage(ColorUtils.translate("&c[x] &7Invalid argument !"));
            return;
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
            player.sendMessage(ColorUtils.translate("&c[x] &7Missing argument !"));
            return;
        }
        if((target = Bukkit.getPlayer(args.get(0).convert(String.class).getValue())) == null) {
            player.sendMessage(ColorUtils.translate("&c[x] &7The player " + args.get(0).convert(String.class).getValue() + " is not connected !"));
            return;
        }
        if(player.equals(target)) {
            player.sendMessage(ColorUtils.translate("&c[x] &7You can't trade with yourself !"));
            return;
        } if(ErisTrade.getTradeManager().hasSendTradeRequest(player, target)) {
            player.sendMessage(ColorUtils.translate("&c[x] &7" + target.getDisplayName() + " &7is already invited to trade !"));
            return;
        }
        System.out.println(player.getName() + " -- " + target.getName());
        ErisTrade.getTradeManager().sendTradeRequest(player, target);
    }
}
