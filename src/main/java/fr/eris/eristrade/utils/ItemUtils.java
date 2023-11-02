package fr.eris.eristrade.utils;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemUtils {

    public static ItemStack AIR = new ItemStack(Material.AIR);

    public static ItemStack createItem(Material material, String name, int amount, List<String> lore, List<ItemFlag> itemFlags) {
        ItemStack item = new ItemStack(material, amount == 0 ? 1 : amount);
        ItemMeta itemMeta = item.getItemMeta();

        if(name != null)
            itemMeta.setDisplayName(ColorUtils.translate(name));
        if(lore != null)
            itemMeta.setLore(ColorUtils.translate(lore));
        if(itemFlags != null)
            for(ItemFlag flag : itemFlags)
                itemMeta.addItemFlags(flag);

        item.setItemMeta(itemMeta);
        return item;
    }
    public static ItemStack setLore(ItemStack item, List<String> lore) {
        List<String> realLore = new ArrayList<>();
        for(String value : lore) if(value != null) realLore.add(value);

        if(item.getItemMeta() != null) {
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setLore(ColorUtils.translate(realLore));
            item.setItemMeta(itemMeta);
        }
        return item;
    }
    public static ItemStack createItem(ItemStack item, String name, List<String> lore, List<ItemFlag> itemFlags) {
        ItemMeta itemMeta = item.getItemMeta();

        if(name != null)
            itemMeta.setDisplayName(ColorUtils.translate(name));
        if(lore != null)
            itemMeta.setLore(ColorUtils.translate(lore));
        if(itemFlags != null)
            for(ItemFlag flag : itemFlags)
                itemMeta.addItemFlags(flag);

        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack addEnchantment(ItemStack itemStack, Enchantment enchantment, int value, boolean overPassedLimit) {
        if (overPassedLimit) itemStack.addUnsafeEnchantment(enchantment, value);
        else itemStack.addEnchantment(enchantment, value);
        return itemStack;
    }

    public static ItemStack placeHolders(Material item, boolean glow) {
        if(glow) return addEnchantment(createItem(new ItemStack(item, 1), " ", null, Collections.singletonList(ItemFlag.HIDE_ENCHANTS)), Enchantment.LURE, 1, true);
        else return createItem(new ItemStack(item, 1), " ", null, null);
    }

    public static ItemStack infoItem(Material mat, String thingToSay, int splitEveryChar) {
        final List<String> lore = new ArrayList<>();
        for(String message : StringUtils.prettySplit(thingToSay, true, splitEveryChar)) {
            lore.add("&7" + message);
        }
        return createItem(mat, "&fInformation: ", 1, lore, null);
    }
    public static ItemStack infoItem(Material mat,String itemName, String thingToSay, int splitEveryChar) {
        return infoItem(mat, itemName, thingToSay, splitEveryChar, false);
    }

    public static ItemStack infoItem(Material mat,String itemName, String thingToSay, int splitEveryChar, boolean glow) {
        final List<String> lore = new ArrayList<>();
        for(String message : StringUtils.prettySplit(thingToSay, true, splitEveryChar)) {
            lore.add("&7" + message);
        }
        if(glow)
            return addEnchantment(createItem(mat, itemName, 1, lore, Collections.singletonList(ItemFlag.HIDE_ENCHANTS)), Enchantment.LURE, 1, true);
        else
            return createItem(mat, itemName, 1, lore, null);
    }

    public static ItemStack infoItem(Material mat, String thingToSay) {
        return infoItem(mat, thingToSay, 25);
    }

    public static ItemStack infoItem(String thingToSay) {
        return infoItem(Material.PAPER, thingToSay, 25);
    }

    public static String getItemName(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        return ColorUtils.strip(nmsStack.getItem().a(nmsStack));
    }
}
