package fr.eris.eristrade.utils.storage;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

public class Triplet <First, Second, Third> {

    @Expose @Getter @Setter private First first;
    @Expose @Getter @Setter private Second second;
    @Expose @Getter @Setter private Third third;

    public Triplet(First first, Second second, Third third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
