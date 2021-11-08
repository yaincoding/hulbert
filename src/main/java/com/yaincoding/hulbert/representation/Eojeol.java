package com.yaincoding.hulbert.representation;

import com.yaincoding.hulbert.pos.Pos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
}
