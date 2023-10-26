package fr.eris.eristrade.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    private final static Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String translate(String text) {
        Matcher matcher = pattern.matcher(text);
        while(matcher.find()) {
            String color = text.substring(matcher.start(), matcher.end());
            text = text.replace(color, ChatColor.stripColor(color) + "");
            matcher = pattern.matcher(text);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    public static List<String> translate(List<String> texts) {
        List<String> newTexts = new ArrayList<>();
        for(String str : texts)
            newTexts.add(translate(str));
        return newTexts;
    }

    public static String reversedTranslate(String text) {
        char[] array = text.toCharArray();
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] == ChatColor.COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(array[i + 1]) != -1) {
                array[i] = '&';
                array[i + 1] = Character.toLowerCase(array[i + 1]);
            }
        }
        return new String(array);
    }

    public static String strip(String text) {return ChatColor.stripColor(text);}
}