package com.gist.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gist record")
class GistTest {

    @Test
    @DisplayName("preserves all fields and makes files defensively immutable")
    void preservesFieldsAndDefensiveCopy() {
        var gist = new Gist("abc123", "My Gist", "https://gist.github.com/abc123", List.of("main.py", "util.py"));

        assertThat(gist.id()).isEqualTo("abc123");
        assertThat(gist.description()).isEqualTo("My Gist");
        assertThat(gist.htmlUrl()).isEqualTo("https://gist.github.com/abc123");
        assertThat(gist.files()).containsExactly("main.py", "util.py");
    }

    @Test
    @DisplayName("defaults files to empty list when null is passed")
    void nullFilesDefaultsToEmptyList() {
        var gist = new Gist("1", "desc", "url", null);

        assertThat(gist.files()).isEmpty();
    }
}
