package fr.eris.eristrade.utils.handler;

import fr.eris.eristrade.ErisTrade;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class Handler implements Listener {

    public final void register() {
        Bukkit.getServer().getPluginManager().registerEvents(this, ErisTrade.getInstance());
    }

    public final void unregister() {
        HandlerList.unregisterAll(this);
    }

    public void start() {}
    public void stop() {}
}
