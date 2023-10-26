package fr.eris.eristrade.utils.file;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    public static File ROOT_FOLDER;

    public static File getOrCreateFile(File parent, String name) {
        File file = new File(parent, name);
        if(!parent.exists()) parent.mkdir();
        if(!file.exists()) {
            try {
                if(name.contains(".")) file.createNewFile();
                else file.mkdir();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static List<File> getAllFile(File parent, String endWith) {
        List<File> list = new ArrayList<>();
        Arrays.asList(parent.getAbsoluteFile().listFiles()).forEach(file -> {
            if(file.getName().endsWith(endWith))
                list.add(file);
        });

        return list;
    }

    public static void writeFile(File file, String content) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readWholeFile(File file) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }

            return builder.length() != 0 ? builder.toString() : null;

        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean deleteFile(File parent, String name) {
        File file = new File(parent, name);
        return file.delete();
    }

    public static boolean deleteFile(File file) {
        return file.delete();
    }

    public static boolean isExist(File parent, String fileName) {
        return new File(parent, fileName).exists();
    }
}
