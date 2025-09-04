package com.trevari.project.search;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SearchQuery {
    private final String original;
    private final String left;
    private final String right;
    private final SearchStrategy strategy;

    public String original() { return original; }
    public String left() { return left; }
    public String right() { return right; }
    public SearchStrategy strategy() { return strategy; }
}
