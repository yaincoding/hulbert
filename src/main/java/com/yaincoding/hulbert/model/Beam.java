package com.yaincoding.hulbert.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.yaincoding.hulbert.pos.Pos;
import com.yaincoding.hulbert.representation.Eojeol;
import com.yaincoding.hulbert.representation.Eojeols;

import lombok.Builder;

@Builder
public class Beam {

    private final int topK;
    private final double unknownPenalty;
    private final double aSyllablePenalty;
    private final double nounPreference;
    private final double longerNounPreference;
    private final List<List<Eojeols>> beam = new ArrayList<>() {
        {
            Eojeol bos = Eojeol.builder().eojeol("BOS/" + Pos.BOS.name()).firstWord("BOS").firstTag(Pos.BOS)
                    .lastWord("BOS").lastTag(Pos.BOS).start(0).end(0).score(0.0).isCompound(false).isUnknown(false)
                    .build();
            Eojeols eojeols = Eojeols.of(Collections.singletonList(bos), 0.0);
            add(Collections.singletonList(eojeols));
        }
    };
    private final Model model;

    public void append(List<Eojeols> candidates) {
        candidates = candidates.stream().sorted(Comparator.comparingDouble(Eojeols::getScore).reversed()).limit(topK)
                .collect(Collectors.toList());
        beam.add(candidates);
    }

    public List<Eojeols> search(List<List<Eojeol>> beginIndex, String chars) {

        for (int end = 1; end <= chars.length(); end++) {
            List<Eojeols> matures = new ArrayList<>();
            for (int begin = Math.max(0, end - model.MAX_WORD_LEN); begin < end; begin++) {
                List<Eojeols> immatures = beam.get(begin);
                final int endCopy = end;
                List<Eojeol> appendingEojeols = beginIndex.get(begin).stream()
                        .filter(eojeol -> eojeol.getEnd() == endCopy).collect(Collectors.toList());
                if (appendingEojeols.isEmpty()) {
                    String sub = chars.substring(begin, end);
                    appendingEojeols = Collections.singletonList(Eojeol.builder().eojeol(sub + "/" + Pos.UNK.name())
                            .firstWord(sub).lastWord(sub).firstTag(Pos.UNK).lastTag(Pos.UNK).start(begin).end(end)
                            .score(unknownPenalty).isCompound(false).isUnknown(true).build());
                }
                appending(immatures, appendingEojeols, matures);
            }
            append(matures);
        }

        Eojeol eos = Eojeol.builder().eojeol("EOS/" + Pos.EOS.name()).firstWord("EOS").firstTag(Pos.EOS).lastWord("EOS")
                .lastTag(Pos.EOS).start(chars.length()).end(chars.length()).score(0.0).isCompound(false)
                .isUnknown(false).build();

        List<Eojeols> matures = new ArrayList<>();
        appending(beam.get(beam.size() - 1), Collections.singletonList(eos), matures);
        append(matures);

        return beam.get(beam.size() - 1);
    }

    private void appending(List<Eojeols> immatures, List<Eojeol> appendingWords, List<Eojeols> matures) {
        for (Eojeols immature : immatures) {
            for (Eojeol eojeol : appendingWords) {
                List<Eojeol> eojeols = new ArrayList<>();
                eojeols.addAll(immature.getEojeols());
                eojeols.add(eojeol);
                double score = immature.getScore();
                score += preferenceScore(eojeol);
                score += model.score(immature, eojeol);
                matures.add(Eojeols.of(eojeols, score));
            }
        }
    }

    private double preferenceScore(Eojeol eojeol) {
        double score = 0.0;
        if (eojeol.length() == 1) {
            score += aSyllablePenalty;
            if (eojeol.isNoun()) {
                score += aSyllablePenalty * nounPreference;
            }
        }

        if (eojeol.isNoun() && eojeol.length() > 1 && !eojeol.isUnknown()) {
            score += nounPreference;
        }

        if (eojeol.isNoun()) {
            score += longerNounPreference * (eojeol.length() - 1);
        }

        return score;
    }

}
