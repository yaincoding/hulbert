package com.yaincoding.hulbert.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yaincoding.hulbert.pos.Pos;
import com.yaincoding.hulbert.representation.Eojeol;

import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public abstract class Model {

    protected Map<StateFeature, Double> stateFeatures;
    protected Map<Transition, Double> transitions;
    protected Map<Pos, Map<String, Double>> pos2words;

    protected static int MAX_WORD_LEN;
    protected static double MAX_SCORE;

    private static final double UNKNOWN_PENALTY = -0.1;

    @PostConstruct
    private void loadJsonModel() throws IOException {
        String modelPath = System.getenv("model_path");
        String jsonString = Files.readString(Paths.get(modelPath), StandardCharsets.UTF_8);
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

        JsonObject stateFeaturesJsonObject = jsonObject.get("state_features").getAsJsonObject();
        this.stateFeatures = createStateFeatureModel(stateFeaturesJsonObject);

        JsonObject transitionsJsonObject = jsonObject.get("transitions").getAsJsonObject();
        this.transitions = createTransitionModel(transitionsJsonObject);

        this.pos2words = constructDictionaryFromStateFeatures();

        separateFeatures();
    }

    private Map<StateFeature, Double> createStateFeatureModel(JsonObject stateFeaturesJsonObject) {
        Map<StateFeature, Double> stateFeatures = new HashMap<>();
        for (Entry<String, JsonElement> entry : stateFeaturesJsonObject.entrySet()) {
            String stateFeature = entry.getKey();
            double score = entry.getValue().getAsDouble();
            MAX_SCORE = Math.max(MAX_SCORE, score);

            String[] tokens = stateFeature.split(" -> ");
            StateFeature sf = StateFeature.of(tokens[0], Pos.valueOf(tokens[1]));

            stateFeatures.put(sf, score);
        }

        return stateFeatures;
    }

    private Map<Transition, Double> createTransitionModel(JsonObject transitionsJsonObject) {
        Map<Transition, Double> transitions = new HashMap<>();
        for (Entry<String, JsonElement> entry : transitionsJsonObject.entrySet()) {
            String transition = entry.getKey();
            String[] poses = transition.split(" -> ");
            Pos prevPos = Pos.valueOf(poses[0]);
            Pos nextPos = Pos.valueOf(poses[1]);

            Transition t = Transition.of(prevPos, nextPos);
            double score = entry.getValue().getAsDouble();
            MAX_SCORE = Math.max(MAX_SCORE, score);

            transitions.put(t, score);
        }

        return transitions;
    }

    private Map<Pos, Map<String, Double>> constructDictionaryFromStateFeatures() {
        Map<Pos, Map<String, Double>> pos2words = new HashMap<>();
        for (Entry<StateFeature, Double> entry : this.stateFeatures.entrySet()) {
            StateFeature sf = entry.getKey();
            double score = entry.getValue();

            if (sf.getFeature().substring(0, 4).equals("x[0]") && !sf.getFeature().contains(", ") && score > 0) {
                String word = sf.getFeature().substring(5);
                MAX_WORD_LEN = Math.max(MAX_WORD_LEN, word.length());
                Pos pos = sf.getPos();

                Map<String, Double> wordScore = pos2words.getOrDefault(pos, new HashMap<>());
                wordScore.put(word, score);
                pos2words.put(pos, wordScore);
            }
        }
        return pos2words;
    }

    public List<List<Eojeol>> lookup(String sentence, boolean guessTag) {
        final String doubleSpaceRemovedSentece = sentence.replaceAll("\\s{2,}", "\\s");
        return Arrays.stream(sentence.split("\\s"))
                .map(w -> wordLookup(w, doubleSpaceRemovedSentece.length(), guessTag)).flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<List<Eojeol>> wordLookup(String eojeol, int offset, boolean guessTag) {

        List<List<Eojeol>> poses = new ArrayList<>();
        for (int i = 0; i < eojeol.length(); i++) {
            poses.add(new ArrayList<>());
        }

        for (int begin = 0; begin < eojeol.length(); begin++) {
            for (int len = 1; len < MAX_WORD_LEN + 1; len++) {
                int end = begin + len;
                if (end > eojeol.length()) {
                    continue;
                }

                String sub = eojeol.substring(begin, end);
                Map<Pos, Double> tagScores = getTagScore(sub);

                if (!tagScores.isEmpty()) { // if sub is known word
                    for (Entry<Pos, Double> tagScore : tagScores.entrySet()) {
                        Pos tag = tagScore.getKey();
                        double score = tagScore.getValue();
                        poses.get(begin)
                                .add(Eojeol.builder().eojeol(sub + "/" + tag.name()).firstWord(sub).lastWord(sub)
                                        .firstTag(tag).lastTag(tag).start(offset + begin).end(offset + end).score(score)
                                        .isCompound(false).isUnknown(false).build());
                    }
                } else if (guessTag) { // if sub is unknown
                    for (Entry<Pos, Double> tagScore : guessTag().entrySet()) {
                        Pos tag = tagScore.getKey();
                        double score = tagScore.getValue();
                        poses.get(begin)
                                .add(Eojeol.builder().eojeol(sub + "/" + tag.name()).firstWord(sub).lastWord(sub)
                                        .firstTag(tag).lastTag(tag).start(offset + begin).end(offset + end).score(score)
                                        .isCompound(false).isUnknown(true).build());
                    }
                }

                /**
                 * 어간+어미(lemmas) 추후 필요시 추가
                 */
            }
        }

        return poses;
    }

    private Map<Pos, Double> getTagScore(String word) {
        Map<Pos, Double> tagScores = new HashMap<>();
        for (Pos pos : pos2words.keySet()) {
            if (pos2words.get(pos).containsKey(word)) {
                tagScores.put(pos, pos2words.get(pos).get(word));
            }
        }
        return tagScores;
    }

    private Map<Pos, Double> guessTag() {
        Map<Pos, Double> tagScores = new HashMap<>();

        tagScores.put(Pos.NNG, UNKNOWN_PENALTY);
        tagScores.put(Pos.VA, UNKNOWN_PENALTY);
        tagScores.put(Pos.VV, UNKNOWN_PENALTY);
        tagScores.put(Pos.VX, UNKNOWN_PENALTY);
        tagScores.put(Pos.UNK, UNKNOWN_PENALTY);

        return tagScores;
    }

    protected abstract void separateFeatures();

    public Map<StateFeature, Double> getStateFeatures() {
        return this.stateFeatures;
    }

    public Map<Transition, Double> getTransitions() {
        return this.transitions;
    }

    @Getter
    public static class StateFeature {

        private String feature;
        private Pos pos;

        private StateFeature(String feature, Pos pos) {
            this.feature = feature;
            this.pos = pos;
        }

        static StateFeature of(String feature, Pos pos) {
            return new StateFeature(feature, pos);
        }
    }

    @Getter
    public static class Transition {
        private Pos prevPos;
        private Pos nextPos;

        private Transition(Pos prevPos, Pos nextPos) {
            this.prevPos = prevPos;
            this.nextPos = nextPos;
        }

        static Transition of(Pos prevPos, Pos nextPos) {
            return new Transition(prevPos, nextPos);
        }
    }

}
