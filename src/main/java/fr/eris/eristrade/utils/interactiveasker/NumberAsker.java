package fr.eris.eristrade.utils.interactiveasker;

import fr.eris.eristrade.utils.ColorUtils;
import fr.eris.eristrade.utils.InventoryUtils;
import fr.eris.eristrade.utils.StringUtils;
import fr.eris.eristrade.utils.item.ClickableItem;
import fr.eris.eristrade.utils.item.ItemCache;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class NumberAsker extends InteractiveAsker<Number> {
    public NumberAsker(Player player, String askReason, Consumer<Number> action) {
        super(player, askReason, 0, "&6[Number] &7" + askReason, action, true);
    }

    @Override
    protected void initInventory() {
        InventoryUtils.aroundInventory(askInventory.getInventory(), ItemCache.placeHolder);
        setItem(new ClickableItem(() -> ItemCache.plus1, (event) -> value = value.longValue() + 1), 21);
        setItem(new ClickableItem(() -> ItemCache.plus10, (event) -> value = value.longValue() + 10), 22);
        setItem(new ClickableItem(() -> ItemCache.plus100, (event) -> value = value.longValue() + 100), 23);

        setItem(new ClickableItem(() -> ItemCache.minus1, (event) -> value = value.longValue() - 1), 30);
        setItem(new ClickableItem(() -> ItemCache.minus10, (event) -> value = value.longValue() - 10), 31);
        setItem(new ClickableItem(() -> ItemCache.minus100, (event) -> value = value.longValue() - 100), 32);

        setItem(new ClickableItem(() -> ItemCache.fromChat, event -> {
            canClose = true;
            new StringAsker(player, "&7Put a number in the chat to set it as current number",
            respond -> {
                askInventory.update(player);
                if(StringUtils.isInteger(respond))
                    value = Integer.parseInt(respond);
                else player.sendMessage(ColorUtils.translate("&7Invalid number : &6" + respond));
            });
        }), 37);

        setItem(new ClickableItem(() -> ItemCache.finish, (event) -> end()), 43);
    }
}
