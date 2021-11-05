package com.yaincoding.hulbert.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yaincoding.hulbert.pos.Pos;

import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public final class Model {

    private Map<StateFeature, Double> stateFeatures;
    private Map<Transition, Double> transitions;

    @PostConstruct
    private void loadJsonModel() throws IOException {
        String modelPath = System.getenv("model_path");
        String jsonString = Files.readString(Paths.get(modelPath), StandardCharsets.UTF_8);
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

        JsonObject stateFeaturesJsonObject = jsonObject.get("state_features").getAsJsonObject();
        this.stateFeatures = createStateFeatureModel(stateFeaturesJsonObject);

        JsonObject transitionsJsonObject = jsonObject.get("transitions").getAsJsonObject();
        this.transitions = createTransitionModel(transitionsJsonObject);
    }

    private Map<StateFeature, Double> createStateFeatureModel(JsonObject stateFeaturesJsonObject) {
        Map<StateFeature, Double> stateFeatures = new HashMap<>();
        for (Entry<String, JsonElement> entry : stateFeaturesJsonObject.entrySet()) {
            String stateFeature = entry.getKey();
            double score = entry.getValue().getAsDouble();

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

            transitions.put(t, score);
        }

        return transitions;
    }

    public Double getScore(StateFeature sf) {
        return stateFeatures.getOrDefault(sf, 0.0);
    }

    public Double getScore(Transition t) {
        return transitions.getOrDefault(t, 0.0);
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
