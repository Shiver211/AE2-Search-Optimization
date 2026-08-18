/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.recentsearch;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedHashMap;
import java.util.Map;

/** JSON conversion kept separate from Minecraft file access for testing. */
final class RecentSearchJson {

    private RecentSearchJson() {
    }

    static Map<String, RecentSearchHistory> read(final JsonObject root) {
        final Map<String, RecentSearchHistory> histories = new LinkedHashMap<>();
        final JsonObject players = getObject(root, "players");
        if (players != null) {
            for (final Map.Entry<String, JsonElement> player : players.entrySet()) {
                if (!player.getValue().isJsonArray()) {
                    continue;
                }

                final RecentSearchHistory history = new RecentSearchHistory();
                for (final JsonElement element : player.getValue().getAsJsonArray()) {
                    final RecentSearchHistory.SearchEntry parsed = parseEntry(element);
                    if (parsed != null) {
                        history.addLoadedEntry(parsed.getValue(), parsed.isFavorite());
                    }
                }
                histories.put(player.getKey(), history);
            }
        }

        final JsonObject settings = getObject(root, "settings");
        if (settings != null) {
            for (final Map.Entry<String, JsonElement> player : settings.entrySet()) {
                if (!player.getValue().isJsonObject()) {
                    continue;
                }

                final RecentSearchHistory history = getOrCreate(histories, player.getKey());
                final JsonObject state = player.getValue().getAsJsonObject();
                final Boolean enabled = getBoolean(state, "enabled");
                final Boolean applyOnClick = getBoolean(state, "applyOnClick");
                final Boolean syncExternalSearch = getBoolean(state, "syncExternalSearch");
                final Boolean deleteButtonsEnabled = getBoolean(state, "deleteButtonsEnabled");
                final Boolean favoritesEnabled = getBoolean(state, "favoritesEnabled");
                final Boolean keyboardNavigationEnabled = getBoolean(state, "keyboardNavigationEnabled");

                if (enabled != null) {
                    history.setEnabled(enabled);
                }
                if (applyOnClick != null) {
                    history.setApplyOnClick(applyOnClick);
                }
                if (syncExternalSearch != null) {
                    history.setSyncExternalSearch(syncExternalSearch);
                }
                if (deleteButtonsEnabled != null) {
                    history.setDeleteButtonsEnabled(deleteButtonsEnabled);
                }
                if (favoritesEnabled != null) {
                    history.setFavoritesEnabled(favoritesEnabled);
                }
                if (keyboardNavigationEnabled != null) {
                    history.setKeyboardNavigationEnabled(keyboardNavigationEnabled);
                }
            }
        }

        return histories;
    }

    static JsonObject write(final Map<String, RecentSearchHistory> histories) {
        final JsonObject players = new JsonObject();
        final JsonObject settings = new JsonObject();

        for (final Map.Entry<String, RecentSearchHistory> player : histories.entrySet()) {
            final RecentSearchHistory history = player.getValue();
            final JsonArray values = new JsonArray();
            for (final RecentSearchHistory.SearchEntry entry : history.getEntries()) {
                final JsonObject value = new JsonObject();
                value.addProperty("value", entry.getValue());
                value.addProperty("favorite", entry.isFavorite());
                values.add(value);
            }
            players.add(player.getKey(), values);

            final JsonObject state = new JsonObject();
            state.addProperty("enabled", history.isEnabled());
            state.addProperty("applyOnClick", history.isApplyOnClick());
            state.addProperty("syncExternalSearch", history.isSyncExternalSearch());
            state.addProperty("deleteButtonsEnabled", history.isDeleteButtonsEnabled());
            state.addProperty("favoritesEnabled", history.isFavoritesEnabled());
            state.addProperty("keyboardNavigationEnabled", history.isKeyboardNavigationEnabled());
            settings.add(player.getKey(), state);
        }

        final JsonObject root = new JsonObject();
        root.add("players", players);
        root.add("settings", settings);
        return root;
    }

    private static RecentSearchHistory.SearchEntry parseEntry(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return new RecentSearchHistory.SearchEntry(primitive.getAsString(), false);
            }
            return null;
        }

        if (!element.isJsonObject()) {
            return null;
        }

        final JsonObject object = element.getAsJsonObject();
        final JsonElement value = object.get("value");
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            return null;
        }

        final Boolean favorite = getBoolean(object, "favorite");
        return new RecentSearchHistory.SearchEntry(value.getAsString(), favorite != null && favorite);
    }

    private static RecentSearchHistory getOrCreate(
            final Map<String, RecentSearchHistory> histories,
            final String playerKey) {
        RecentSearchHistory history = histories.get(playerKey);
        if (history == null) {
            history = new RecentSearchHistory();
            histories.put(playerKey, history);
        }
        return history;
    }

    private static JsonObject getObject(final JsonObject parent, final String name) {
        final JsonElement element = parent.get(name);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static Boolean getBoolean(final JsonObject object, final String name) {
        final JsonElement element = object.get(name);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            return null;
        }
        return element.getAsBoolean();
    }
}
