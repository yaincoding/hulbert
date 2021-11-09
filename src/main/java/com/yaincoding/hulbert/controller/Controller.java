package com.yaincoding.hulbert.controller;

import java.util.List;

import com.yaincoding.hulbert.representation.Eojeols;
import com.yaincoding.hulbert.tag.Tagger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class Controller {

    private final Tagger tagger;

    @GetMapping
    public ResponseEntity<List<Eojeols>> analyze(String s) {
        return ResponseEntity.ok(tagger.tag(s, true, false));
    }
}
