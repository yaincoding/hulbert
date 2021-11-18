package com.yaincoding.hulbert.tag;

import java.util.List;

import com.yaincoding.hulbert.representation.Eojeols;

public interface Tagger {

    List<Eojeols> tag(String sentence, boolean flatten);

    List<Eojeols> tag(String sentence, boolean flatten, double aSyllablePenalty, double nounPreference,
            double longerNounPreference, double unknownPenalty);
}
