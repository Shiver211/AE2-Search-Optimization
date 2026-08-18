/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.recentsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.shiver.ae2searchoptimization.AE2SearchOptimizationConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Client-local, player-specific storage for recent AE2 searches. */
public final class SearchHistoryStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String UNKNOWN_PLAYER = "unknown";
    private static final String FILE_NAME = "ae2_recent_search_history.json";

    private static final Map<String, RecentSearchHistory> HISTORY_BY_PLAYER = new LinkedHashMap<>();
    private static String loadedPlayerKey;
    private static boolean loaded;

    private SearchHistoryStore() {
    }

    public static List<RecentSearchHistory.SearchEntry> getVisibleEntries() {
        ensureLoaded();
        return currentHistory().getVisibleEntries(AE2SearchOptimizationConfig.getMaxVisibleRecentEntries());
    }

    public static boolean isEnabled() {
        ensureLoaded();
        return currentHistory().isEnabled();
    }

    public static void setEnabled(final boolean enabled) {
        ensureLoaded();
        currentHistory().setEnabled(enabled);
        save();
    }

    public static boolean isApplyOnClick() {
        ensureLoaded();
        return currentHistory().isApplyOnClick();
    }

    public static void setApplyOnClick(final boolean applyOnClick) {
        ensureLoaded();
        currentHistory().setApplyOnClick(applyOnClick);
        save();
    }

    public static boolean isSyncExternalSearch() {
        ensureLoaded();
        return currentHistory().isSyncExternalSearch();
    }

    public static void setSyncExternalSearch(final boolean syncExternalSearch) {
        ensureLoaded();
        currentHistory().setSyncExternalSearch(syncExternalSearch);
        save();
    }

    public static boolean isDeleteButtonsEnabled() {
        ensureLoaded();
        return currentHistory().isDeleteButtonsEnabled();
    }

    public static void setDeleteButtonsEnabled(final boolean deleteButtonsEnabled) {
        ensureLoaded();
        currentHistory().setDeleteButtonsEnabled(deleteButtonsEnabled);
        save();
    }

    public static boolean isFavoritesEnabled() {
        ensureLoaded();
        return currentHistory().isFavoritesEnabled();
    }

    public static void setFavoritesEnabled(final boolean favoritesEnabled) {
        ensureLoaded();
        currentHistory().setFavoritesEnabled(favoritesEnabled);
        save();
    }

    public static boolean isKeyboardNavigationEnabled() {
        ensureLoaded();
        return currentHistory().isKeyboardNavigationEnabled();
    }

    public static void setKeyboardNavigationEnabled(final boolean keyboardNavigationEnabled) {
        ensureLoaded();
        currentHistory().setKeyboardNavigationEnabled(keyboardNavigationEnabled);
        save();
    }

    public static void record(final String value) {
        ensureLoaded();
        currentHistory().record(value);
        saveIfEnabled(value);
    }

    public static void remove(final String value) {
        ensureLoaded();
        if (currentHistory().remove(value)) {
            save();
        }
    }

    public static void toggleFavoriteForSearch(final String value) {
        ensureLoaded();
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        currentHistory().toggleFavoriteForSearch(value);
        save();
    }

    public static boolean isFavorite(final String value) {
        ensureLoaded();
        return currentHistory().isFavorite(value);
    }

    public static void clear() {
        ensureLoaded();
        currentHistory().clear();
        save();
    }

    private static void saveIfEnabled(final String value) {
        if (value != null && !value.trim().isEmpty() && currentHistory().isEnabled()) {
            save();
        }
    }

    private static RecentSearchHistory currentHistory() {
        final String playerKey = currentPlayerKey();
        RecentSearchHistory history = HISTORY_BY_PLAYER.get(playerKey);
        if (history == null) {
            history = new RecentSearchHistory();
            HISTORY_BY_PLAYER.put(playerKey, history);
        }
        return history;
    }

    private static void ensureLoaded() {
        final String playerKey = currentPlayerKey();
        if (loaded && playerKey.equals(loadedPlayerKey)) {
            return;
        }

        loaded = true;
        loadedPlayerKey = playerKey;
        HISTORY_BY_PLAYER.clear();
        load();
    }

    private static String currentPlayerKey() {
        final Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.getSession() == null) {
            return UNKNOWN_PLAYER;
        }

        final Session session = minecraft.getSession();
        try {
            final GameProfile profile = session.getProfile();
            final UUID uuid = profile == null ? null : profile.getId();
            if (uuid != null) {
                return uuid.toString();
            }
        } catch (final RuntimeException ignored) {
            // Fall back to the username for offline or incomplete sessions.
        }
        return session.getUsername() == null ? UNKNOWN_PLAYER : session.getUsername();
    }

    private static Path filePath() {
        final File configDirectory = new File(Minecraft.getMinecraft().gameDir, "config");
        return new File(configDirectory, FILE_NAME).toPath();
    }

    private static void load() {
        final Path path = filePath();
        if (!Files.isRegularFile(path)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            final JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null) {
                HISTORY_BY_PLAYER.putAll(RecentSearchJson.read(root));
            }
        } catch (final Exception ignored) {
            // A malformed local history must never prevent the game from starting.
        }
    }

    private static void save() {
        final Path path = filePath();
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(RecentSearchJson.write(HISTORY_BY_PLAYER), writer);
            }
        } catch (final IOException ignored) {
            // Search history is optional local convenience data.
        }
    }
}
