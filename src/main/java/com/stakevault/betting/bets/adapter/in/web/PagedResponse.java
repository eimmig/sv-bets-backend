package com.stakevault.betting.bets.adapter.in.web;

import java.util.List;
import java.util.function.Function;

import com.stakevault.betting.bets.domain.model.PagedResult;

public record PagedResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	static <S, T> PagedResponse<T> from(PagedResult<S> result, Function<S, T> mapper) {
		return new PagedResponse<>(result.content().stream().map(mapper).toList(), result.page(), result.size(),
				result.totalElements(), result.totalPages());
	}
}
