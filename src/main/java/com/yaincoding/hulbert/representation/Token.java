package com.yaincoding.hulbert.representation;

import com.yaincoding.hulbert.pos.Pos;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Token {
	
	private final String word;
	private final Pos pos;
	private final int begin;
	private final int end;
	private final double score;
}
