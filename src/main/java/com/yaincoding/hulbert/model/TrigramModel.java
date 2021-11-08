package com.yaincoding.hulbert.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.yaincoding.hulbert.pos.Pos;

public class TrigramModel extends Model {

    private Map<Pos, List<String>> previous_1X0;
    private Map<Pos, List<String>> previous_X0_1Y;
    private Map<Pos, List<String>> successive_X01;
    private Map<Pos, List<String>> successive_X01_Y1;
    private Map<Pos, List<String>> bothside_1X1;
    private Map<Pos, List<String>> bothside_1X01;

    @Override
    protected void separateFeatures() {

        Predicate<String> is_1X0 = f -> f.contains("x[-1:0]") && !f.contains(" ");
        Predicate<String> is_X0_1Y = f -> f.contains("y[-1]") && !f.contains(" ");
        Predicate<String> is_X01 = f -> f.contains("x[0:1]") && !f.contains(" ");
        Predicate<String> is_X01_Y1 = f -> f.contains("x[0:1]") && f.contains("y[1]");
        Predicate<String> is_1X1 = f -> f.contains("x[-1,1]");
        Predicate<String> is_1X01 = f -> f.contains("x[-1:1]");

        for (Entry<StateFeature, Double> entry : stateFeatures.entrySet()) {
            String feature = entry.getKey().getFeature();
            Pos tag = entry.getKey().getPos();
            double score = entry.getValue();
            if (is_1X0(feature)) {
                List<String> wordsposes = previous_1X0.getOrDefault(tag, new ArrayList<>());
                previous_1X0.put(tag, wordsposes);
            }
        }
    }

    private List<String> parseWord(String feature) {
        return Arrays.stream(feature.split(", ")).map(p -> p.split("]=")[1].split("-")).flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

}
