package fr.eris.eristrade.utils.item;

import de.tr7zw.changeme.nbtapi.NBTItem;
import fr.eris.eristrade.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private ItemStack item;
    private ItemMeta itemMeta;

    public ItemBuilder() {
        this(new ItemStack(Material.BEDROCK));
    }

    public ItemBuilder(ItemStack defaultItem) {
        this.item = defaultItem.clone();
    }

    public ItemBuilder setMaterial(Material material) {
        item.setType(material);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag... itemFlag) {
        itemMeta = item.getItemMeta();
        itemMeta.addItemFlags(itemFlag);
        item.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        itemMeta = item.getItemMeta();
        itemMeta.addEnchant(enchantment, level, true);
        item.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setDisplayName(String displayName) {
        itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ColorUtils.translate(displayName));
        item.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        if(lore == null || lore.length == 0) return this;
        itemMeta = item.getItemMeta();
        itemMeta.setLore(ColorUtils.translate(Arrays.asList(lore)));
        item.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if(lore == null || lore.size() == 0) return this;
        itemMeta = item.getItemMeta();
        itemMeta.setLore(ColorUtils.translate(lore));
        item.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        if(itemMeta == null) itemMeta = item.getItemMeta();
        itemMeta.spigot().setUnbreakable(unbreakable);
        item.setItemMeta(itemMeta);
        return this;
    }

    public ItemStack build() {
        return item;
    }

    public static ItemBuilder placeHolders(Material item, boolean glowing) {
        final ItemBuilder itemBuilder = new ItemBuilder().setMaterial(item).setDisplayName("").setAmount(1);
        if(glowing) itemBuilder.addEnchant(Enchantment.ARROW_DAMAGE, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
        return itemBuilder;
    }

    public static ItemBuilder placeHolders(Material item, short color, boolean glowing) {
        final ItemBuilder itemBuilder = new ItemBuilder().setMaterial(item).setDisplayName("").setAmount(1).setDurability(color);
        if(glowing) itemBuilder.addEnchant(Enchantment.ARROW_DAMAGE, 1).addItemFlag(ItemFlag.HIDE_ENCHANTS);
        return itemBuilder;
    }

    public ItemBuilder setDurability(short durability) {
        item.setDurability(durability);
        return this;
    }

    public ItemBuilder addLore(String... lore) {
        if(lore == null || lore.length == 0) return this;
        itemMeta = item.getItemMeta();

        final List<String> currentLore;
        if(itemMeta.hasLore()) currentLore = itemMeta.getLore();
        else currentLore = new ArrayList<>();
        currentLore.addAll(Arrays.asList(lore));

        itemMeta.setLore(ColorUtils.translate(currentLore));
        item.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setNbtValue(String key, Object value) {
        item = new NBTUtils(item).set(key, value).build();
        return this;
    }

    public ItemBuilder clearNbtValue(String key) {
        NBTItem tempNbt = new NBTItem(item);
        tempNbt.removeKey(key);
        item = tempNbt.getItem();
        return this;
    }
}
