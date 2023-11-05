package fr.eris.eristrade.utils.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Use to load only 1 time certain item
 */
public class ItemCache {

    public static final ItemStack placeHolderGlowing = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.GRAY, true).build();
    public static final ItemStack placeHolder = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.GRAY, false).build();

    public static final ItemStack confirm = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.GREEN, true).setDisplayName("&2Confirm").build();
    public static final ItemStack cancel = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.RED, true).setDisplayName("&cCancel").build();
    public static final ItemStack back = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.RED, false).setDisplayName("&cBack").build();

    public static final ItemStack nextPage = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.GREEN, true).setDisplayName("&2Next Page").build();
    public static final ItemStack previousPage = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.RED, false).setDisplayName("&cPrevious Page").build();

    public static final ItemStack plus1 = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.LIME, false).setDisplayName("&a+1").build();
    public static final ItemStack plus10 = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.LIME, false).setDisplayName("&a+10").build();
    public static final ItemStack plus100 = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.LIME, false).setDisplayName("&a+100").build();

    public static final ItemStack minus1 = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.RED, false).setDisplayName("&c-1").build();
    public static final ItemStack minus10 = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.RED, false).setDisplayName("&c-10").build();
    public static final ItemStack minus100 = ItemBuilder.placeHolders(Material.STAINED_GLASS_PANE, ItemColor.RED, false).setDisplayName("&c-100").build();

    public static final ItemStack finish = new ItemBuilder().setMaterial(Material.BEDROCK).setDisplayName("&7Finish").build();
    public static final ItemStack fromChat = new ItemBuilder().setMaterial(Material.SIGN).setDisplayName("&7Set from chat").build();

    public static class ItemColor {
        public static final short WHITE = 0;
        public static final short ORANGE = 1;
        public static final short MAGENTA = 2;
        public static final short LIGHT_BLUE = 3;
        public static final short YELLOW = 4;
        public static final short LIME = 5;
        public static final short PINK = 6;
        public static final short GRAY = 7;
        public static final short LIGHT_GRAY = 8;
        public static final short CYAN = 9;
        public static final short PURPLE = 10;
        public static final short BLUE = 11;
        public static final short BROWN = 12;
        public static final short GREEN = 13;
        public static final short RED = 14;
        public static final short BLACK = 15;
    }
}
