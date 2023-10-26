package fr.eris.eristrade.manager.commands;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public abstract class SubCommand {

    protected final String name;
    protected final List<String> aliases;
    protected final String permission;

    @Setter protected String prefix;
    @Setter private boolean executeAsync;

    protected final boolean forPlayersOnly;

    public SubCommand(String name) {
        this(name, new ArrayList<>());
    }

    public SubCommand(String name, List<String> aliases) {
        this(name, aliases, null);
    }

    public SubCommand(String name, boolean forPlayersOnly) {
        this(name, new ArrayList<>(), forPlayersOnly);
    }

    public SubCommand(String name, String permission) {
        this(name, new ArrayList<>(), permission);
    }

    public SubCommand(String name, List<String> aliases, String permission) {
        this(name, aliases, permission, false);
    }

    public SubCommand(String name, String permission, boolean forPlayersOnly) {
        this(name, new ArrayList<>(), permission, forPlayersOnly);
    }

    public SubCommand(String name, List<String> aliases, boolean forPlayersOnly) {
        this(name, aliases, null, forPlayersOnly);
    }

    public SubCommand(String name, List<String> aliases, String permission, boolean forPlayersOnly) {
        this.name = name;
        this.aliases = aliases;
        this.permission = permission;
        this.forPlayersOnly = forPlayersOnly;
    }

    protected boolean checkConsoleSender(CommandSender sender) {
        return sender instanceof ConsoleCommandSender;
    }

    public Player getPlayer(Object id) {
        try {
            if (id instanceof UUID)
                return Bukkit.getPlayer(UUID.fromString(id.toString()));
            return Bukkit.getPlayer(id.toString());
        } catch (Exception ex) {
            return null;
        }
    }

    public OfflinePlayer checkOfflinePlayer(Object id) {
        try {
            if (id instanceof UUID)
                return Bukkit.getOfflinePlayer(UUID.fromString(id.toString()));
            return Bukkit.getOfflinePlayer(id.toString());
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean isPlayerOnline(Object id) {
        if(getPlayer(id) != null) return true;
        OfflinePlayer player = checkOfflinePlayer(id);
        return player != null && player.isOnline();
    }
    public final void preExecute(CommandSender sender, String[] args) {
        if(forPlayersOnly && checkConsoleSender(sender))
            return;
        execute(sender, args);
    }
    public abstract void execute(CommandSender sender, String[] args);
}
