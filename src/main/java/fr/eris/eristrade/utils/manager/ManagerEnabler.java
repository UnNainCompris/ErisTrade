package fr.eris.eristrade.utils.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ManagerEnabler {
    /**
     * Create an instance of every class that implement Manager is the "instance" class
     */
    public static <T> void init(T instance) {
        List<Field> managerFields = new ArrayList<>();

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!Manager.class.isAssignableFrom(field.getType())) continue;
            managerFields.add(field);
        }

        managerFields.sort(Comparator.comparingInt(managerField -> {
            Priority priority = Priority.NORMAL;
            final ManagerPriority managerPriority;
            if((managerPriority = managerField.getAnnotation(ManagerPriority.class)) != null) priority = managerPriority.initPriority();
            return priority.getValue();
        }));
        Collections.reverse(managerFields);
        for(Field managerField : managerFields) {
            try {
                managerField.setAccessible(true);
                Constructor<?> constructor = managerField.getType().getDeclaredConstructor();
                managerField.set(instance, constructor.newInstance());
                Manager manager = ((Manager) managerField.get(instance));
                manager.start();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call the stop function on every class that implement Manager is the "instance" class
     */
    public static <T> void stop(T instance) {
        List<Field> managerFields = new ArrayList<>();

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!Manager.class.isAssignableFrom(field.getType())) continue;
            managerFields.add(field);
        }

        managerFields.sort(Comparator.comparingInt(managerField -> {
            Priority priority = Priority.NORMAL;
            final ManagerPriority managerPriority;
            if((managerPriority = managerField.getAnnotation(ManagerPriority.class)) != null) priority = managerPriority.stopPriority();
            return priority.getValue();
        }));

        Collections.reverse(managerFields);
        for(Field managerField : managerFields) {
            managerField.setAccessible(true);
            try {
                if (managerField.get(instance) != null) {
                    Manager manager = ((Manager) managerField.get(instance));
                    manager.stop();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}