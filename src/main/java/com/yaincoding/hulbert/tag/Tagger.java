package com.yaincoding.hulbert.tag;

import java.util.List;

import com.yaincoding.hulbert.representation.Eojeols;

public interface Tagger {

    List<Eojeols> tag(String sentence, boolean flatten, boolean debug);
}
