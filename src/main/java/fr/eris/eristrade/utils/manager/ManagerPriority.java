package fr.eris.eristrade.utils.manager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ManagerPriority {
    Priority initPriority() default Priority.NORMAL;
    Priority stopPriority() default Priority.NORMAL;
}
