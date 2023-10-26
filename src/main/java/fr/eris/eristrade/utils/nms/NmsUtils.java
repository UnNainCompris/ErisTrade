package fr.eris.eristrade.utils.nms;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.UUID;

public class NmsUtils {
    public static CommandMap getCommandMap() {
        try {
            Field commandMapField = commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch(NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
