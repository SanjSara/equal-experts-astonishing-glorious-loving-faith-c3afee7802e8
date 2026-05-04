package com.gist.domain;

import java.util.List;

/**
 * Immutable domain value object representing a public GitHub Gist.
 */
public record Gist(
        String id,
        String description,
        String htmlUrl,
        List<String> files
) {
    public Gist {
        files = files != null ? List.copyOf(files) : List.of();
    }
}
