package com.yaincoding.hulbert.tag;

import java.io.IOException;
import java.util.List;

import com.yaincoding.hulbert.model.Beam;
import com.yaincoding.hulbert.model.Model;
import com.yaincoding.hulbert.model.TrigramModel;
import com.yaincoding.hulbert.representation.Eojeol;
import com.yaincoding.hulbert.representation.Eojeols;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TrigramTagger implements Tagger {

    private final Model model;

    public TrigramTagger() throws IOException {
        String modelPath = System.getenv("TRIGRAM_MODEL_PATH");
        model = new TrigramModel(modelPath);
    }

    @Override
    public List<Eojeols> tag(String sentence, boolean flatten, boolean debug) {

        List<List<Eojeol>> beginIndex = model.lookup(sentence, false);
        String chars = StringUtils.trimAllWhitespace(sentence);

        Beam beam = Beam.builder().topK(5).unknownPenalty(model.UNKNOWN_PENALTY).aSyllablePenalty(-0.3)
                .nounPreference(0.4).longerNounPreference(0.3).model(model).build();

        List<Eojeols> topEojeols = beam.search(beginIndex, chars);
        return topEojeols.subList(1, topEojeols.size() - 1);
    }

}
