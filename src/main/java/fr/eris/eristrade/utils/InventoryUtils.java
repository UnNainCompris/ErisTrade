package fr.eris.eristrade.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InventoryUtils {

    public static byte getSizeFromInt(int input) {
        for(byte size : Arrays.asList((byte)9, (byte)18, (byte)27, (byte)36, (byte)45))
            if (input <= size) return size;
        return (byte) 54;
    }

    public static Inventory fillInventory(Inventory inventory, ItemStack... itemStack) {
        int currentItem = 0;
        for(int i = 0; i < inventory.getSize() ; i++) {
            if(inventory.getContents()[0] == null || inventory.getContents()[0].getType() == Material.AIR) {
                inventory.setItem(i, itemStack[currentItem]);
                currentItem++;
                if(currentItem > itemStack.length) currentItem = 0;
            }
        }
        return inventory;
    }

    public static Inventory aroundInventory(Inventory inventory, ItemStack... items) {
        setSideInventory(inventory, items, new Side[]{Side.RIGHT, Side.DOWN, Side.LEFT, Side.UP});
        return inventory;
    }

    public enum Side{
        RIGHT, LEFT, UP, DOWN;
    }

    public static Inventory setSideInventory(Inventory inventory, ItemStack[] items, Side[] sides){
        int currentItem = 0;
        for(Side side : sides){
            int[] pos = {};
            if(side == Side.RIGHT) {
                pos = new int[]{8, 17, 26, 35, 44, 53};
            } else if(side == Side.LEFT) {
                pos = new int[]{0, 9, 18, 27, 36, 45};
            } else if(side == Side.UP) {
                pos = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
            } else if(side == Side.DOWN) {
                final int size = inventory.getSize();
                pos = new int[]{size - 9, size - 8, size - 7, size - 6, size - 5, size - 4, size - 3, size - 2, size - 1};
            }
            for(int i = 0 ; i < pos.length ; i++) {
                if(pos[i] > inventory.getSize() - 1) continue;
                inventory.setItem(pos[i], items[currentItem]);
                currentItem++;
                if(currentItem > items.length-1) currentItem = 0;
            }
        }
        return inventory;
    }

    public static Inventory addCorner(Inventory inventory, ItemStack item, int cornerLength) {
        Set<Integer> posList = new HashSet<>();

        for(int i = 0 ; i < cornerLength ; i++) {
            //Top left corner
            posList.add(i);
            posList.add(9 * i);
            //Top right corner
            posList.add(8 - i);
            posList.add((9 * (i + 1)) - 1);
            //Bottom left corner
            posList.add((inventory.getSize() - 8) + i);
            posList.add((inventory.getSize() - 8) - (i * 9));
            //Bottom right corner
            posList.add(inventory.getSize() - i);
            posList.add(inventory.getSize() - (i * 9));
        }

        for(int pos : posList) {
            inventory.setItem(pos, item);
        }
        return inventory;
    }

}
