package com.yaincoding.hulbert.model;

import com.yaincoding.hulbert.pos.Pos;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class StateFeature {

    private String feature;
    private Pos pos;
}
