package com.yaincoding.hulbert.transformer;

import java.util.ArrayList;
import java.util.List;
import com.yaincoding.hulbert.feature.BaseFeatureTransformer;
import com.yaincoding.hulbert.feature.FeatureTransformer;
import com.yaincoding.hulbert.pos.Pos;
import com.yaincoding.hulbert.representation.WordPos;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseFeatureTransformerTest {

    private FeatureTransformer featureTransformer;

    @BeforeEach
    public void setup() {
        Configurator.setAllLevels("", Level.INFO);
        featureTransformer = new BaseFeatureTransformer();
    }

    @Test
    void testTransformWordPosSentenceToFeatures() {

        List<WordPos> wordPosSentence = new ArrayList<>();
        wordPosSentence.add(WordPos.of("안녕", Pos.NNG));
        wordPosSentence.add(WordPos.of("하", Pos.XSA));
        wordPosSentence.add(WordPos.of("세요", Pos.EF));

        log.info(
                featureTransformer.transformWordPosSentenceToFeatures(wordPosSentence).toString());
    }
}
