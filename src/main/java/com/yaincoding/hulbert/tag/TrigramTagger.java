package com.yaincoding.hulbert.tag;

import java.io.IOException;
import java.util.List;

import com.yaincoding.hulbert.model.Beam;
import com.yaincoding.hulbert.model.Model;
import com.yaincoding.hulbert.model.TrigramModel;
import com.yaincoding.hulbert.representation.Eojeol;
import com.yaincoding.hulbert.representation.Eojeols;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TrigramTagger implements Tagger {

    private final Model model;

    @Value("${modelparam.a_syllable_penalty}")
    private double A_SYLLABLE_PENALTY;

    @Value("${modelparam.noun_preference}")
    private double NOUN_PREFERENCE;

    @Value("${modelparam.longer_noun_preference}")
    private double LONGER_NOUN_PREFERENCE;

    @Value("${modelparam.unknown_penalty}")
    private double UNKNOWN_PENALTY;

    public TrigramTagger() throws IOException {
        String modelPath = System.getenv("TRIGRAM_MODEL_PATH");
        model = new TrigramModel(modelPath);
    }

    @Override
    public List<Eojeols> tag(String sentence, boolean flatten) {
        List<List<Eojeol>> beginIndex = model.lookup(sentence, false);

        Beam beam = Beam.builder().topK(5).unknownPenalty(UNKNOWN_PENALTY).aSyllablePenalty(A_SYLLABLE_PENALTY)
                .nounPreference(NOUN_PREFERENCE).longerNounPreference(LONGER_NOUN_PREFERENCE).model(model).build();

        List<Eojeols> topEojeols = beam.search(beginIndex, StringUtils.trimAllWhitespace(sentence));
        return topEojeols;
    }

    @Override
    public List<Eojeols> tag(String sentence, boolean flatten, double aSyllablePenalty, double nounPreference,
            double longerNounPreference, double unknownPenalty) {
        List<List<Eojeol>> beginIndex = model.lookup(sentence, false);

        Beam beam = Beam.builder().topK(5).unknownPenalty(unknownPenalty).aSyllablePenalty(aSyllablePenalty)
                .nounPreference(nounPreference).longerNounPreference(longerNounPreference).model(model).build();

        List<Eojeols> topEojeols = beam.search(beginIndex, StringUtils.trimAllWhitespace(sentence));
        return topEojeols;
    }

}
