package fr.eris.eristrade.manager.commands;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.utils.manager.Manager;
import fr.eris.eristrade.utils.nms.NmsUtils;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;

public class CommandManager extends Manager {
    private final CommandMap commandMap;

    public CommandManager() {
        commandMap = NmsUtils.getCommandMap();
        registersCommands();
    }

    private void registersCommands() {

    }

    public void registerCommand(BukkitCommand command) {
        commandMap.register(ErisTrade.getConfiguration().name().toLowerCase(), command);
    }
}
