package com.yaincoding.hulbert.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.yaincoding.hulbert.pos.Pos;

public final class Transition {

    private Map<StateFeature, Double> stateFeatures;

    public Transition(JsonObject obj) {
        stateFeatures = new HashMap<>();
        for (Entry<String, JsonElement> entry : obj.entrySet()) {
            String stateFeature = entry.getKey();
            String[] tokens = stateFeature.split(" -> ");
            StateFeature sf = StateFeature.builder().feature(tokens[0]).pos(Pos.valueOf(tokens[1])).build();

            stateFeatures.put(sf, entry.getValue().getAsDouble());
        }
    }

    public Double getScore(StateFeature sf) {
        return stateFeatures.getOrDefault(sf, 0.0);
    }
}
