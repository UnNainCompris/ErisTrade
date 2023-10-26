package fr.eris.eristrade.manager.trade.commands;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.commands.SubCommand;
import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.MessageBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeAskCommand extends SubCommand {
    public TradeAskCommand() {
        super("ask", "eristrade.trade.ask", true);
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
        } if(player.equals(target)) {
            player.sendMessage(ColorUtils.translate("&c[x] &7" + args[0] + " &7you can't trade yourself !"));
            return;
        } if(ErisTrade.getTradeManager().hasSendTradeRequest(player, target)) {
            player.sendMessage(ColorUtils.translate("&c[x] &7" + target.getDisplayName() + " &7is already invited to trade !"));
            return;
        }
        ErisTrade.getTradeManager().sendTradeRequest(player, target);
    }
}
