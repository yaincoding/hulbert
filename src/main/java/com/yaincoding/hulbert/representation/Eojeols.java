package com.yaincoding.hulbert.representation;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Eojeols {
    private final List<Eojeol> eojeols;
    private final double score;

    private Eojeols(List<Eojeol> eojeols, double score) {
        this.eojeols = eojeols;
        this.score = score;
    }

    public static Eojeols of(List<Eojeol> eojeols, double score) {
        return new Eojeols(eojeols, score);
    }
}
