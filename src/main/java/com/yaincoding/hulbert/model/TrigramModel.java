package com.yaincoding.hulbert.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.yaincoding.hulbert.pos.Pos;
import com.yaincoding.hulbert.representation.Eojeol;
import com.yaincoding.hulbert.representation.Eojeols;

public class TrigramModel extends Model {

    public TrigramModel(String modelPath) throws IOException {
        super(modelPath);
        separateFeatures();
    }

    private Map<Pos, Map<String, Double>> previous_1X0 = new HashMap<>();
    private Map<Pos, Map<String, Double>> previous_X0_1Y = new HashMap<>();
    private Map<Pos, Map<String, Double>> successive_X01 = new HashMap<>();
    private Map<Pos, Map<String, Double>> successive_X01_Y1 = new HashMap<>();
    private Map<Pos, Map<String, Double>> bothside_1X1 = new HashMap<>();
    private Map<Pos, Map<String, Double>> bothside_1X01 = new HashMap<>();

    private final Predicate<String> is_1X0 = f -> f.contains("x[-1:0]") && !f.contains(" ");
    private final Predicate<String> is_X0_1Y = f -> f.contains("y[-1]") && !f.contains(" ");
    private final Predicate<String> is_X01 = f -> f.contains("x[0:1]") && !f.contains(" ");
    private final Predicate<String> is_X01_Y1 = f -> f.contains("x[0:1]") && f.contains("y[1]");
    private final Predicate<String> is_1X1 = f -> f.contains("x[-1,1]");
    private final Predicate<String> is_1X01 = f -> f.contains("x[-1:1]");

    // feature의 단어/태그 -> ex) x[-1,1]=세계-이, y[-1]=NNG -> 세계-이-NNG
    private final Function<String, String> parseWord = feature -> Arrays.stream(feature.split(", "))
            .map(p -> p.split("]=")[1].split("-")).flatMap(Arrays::stream).collect(Collectors.joining("-"));

    @Override
    protected void separateFeatures() {

        for (Entry<StateFeature, Double> entry : stateFeatures.entrySet()) {
            String feature = entry.getKey().getFeature();
            Pos tag = entry.getKey().getPos();
            double score = entry.getValue();
            if (is_1X0.test(feature)) {
                Map<String, Double> wordsposes = previous_1X0.getOrDefault(tag, new HashMap<>());
                previous_1X0.put(tag, wordsposes);
                wordsposes.put(parseWord.apply(feature), score);
            } else if (is_X0_1Y.test(feature)) {
                Map<String, Double> wordsposes = previous_X0_1Y.getOrDefault(tag, new HashMap<>());
                previous_X0_1Y.put(tag, wordsposes);
                wordsposes.put(parseWord.apply(feature), score);
            } else if (is_X01.test(feature)) {
                Map<String, Double> wordsposes = successive_X01.getOrDefault(tag, new HashMap<>());
                successive_X01.put(tag, wordsposes);
                wordsposes.put(parseWord.apply(feature), score);
            } else if (is_X01_Y1.test(feature)) {
                Map<String, Double> wordsposes = successive_X01_Y1.getOrDefault(tag, new HashMap<>());
                successive_X01_Y1.put(tag, wordsposes);
                wordsposes.put(parseWord.apply(feature), score);
            } else if (is_1X1.test(feature)) {
                Map<String, Double> wordsposes = bothside_1X1.getOrDefault(tag, new HashMap<>());
                bothside_1X1.put(tag, wordsposes);
                wordsposes.put(parseWord.apply(feature), score);
            } else if (is_1X01.test(feature)) {
                Map<String, Double> wordsposes = bothside_1X01.getOrDefault(tag, new HashMap<>());
                bothside_1X01.put(tag, wordsposes);
                wordsposes.put(parseWord.apply(feature), score);
            }
        }
    }

    @Override
    public double score(Eojeols immature, Eojeol eojeol) {

        Eojeol eojeolPrev = immature.getEojeols().get(immature.getEojeols().size() - 1); // x[-1]
        double score = eojeol.getScore(); // x[0]

        // transition score
        score += transitions.getOrDefault(Transition.of(eojeolPrev.getLastTag(), eojeol.getFirstTag()), 0.0);

        if (!eojeol.isUnknown()) {
            score += previous_1X0.getOrDefault(eojeol.getFirstTag(), Collections.emptyMap())
                    .getOrDefault(String.join("-", eojeolPrev.getLastWord(), eojeol.getFirstWord()), 0.0);
            score += previous_X0_1Y.getOrDefault(eojeol.getFirstTag(), Collections.emptyMap())
                    .getOrDefault(String.join("-", eojeol.getFirstWord(), eojeolPrev.getLastTag().name()), 0.0);
            score += successive_X01.getOrDefault(eojeolPrev.getLastTag(), Collections.emptyMap())
                    .getOrDefault(String.join("-", eojeolPrev.getLastWord(), eojeol.getFirstWord()), 0.0);
            score += successive_X01_Y1.getOrDefault(eojeolPrev.getLastTag(), Collections.emptyMap()).getOrDefault(
                    String.join("-", eojeolPrev.getLastWord(), eojeol.getFirstWord(), eojeol.getFirstTag().name()),
                    0.0);
        }

        if (immature.getEojeols().size() >= 2 && !eojeol.isUnknown()) {
            Eojeol eojeolPrev2 = immature.getEojeols().get(immature.getEojeols().size() - 2);
            score += bothside_1X1.getOrDefault(eojeolPrev.getFirstTag(), Collections.emptyMap())
                    .getOrDefault(String.join("-", eojeolPrev2.getLastWord(), eojeol.getFirstWord()), 0.0);
            score += bothside_1X01.getOrDefault(eojeolPrev.getFirstTag(), Collections.emptyMap()).getOrDefault(
                    String.join("-", eojeolPrev2.getLastWord(), eojeolPrev.getFirstWord(), eojeol.getFirstWord()), 0.0);
        }

        return score;
    }
}
