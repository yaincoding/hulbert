package com.yaincoding.hulbert.representation;

import com.yaincoding.hulbert.pos.Pos;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Eojeol {
    private final String eojeol;
    private final String firstWord;
    private final Pos firstTag;
    private final String lastWord;
    private final Pos lastTag;
    private final int start;
    private final int end;
    private final double score;
    private final boolean isCompound;
    private final boolean isUnknown;

    public int length() {
        return end - start;
    }

    public boolean isNoun() {
        return firstTag == Pos.NNG || firstTag == Pos.NNP;
    }
}
