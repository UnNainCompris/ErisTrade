package fr.eris.eristrade.utils.interactiveasker;

import fr.eris.eristrade.utils.BukkitTasks;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.Consumer;

public class StringAsker extends InteractiveAsker<String> {
    public StringAsker(Player player, String askReason, Consumer<String> action) {
        super(player, askReason, "", null, action, false);
    }

    @EventHandler
    public void playerChatEvent(AsyncPlayerChatEvent event) {
        if(!event.getPlayer().equals(this.player) || finished) return;
        event.setCancelled(true);
        this.value = event.getMessage();
        BukkitTasks.sync(this::end);
    }
}
