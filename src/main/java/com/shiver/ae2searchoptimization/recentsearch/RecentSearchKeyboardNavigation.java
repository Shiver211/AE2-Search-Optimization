/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.recentsearch;

import appeng.client.gui.widgets.MEGuiTextField;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Keyboard selection state for each open AE2 search field. */
public final class RecentSearchKeyboardNavigation {

    private static final Map<MEGuiTextField, String> SELECTED_VALUES = new WeakHashMap<>();

    private RecentSearchKeyboardNavigation() {
    }

    public static boolean moveSelection(final MEGuiTextField searchField, final int direction) {
        if (!isNavigationAvailable(searchField)) {
            clear(searchField);
            return false;
        }

        final List<RecentSearchHistory.SearchEntry> entries = SearchHistoryStore.getVisibleEntries();
        if (entries.isEmpty()) {
            clear(searchField);
            return false;
        }

        final int currentIndex = selectedIndex(entries, SELECTED_VALUES.get(searchField));
        final int nextIndex = nextIndex(currentIndex, entries.size(), direction);

        SELECTED_VALUES.put(searchField, entries.get(nextIndex).getValue());
        return true;
    }

    public static RecentSearchOverlay.ClickTarget selectedTarget(final MEGuiTextField searchField) {
        final String value = selectedValue(searchField);
        return value == null
                ? null
                : new RecentSearchOverlay.ClickTarget(RecentSearchOverlay.ClickTargetType.ENTRY, value);
    }

    public static String selectedValue(final MEGuiTextField searchField) {
        if (!isNavigationAvailable(searchField)) {
            clear(searchField);
            return null;
        }

        final List<RecentSearchHistory.SearchEntry> entries = SearchHistoryStore.getVisibleEntries();
        final String value = SELECTED_VALUES.get(searchField);
        if (selectedIndex(entries, value) < 0) {
            clear(searchField);
            return null;
        }
        return value;
    }

    public static void clear(final MEGuiTextField searchField) {
        if (searchField != null) {
            SELECTED_VALUES.remove(searchField);
        }
    }

    static int nextIndex(final int currentIndex, final int size, final int direction) {
        if (size <= 0) {
            return -1;
        }
        if (currentIndex < 0) {
            return direction > 0 ? 0 : size - 1;
        }
        return Math.floorMod(currentIndex + direction, size);
    }

    private static boolean isNavigationAvailable(final MEGuiTextField searchField) {
        return SearchHistoryStore.isKeyboardNavigationEnabled()
                && RecentSearchOverlay.shouldShow(searchField);
    }

    private static int selectedIndex(
            final List<RecentSearchHistory.SearchEntry> entries,
            final String value) {
        if (value == null) {
            return -1;
        }

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getValue().equals(value)) {
                return i;
            }
        }
        return -1;
    }
}
