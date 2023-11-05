package fr.eris.eristrade.utils.handler;

import fr.eris.eristrade.utils.manager.Priority;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerPriority {
    Priority initPriority() default Priority.NORMAL;
    Priority stopPriority() default Priority.NORMAL;
}
