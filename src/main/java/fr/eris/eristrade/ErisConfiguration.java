package fr.eris.eristrade;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ErisConfiguration {
    String permissionPrefix();
    String name();
    String version();
}
