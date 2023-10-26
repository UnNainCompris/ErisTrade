package fr.eris.eristrade.utils;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

public class Tuple<First, Second> {
    @Expose @Getter @Setter private First first;
    @Expose @Getter @Setter private Second second;

    public Tuple(First first, Second second) {
        this.first = first;
        this.second = second;
    }
}