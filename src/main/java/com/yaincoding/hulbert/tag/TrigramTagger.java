package com.yaincoding.hulbert.tag;

import java.util.List;

import com.yaincoding.hulbert.model.WordPos;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TrigramTagger implements Tagger {

    @Override
    public List<WordPos> tag(String sentence, boolean flatten, boolean debug) {
        return null;
    }

    
}
