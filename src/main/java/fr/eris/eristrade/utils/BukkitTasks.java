package fr.eris.eristrade.utils;

import fr.eris.eristrade.ErisTrade;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class BukkitTasks {
    public static void sync(Callable callable) {
        Bukkit.getScheduler().runTask(ErisTrade.getInstance(), callable::call);
    }

    public static BukkitTask syncLater(Callable callable, long delay) {
        return Bukkit.getScheduler().runTaskLater(ErisTrade.getInstance(), callable::call, delay);
    }

    public static BukkitTask syncTimer(Callable callable, long delay, long value) {
        return Bukkit.getScheduler().runTaskTimer(ErisTrade.getInstance(), callable::call, delay, value);
    }

    public static void async(Callable callable) {
        Bukkit.getScheduler().runTaskAsynchronously(ErisTrade.getInstance(), callable::call);
    }

    public static BukkitTask asyncLater(Callable callable, long delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(ErisTrade.getInstance(), callable::call, delay);
    }

    public static BukkitTask asyncTimer(Callable callable, long delay, long value) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(ErisTrade.getInstance(), callable::call, delay, value);
    }


    public interface Callable {
        void call();
    }
}
