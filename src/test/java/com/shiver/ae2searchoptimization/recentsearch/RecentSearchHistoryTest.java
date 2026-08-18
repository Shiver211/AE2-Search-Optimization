package com.shiver.ae2searchoptimization.recentsearch;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecentSearchHistoryTest {

    @Test
    void recordsWithoutDuplicatesAndPreservesFavoriteStateWhenReordered() {
        final RecentSearchHistory history = new RecentSearchHistory();
        history.record("iron");
        history.record("gold");
        history.toggleFavoriteForSearch("iron");
        history.record("iron");

        assertEquals(Arrays.asList("iron", "gold"), values(history.getEntries()));
        assertTrue(history.isFavorite("iron"));
    }

    @Test
    void favoritesAreSortedFirstAndVisibleCountIsAppliedAfterSorting() {
        final RecentSearchHistory history = new RecentSearchHistory();
        history.record("one");
        history.record("two");
        history.record("three");
        history.toggleFavoriteForSearch("one");
        history.toggleFavoriteForSearch("three");

        assertEquals(Arrays.asList("three", "one"), values(history.getVisibleEntries(2)));
        history.setFavoritesEnabled(false);
        assertEquals(Arrays.asList("three", "one", "two"), values(history.getVisibleEntries(10)));
    }

    @Test
    void removesAndClearsEntries() {
        final RecentSearchHistory history = new RecentSearchHistory();
        history.record("iron");
        history.record("gold");

        assertTrue(history.remove("iron"));
        assertFalse(history.remove("missing"));
        history.clear();
        assertTrue(history.getEntries().isEmpty());
    }

    @Test
    void disabledHistoryDoesNotRecordNewSearches() {
        final RecentSearchHistory history = new RecentSearchHistory();
        history.setEnabled(false);
        history.record("iron");

        assertTrue(history.getEntries().isEmpty());
    }

    private static List<String> values(final List<RecentSearchHistory.SearchEntry> entries) {
        final String[] values = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            values[i] = entries.get(i).getValue();
        }
        return Arrays.asList(values);
    }
}
