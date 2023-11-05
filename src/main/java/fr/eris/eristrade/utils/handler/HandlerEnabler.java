package fr.eris.eristrade.utils.handler;

import fr.eris.eristrade.utils.manager.Priority;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HandlerEnabler {
    /**
     * Create an instance of every class that implement Handler is the "instance" class
     */
    public static <T> void init(T instance) {
        List<Field> handlerFields = new ArrayList<>();

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!Handler.class.isAssignableFrom(field.getType())) continue;
            handlerFields.add(field);
        }

        handlerFields.sort(Comparator.comparingInt(handlerField -> {
            Priority priority = Priority.NORMAL;
            final HandlerPriority handlerPriority;
            if((handlerPriority = handlerField.getAnnotation(HandlerPriority.class)) != null) priority = handlerPriority.initPriority();
            return priority.getValue();
        }));
        Collections.reverse(handlerFields);
        for(Field handlerField : handlerFields) {
            try {
                handlerField.setAccessible(true);
                Constructor<?> constructor = handlerField.getType().getDeclaredConstructor();
                handlerField.set(instance, constructor.newInstance());
                Handler handler = ((Handler) handlerField.get(instance));
                handler.register();
                handler.start();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call the stop function on every class that implement Handler is the "instance" class
     */
    public static <T> void stop(T instance) {
        List<Field> handlerFields = new ArrayList<>();

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!Handler.class.isAssignableFrom(field.getType())) continue;
            handlerFields.add(field);
        }

        handlerFields.sort(Comparator.comparingInt(handlerField -> {
            Priority priority = Priority.NORMAL;
            final HandlerPriority handlerPriority;
            if((handlerPriority = handlerField.getAnnotation(HandlerPriority.class)) != null) priority = handlerPriority.stopPriority();
            return priority.getValue();
        }));

        Collections.reverse(handlerFields);
        for(Field handlerField : handlerFields) {
            handlerField.setAccessible(true);
            try {
                if (handlerField.get(instance) != null) {
                    Handler handler = ((Handler) handlerField.get(instance));
                    handler.unregister();
                    handler.stop();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
