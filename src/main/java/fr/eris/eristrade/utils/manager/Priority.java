package fr.eris.eristrade.utils.manager;

import lombok.Getter;

public enum Priority {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4);

    @Getter
    private final int value;
    Priority(int value) {
        this.value = value;
    }
}
