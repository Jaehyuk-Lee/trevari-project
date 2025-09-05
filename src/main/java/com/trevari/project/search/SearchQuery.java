package com.trevari.project.search;

public record SearchQuery(String query, String left, String right, SearchStrategy strategy) {
}
