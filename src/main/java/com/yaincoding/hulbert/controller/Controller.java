package com.yaincoding.hulbert.controller;

import java.util.List;

import com.yaincoding.hulbert.representation.Eojeols;
import com.yaincoding.hulbert.tag.Tagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class Controller {

    private final Tagger tagger;

    @GetMapping
    public ResponseEntity<List<Eojeols>> analyze(String s,
            @RequestParam(name = "a_syllable_penalty", required = false) Double aSyllablePenalty,
            @RequestParam(name = "noun_preference", required = false) Double nounPreference,
            @RequestParam(name = "longer_noun_preference", required = false) Double longerNounPreference,
            @RequestParam(name = "unknown_penalty", required = false) Double unknownPenalty) {
        if (aSyllablePenalty != null || nounPreference != null || longerNounPreference != null
                || unknownPenalty != null) {
            return ResponseEntity
                    .ok(tagger.tag(s, true, aSyllablePenalty, nounPreference, longerNounPreference, unknownPenalty));
        }

        return ResponseEntity.ok(tagger.tag(s, true));
    }

}
