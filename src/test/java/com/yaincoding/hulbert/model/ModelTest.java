package com.yaincoding.hulbert.model;

import java.io.IOException;
import java.util.List;

import com.yaincoding.hulbert.representation.Eojeol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModelTest {

    private Model model;

    @BeforeEach
    public void setup() throws IOException {
        model = new TrigramModel(System.getenv("TRIGRAM_MODEL_PATH"));
    }

    @Test
    void testLookup() {
        List<List<Eojeol>> beginIndex = model.lookup("안녕하세요", false);
        log.debug(beginIndex.toString());
        System.out.println(beginIndex.toString());
    }

    @Test
    void testScore() {

    }
}
