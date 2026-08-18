/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.recentsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The player-local history model. It intentionally contains no Minecraft
 * classes so its ordering and mutation rules can be tested independently.
 */
public final class RecentSearchHistory {

    private final List<SearchEntry> entries = new ArrayList<>();
    private boolean enabled = true;
    private boolean applyOnClick = true;
    private boolean syncExternalSearch = true;
    private boolean deleteButtonsEnabled = true;
    private boolean favoritesEnabled = true;
    private boolean keyboardNavigationEnabled = true;

    public List<SearchEntry> getVisibleEntries(final int maxVisibleEntries) {
        final List<SearchEntry> ordered = new ArrayList<>(entries.size());
        if (favoritesEnabled) {
            for (final SearchEntry entry : entries) {
                if (entry.isFavorite()) {
                    ordered.add(entry);
                }
            }
            for (final SearchEntry entry : entries) {
                if (!entry.isFavorite()) {
                    ordered.add(entry);
                }
            }
        } else {
            ordered.addAll(entries);
        }

        final int visibleCount = Math.min(Math.max(0, maxVisibleEntries), ordered.size());
        return Collections.unmodifiableList(new ArrayList<>(ordered.subList(0, visibleCount)));
    }

    public List<SearchEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void record(final String value) {
        if (!enabled || isBlank(value)) {
            return;
        }

        boolean favorite = false;
        for (int i = 0; i < entries.size(); i++) {
            final SearchEntry entry = entries.get(i);
            if (entry.getValue().equals(value)) {
                favorite = entry.isFavorite();
                entries.remove(i);
                break;
            }
        }

        entries.add(0, new SearchEntry(value, favorite));
    }

    public boolean remove(final String value) {
        if (isBlank(value)) {
            return false;
        }

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getValue().equals(value)) {
                entries.remove(i);
                return true;
            }
        }
        return false;
    }

    public void setFavorite(final String value, final boolean favorite) {
        if (isBlank(value)) {
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            final SearchEntry entry = entries.get(i);
            if (!entry.getValue().equals(value)) {
                continue;
            }

            if (entry.isFavorite() == favorite) {
                return;
            }

            entries.set(i, new SearchEntry(value, favorite));
            if (favorite) {
                entries.add(0, entries.remove(i));
            }
            return;
        }
    }

    public void toggleFavorite(final String value) {
        if (isBlank(value)) {
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            final SearchEntry entry = entries.get(i);
            if (!entry.getValue().equals(value)) {
                continue;
            }

            if (entry.isFavorite()) {
                entries.set(i, new SearchEntry(value, false));
            } else {
                entries.add(0, new SearchEntry(value, true));
                entries.remove(i + 1);
            }
            return;
        }
    }

    public void toggleFavoriteForSearch(final String value) {
        if (isBlank(value)) {
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            final SearchEntry entry = entries.get(i);
            if (!entry.getValue().equals(value)) {
                continue;
            }

            if (entry.isFavorite()) {
                entries.set(i, new SearchEntry(value, false));
            } else {
                entries.add(0, new SearchEntry(value, true));
                entries.remove(i + 1);
            }
            return;
        }

        entries.add(0, new SearchEntry(value, true));
    }

    public boolean isFavorite(final String value) {
        if (isBlank(value)) {
            return false;
        }

        for (final SearchEntry entry : entries) {
            if (entry.getValue().equals(value)) {
                return entry.isFavorite();
            }
        }
        return false;
    }

    public void addLoadedEntry(final String value, final boolean favorite) {
        if (isBlank(value)) {
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            final SearchEntry existing = entries.get(i);
            if (!existing.getValue().equals(value)) {
                continue;
            }

            if (favorite && !existing.isFavorite()) {
                entries.set(i, new SearchEntry(value, true));
            }
            return;
        }

        entries.add(new SearchEntry(value, favorite));
    }

    public void clear() {
        entries.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isApplyOnClick() {
        return applyOnClick;
    }

    public void setApplyOnClick(final boolean applyOnClick) {
        this.applyOnClick = applyOnClick;
    }

    public boolean isSyncExternalSearch() {
        return syncExternalSearch;
    }

    public void setSyncExternalSearch(final boolean syncExternalSearch) {
        this.syncExternalSearch = syncExternalSearch;
    }

    public boolean isDeleteButtonsEnabled() {
        return deleteButtonsEnabled;
    }

    public void setDeleteButtonsEnabled(final boolean deleteButtonsEnabled) {
        this.deleteButtonsEnabled = deleteButtonsEnabled;
    }

    public boolean isFavoritesEnabled() {
        return favoritesEnabled;
    }

    public void setFavoritesEnabled(final boolean favoritesEnabled) {
        this.favoritesEnabled = favoritesEnabled;
    }

    public boolean isKeyboardNavigationEnabled() {
        return keyboardNavigationEnabled;
    }

    public void setKeyboardNavigationEnabled(final boolean keyboardNavigationEnabled) {
        this.keyboardNavigationEnabled = keyboardNavigationEnabled;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    public static final class SearchEntry {
        private final String value;
        private final boolean favorite;

        public SearchEntry(final String value, final boolean favorite) {
            this.value = value;
            this.favorite = favorite;
        }

        public String getValue() {
            return value;
        }

        public boolean isFavorite() {
            return favorite;
        }
    }
}
