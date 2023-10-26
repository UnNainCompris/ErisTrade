package fr.eris.eristrade.utils;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class StringUtils {

    public static String center(String value, int maxLength) {
        StringBuilder builder = new StringBuilder(maxLength - value.length());
        IntStream.range(0, maxLength - value.length()).forEach(i -> builder.append(" "));

        builder.insert((builder.length() / 2) + 1, value);
        return builder.toString();
    }

    public static String fastReplace(String defaultString, String... toReplaceList){
        String returnValue = defaultString;
        for(String toReplaceItem : toReplaceList){
            String[] toReplaceArray = toReplaceItem.split("->");
            returnValue = returnValue.replace(toReplaceArray[0], toReplaceArray[1]);
        }
        return returnValue;
    }

    public static void sendMessage(String toPrint, int splitEveryChar, int maxLength, String messageColor, String barColor1, String barColor2){
        String border = getBorder(toPrint, maxLength, barColor1, barColor2);

        Bukkit.getConsoleSender().sendMessage(ColorUtils.translate(border));
        Bukkit.getConsoleSender().sendMessage(" ");
        for(String string : toPrint.split("\n")) {
            for (String message : string.split("(?<=\\G.{" + splitEveryChar + "})"))
                Bukkit.getConsoleSender().sendMessage(ColorUtils.translate(messageColor + center(message, maxLength)));
        }
        Bukkit.getConsoleSender().sendMessage(" ");
        Bukkit.getConsoleSender().sendMessage(ColorUtils.translate(border));
    }

    private static String getBorder(String toPrint, int maxLength, String barColor1, String barColor2) {
        float temp = (float) toPrint.length() + (maxLength - toPrint.length());
        int borderLenght = (int) temp;
        StringBuilder border = new StringBuilder(borderLenght);
        IntStream.range(0, borderLenght).forEach(i -> {
            if(i < 2 || i >= borderLenght - 2) border.append(barColor1).append('-');
            else border.append(barColor2).append('-');
        });
        return border.toString();
    }

    public static void sendMessage(String toPrint, String messageColor) {
        sendMessage(toPrint, 50, 70, messageColor, messageColor, messageColor);
    }

    public static void sendMessage(String toPrint, String messageColor, String barColor1, String barColor2) {
        sendMessage(toPrint, 50, 70, messageColor, barColor1, barColor2);
    }

    public static void sendErrorMessage(String toPrint, Exception exception, String messageColor) {
        sendMessage(toPrint, 50, 70, messageColor, messageColor, messageColor);
        exception.printStackTrace();
        Bukkit.getConsoleSender().sendMessage(ColorUtils.translate(getBorder(exception.toString(), 100, messageColor, messageColor)));
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isLong(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String upperCaseFirstLetter(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static List<String> prettySplit(String toSplit, boolean keepWord, int everyChar) {
        final List<String> newString = new ArrayList<>();

        if(keepWord) {
            int charCounter = 0;
            for(String line : toSplit.split("\n")) {
                StringBuilder toAdd = new StringBuilder();
                for(String word : line.split(" ")) {
                    charCounter += (word + " ").length() - 2;
                    if(charCounter > everyChar) {
                        if(toAdd.length() == 0)
                            toAdd.append(word);
                        newString.add(toAdd.toString());
                        toAdd = new StringBuilder();
                        charCounter = 0;
                    } else {
                        toAdd.append(word).append(" ");
                    }
                }
                charCounter = 0;
                if(!toAdd.toString().equals(""))
                    newString.add(toAdd.toString());
            }
        } else
            for(String line : toSplit.split("\n"))
                newString.addAll(Arrays.asList(line.split("(?<=\\G.{" + everyChar + "})")));
        return newString;
    }

    public static List<String> findPlaceholders(String[] placeholdersChar, String string) {
        final List<String> found = new ArrayList<>();
        if(!string.contains(placeholdersChar[0].replace("\\", "")))
            return found;
        for(String str : string.split(placeholdersChar[0])) {
            if(!str.contains(placeholdersChar[1].replace("\\", ""))) continue;
            found.add("&7{" + ColorUtils.strip(str.split(placeholdersChar[1])[0]) + "}");
        }
        return found;
    }

    public static String listToString(List<String> list, String separator) {
        StringBuilder builder = new StringBuilder();
        for(String str : list) {
            builder.append(str);
            if(list.indexOf(str) != list.size() - 1)
                builder.append(separator);
        }
        return builder.toString();
    }

    public static boolean isDouble(String argument) {
        try {
            Double.parseDouble(argument);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String repeat(String stringToRepeat, int amount) {
        StringBuilder toReturn = new StringBuilder();
        for(int i = 0 ; i < amount ; i++) {
            toReturn.append(stringToRepeat);
        }
        return toReturn.toString();
    }

    public static boolean isUUID(String argument) {
        try {
            UUID.fromString(argument);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
