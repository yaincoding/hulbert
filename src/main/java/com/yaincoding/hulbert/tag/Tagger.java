package com.yaincoding.hulbert.tag;

import java.util.List;
import com.yaincoding.hulbert.representation.WordPos;

public interface Tagger {

    List<WordPos> tag(String sentence, boolean flatten, boolean debug);
}
