package fr.eris.eristrade.utils.storage;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

public class Quad <First, Second, Third, Fourth> {

    @Expose @Getter @Setter private First first;
    @Expose @Getter @Setter private Second second;
    @Expose @Getter @Setter private Third third;
    @Expose @Getter @Setter private Fourth fourth;

    public Quad(First first, Second second, Third third, Fourth fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }
}
