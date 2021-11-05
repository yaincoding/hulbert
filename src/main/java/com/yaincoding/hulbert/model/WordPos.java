package com.yaincoding.hulbert.model;

import com.yaincoding.hulbert.pos.Pos;

import lombok.Getter;

@Getter
public final class WordPos {

    private final String word;
    private final Pos pos;

    private WordPos(String word, Pos pos) {
        this.word = word;
        this.pos = pos;
    }

    public static WordPos of(String word, Pos pos) {
        return new WordPos(word, pos);
    }
}
