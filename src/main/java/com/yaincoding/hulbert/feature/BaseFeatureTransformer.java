package com.yaincoding.hulbert.feature;

import java.util.ArrayList;
import java.util.List;
import com.yaincoding.hulbert.pos.Pos;

public class BaseFeatureTransformer extends FeatureTransformer {

	@Override
	protected List<String> toFeature(List<String> words, List<Pos> tags, int i) {
		List<String> features = new ArrayList<>();

		features.add(String.format("x[0]=%s", words.get(i)));
		features.add(String.format("x[0]=%s, y[-1]=%s", words.get(i), tags.get(i - 1)));
		features.add(String.format("x[-1:0]=%s-%s", words.get(i - 1), words.get(i)));
		features.add(
				String.format("x[-1:0]=%s-%s, y[-1]=%s", words.get(i - 1), words.get(i), tags.get(i - 1)));
		features.add(String.format("x[-1,1]=%s-%s", words.get(i - 1), words.get(i + 1)));
		features.add(String.format("x[-1,1]=%s-%s, y[-1]=%s", words.get(i - 1), words.get(i + 1),
				tags.get(i - 1)));

		return features;
	}
}
