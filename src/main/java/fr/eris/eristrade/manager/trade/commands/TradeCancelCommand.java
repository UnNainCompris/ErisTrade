package fr.eris.eristrade.manager.trade.commands;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.commands.SubCommand;
import fr.eris.eristrade.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeCancelCommand extends SubCommand {
    public TradeCancelCommand() {
        super("cancel", "eristrade.trade.cancel", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Player target = null;
        if(args == null || args.length == 0) {
            player.sendMessage(ColorUtils.translate("&c[x] &7Missing argument !"));
            return;
        } if((target = Bukkit.getPlayer(args[0])) == null) {
            player.sendMessage(ColorUtils.translate("&c[x] &7" + args[0] + " &7is not online !"));
            return;
        } if(!ErisTrade.getTradeManager().hasSendTradeRequest(player, target)) {
            player.sendMessage(ColorUtils.translate("&c[x] &7" + target.getDisplayName() + " &7is already not invited to trade !"));
            return;
        }
        ErisTrade.getTradeManager().removeTradeRequest(player, target);
    }
}
