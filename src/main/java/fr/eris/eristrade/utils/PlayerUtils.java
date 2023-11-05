package fr.eris.eristrade.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerUtils {

    public static boolean isPlayer(String playerName) {
        return Bukkit.getPlayer(playerName) != null;
    }

    public static boolean isPlayer(UUID playerUUID) {
        return Bukkit.getPlayer(playerUUID) != null;
    }

    public static boolean isOfflinePlayer(UUID playerUUID) {
        return Bukkit.getOfflinePlayer(playerUUID) != null;
    }

    public static Player toPlayer(String playerName) {
        return Bukkit.getPlayer(playerName);
    }

    public static Player toPlayer(UUID playerUUID) {
        return Bukkit.getPlayer(playerUUID);
    }

    public static OfflinePlayer toOfflinePlayer(UUID playerUUID) {
        return Bukkit.getOfflinePlayer(playerUUID);
    }

    public static List<String> getAllPlayerName() {
        List<String> playersName = new ArrayList<>();
        for(Player player : Bukkit.getOnlinePlayers()) {
            playersName.add(player.getName());
        }
        return playersName;
    }
}
