package com.yaincoding.hulbert.controller;

import java.util.List;

import com.yaincoding.hulbert.representation.Eojeols;
import com.yaincoding.hulbert.tag.Tagger;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${modelparam.a_syllable_penalty}")
    private double A_SYLLABLE_PENALTY;

    @Value("${modelparam.noun_preference}")
    private double NOUN_PREFERENCE;

    @Value("${modelparam.longer_noun_preference}")
    private double LONGER_NOUN_PREFERENCE;

    @Value("${modelparam.unknown_penalty}")
    private double UNKNOWN_PENALTY;

    @GetMapping
    public ResponseEntity<List<Eojeols>> analyze(String s,
            @RequestParam(name = "a_syllable_penalty", required = false) Double aSyllablePenalty,
            @RequestParam(name = "noun_preference", required = false) Double nounPreference,
            @RequestParam(name = "longer_noun_preference", required = false) Double longerNounPreference,
            @RequestParam(name = "unknown_penalty", required = false) Double unknownPenalty) {

        if (aSyllablePenalty == null) {
            aSyllablePenalty = A_SYLLABLE_PENALTY;
        }

        if (nounPreference == null) {
            nounPreference = NOUN_PREFERENCE;
        }

        if (longerNounPreference == null) {
            longerNounPreference = LONGER_NOUN_PREFERENCE;
        }

        if (unknownPenalty == null) {
            unknownPenalty = UNKNOWN_PENALTY;
        }

        return ResponseEntity
                .ok(tagger.tag(s, true, aSyllablePenalty, nounPreference, longerNounPreference, unknownPenalty));
    }

}
