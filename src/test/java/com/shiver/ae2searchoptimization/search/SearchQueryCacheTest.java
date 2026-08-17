package com.shiver.ae2searchoptimization.search;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchQueryCacheTest {

    @Test
    void reusesCompiledPatternForTheSameQuery() {
        final Pattern first = SearchQueryCache.compile("iron.*ore", Pattern.CASE_INSENSITIVE);
        final Pattern second = SearchQueryCache.compile("iron.*ore", Pattern.CASE_INSENSITIVE);

        assertSame(first, second);
        assertTrue(first.matcher("Iron Ore").find());
    }

    @Test
    void treatsAnInvalidRegexAsLiteralLikeAe2() {
        final Pattern pattern = SearchQueryCache.compile("[iron", Pattern.CASE_INSENSITIVE);

        assertTrue(pattern.matcher("[IRON").find());
    }

    @Test
    void reusesSplitTermsForTheSameQuery() {
        final String[] first = SearchQueryCache.split("iron ore", " ");
        final String[] second = SearchQueryCache.split("iron ore", " ");

        assertSame(first, second);
        assertArrayEquals(new String[]{"iron", "ore"}, first);
    }
}
