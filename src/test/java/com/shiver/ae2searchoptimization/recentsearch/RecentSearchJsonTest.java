package com.shiver.ae2searchoptimization.recentsearch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecentSearchJsonTest {

    @Test
    void readsLegacyStringsAndNewEntriesWithDuplicateMerging() {
        final JsonObject root = new JsonParser().parse(
                "{\"players\":{"
                        + "\"player-one\":[\"iron\",{\"value\":\"gold\",\"favorite\":true},"
                        + "{\"value\":\"iron\",\"favorite\":true},null,3],"
                        + "\"player-two\":[\"diamond\"]},"
                        + "\"settings\":{\"player-one\":{\"enabled\":false,"
                        + "\"applyOnClick\":false,\"keyboardNavigationEnabled\":false},"
                        + "\"ignored\":\"not-an-object\"}}").getAsJsonObject();

        final Map<String, RecentSearchHistory> histories = RecentSearchJson.read(root);
        final RecentSearchHistory first = histories.get("player-one");

        assertEquals(Arrays.asList("iron", "gold"), values(first.getEntries()));
        assertTrue(first.isFavorite("iron"));
        assertFalse(first.isEnabled());
        assertFalse(first.isApplyOnClick());
        assertFalse(first.isKeyboardNavigationEnabled());
        assertEquals(Arrays.asList("diamond"), values(histories.get("player-two").getEntries()));
    }

    @Test
    void malformedFieldsAreIgnoredAndWriteKeepsPlayerIsolation() {
        final JsonObject root = new JsonParser().parse(
                "{\"players\":{\"one\":[{\"value\":\"iron\",\"favorite\":\"yes\"},"
                        + "{\"value\":5},false]},\"settings\":{\"one\":{\"enabled\":\"yes\"}}}")
                .getAsJsonObject();
        final Map<String, RecentSearchHistory> histories = RecentSearchJson.read(root);

        assertEquals(Arrays.asList("iron"), values(histories.get("one").getEntries()));
        assertTrue(histories.get("one").isEnabled());

        final RecentSearchHistory other = new RecentSearchHistory();
        other.record("gold");
        histories.put("two", other);
        final JsonObject written = RecentSearchJson.write(histories);
        final Map<String, RecentSearchHistory> roundTrip = RecentSearchJson.read(written);
        assertEquals(Arrays.asList("iron"), values(roundTrip.get("one").getEntries()));
        assertEquals(Arrays.asList("gold"), values(roundTrip.get("two").getEntries()));
    }

    private static List<String> values(final List<RecentSearchHistory.SearchEntry> entries) {
        final String[] values = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            values[i] = entries.get(i).getValue();
        }
        return Arrays.asList(values);
    }
}
