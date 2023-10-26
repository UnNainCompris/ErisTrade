package fr.eris.eristrade.utils.interactiveasker;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class InteractiveAskerUtils {

    public static void askFromType(Class<?> needType, Player player, String askReason, Consumer<?> todo) {
        if(needType.isInstance(String.class))
            new StringAsker(player, askReason, (Consumer<String>) todo);
        else if(needType.isInstance(Number.class))
            new NumberAsker(player, askReason, (Consumer<Number>) todo);
    }
}
