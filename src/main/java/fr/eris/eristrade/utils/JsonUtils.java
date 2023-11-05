package fr.eris.eristrade.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

import java.io.File;
import java.util.List;

public class JsonUtils {
    @Getter private static final Gson gson =
        new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting()
        .create();

    public static <T> T getData(File file, Class<T> type) {
        return gson.fromJson(FileUtils.readWholeFile(file), type);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return gson.fromJson(json, type);
        } catch (Exception exception) {
            return null;
        }
    }

    public static <T> List<T> getDataList(File file, Class<T> type) {
        return gson.fromJson(FileUtils.readWholeFile(file), new ListParameterizedType(type));
    }

    public static void writeData(File file, Object clazz) {
        FileUtils.writeFile(file, gson.toJson(clazz));
    }

    public static File getOrCreateJson(File parent, String fileName) {
        return FileUtils.getOrCreateFile(parent, fileName + ".json");
    }

    public static boolean deleteJson(File parent, String fileName) {
        return FileUtils.deleteFile(parent, fileName + ".json");
    }

    public static boolean isExist(File parent, String fileName) {
        return new File(parent, fileName + ".json").exists();
    }
}
