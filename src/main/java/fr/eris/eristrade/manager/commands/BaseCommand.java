package fr.eris.eristrade.manager.commands;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseCommand extends BukkitCommand {

    @Getter private final boolean forPlayersOnly;

    public BaseCommand(String name) {
        this(name, new ArrayList<>());
    }
    public BaseCommand(String name, List<String> aliases) {
        this(name, aliases, false);
    }
    public BaseCommand(String name, List<String> aliases, boolean forPlayersOnly) {
        this(name, aliases, null, forPlayersOnly);
    }

    public BaseCommand(String name, List<String> aliases, String permision) {
        this(name, aliases, permision, false);
    }

    public BaseCommand(String name, String permision) {
        this(name, new ArrayList<>(), permision, false);
    }

    public BaseCommand(String name, boolean forPlayersOnly) {
        this(name, new ArrayList<>(), null, forPlayersOnly);
    }

    public BaseCommand(String name, List<String> aliases, String permission, boolean forPlayersOnly) {
        super(name);
        if(aliases == null) aliases = new ArrayList<>();
        this.setAliases(aliases);
        this.setPermission(permission);
        this.forPlayersOnly = forPlayersOnly;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(forPlayersOnly && checkIfConsoleSender(sender)) return true;
        return execute(sender, args);
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public boolean checkIfConsoleSender(CommandSender sender) {
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
}
