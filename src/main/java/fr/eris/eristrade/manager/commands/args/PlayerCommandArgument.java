package fr.eris.eristrade.manager.commands.args;

import fr.eris.eristrade.utils.GetValue;
import fr.eris.eristrade.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerCommandArgument extends CommandArgument<Player> {

    public PlayerCommandArgument(boolean canBeNull) {
        super(canBeNull);
    }

    public PlayerCommandArgument(GetValue<List<String>> customInput, boolean onlyCustomInput) {
        super(customInput, onlyCustomInput);
    }

    public PlayerCommandArgument(GetValue<List<String>> customInput, boolean onlyCustomInput, boolean canBeNull) {
        super(customInput, onlyCustomInput, canBeNull);
    }

    @Override
    public boolean isValid(String input) {
        return PlayerUtils.isPlayer(input);
    }

    @Override
    public Player convertArgument(String input) {
        System.out.println(input + " -- " + Bukkit.getPlayer(input));
        return PlayerUtils.toPlayer(input);
    }
}
