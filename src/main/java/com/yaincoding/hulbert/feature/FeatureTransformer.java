package com.yaincoding.hulbert.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.yaincoding.hulbert.pos.Pos;
import com.yaincoding.hulbert.representation.WordPos;

public abstract class FeatureTransformer {

    public List<List<String>> transformWordPosSentenceToFeatures(List<WordPos> wordPosSentence) {

        List<String> words =
                wordPosSentence.stream().map(WordPos::getWord).collect(Collectors.toList());
        words.add(0, "BOS");
        words.add("EOS");

        List<Pos> tags = wordPosSentence.stream().map(WordPos::getPos).collect(Collectors.toList());
        tags.add(0, Pos.BOS);
        tags.add(Pos.EOS);

        List<List<String>> features = potentialFunction(words, tags);

        return features;
    }

    protected List<List<String>> potentialFunction(List<String> words, List<Pos> tags) {
        List<List<String>> features = new ArrayList<>();
        for (int i = 1; i < tags.size() - 1; i++) {
            features.add(toFeature(words, tags, i));
        }

        return features;
    }

    protected abstract List<String> toFeature(List<String> words, List<Pos> tags, int i);
}
