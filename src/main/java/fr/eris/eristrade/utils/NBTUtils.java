package fr.eris.eristrade.utils;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class NBTUtils {
  private final NBTItem item;
  
  public NBTUtils(ItemStack item) {
    this.item = new NBTItem(item);
  }
  
  public ItemStack build() {
    return this.item.getItem();
  }
  
  public NBTUtils set(String key, Object value) {
    if (value == null)
      return this; 
    if (value instanceof String) {
      this.item.setString(key, value.toString());
    } else if (value instanceof Boolean) {
      this.item.setBoolean(key, (Boolean)value);
    } else if (value instanceof Byte) {
      this.item.setByte(key, (Byte)value);
    } else if (value instanceof Double) {
      this.item.setDouble(key, (Double)value);
    } else if (value instanceof Float) {
      this.item.setFloat(key, (Float)value);
    } else if (value instanceof byte[]) {
      this.item.setByteArray(key, (byte[])value);
    } else if (value instanceof int[]) {
      this.item.setIntArray(key, (int[])value);
    } else if (value instanceof Integer) {
      this.item.setInteger(key, (Integer)value);
    } else if (value instanceof ItemStack) {
      this.item.setItemStack(key, (ItemStack)value);
    } else if (value instanceof Long) {
      this.item.setLong(key, (Long)value);
    } else if (value instanceof Short) {
      this.item.setShort(key, (Short)value);
    } else if (value instanceof UUID) {
      this.item.setUUID(key, (UUID)value);
    } 
    return this;
  }
  
  public static NBTItem toNBT(ItemStack item) {
    return new NBTItem(item);
  }
}
