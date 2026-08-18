/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.recentsearch;

import appeng.client.gui.widgets.MEGuiTextField;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Legacy 1.12.2 renderer and hit testing for the recent-search dropdown. */
public final class RecentSearchOverlay {

    private static final int PADDING = 2;
    private static final int ROW_HEIGHT = 12;
    private static final int ACTION_BUTTON_SIZE = 9;
    private static final int TEXT_BUTTON_GAP = 4;
    private static final int SEARCH_ACTION_HIT_SIZE = 12;
    private static final int SEARCH_ACTION_RIGHT_OFFSET = 3;

    private static final int TEXT_COLOR = 0xFF303040;
    private static final int TEXT_HOVER_COLOR = 0xFF101020;
    private static final int BACKGROUND_COLOR = 0xFFE1E4F0;
    private static final int INNER_BACKGROUND_COLOR = 0xFFD2D6E6;
    private static final int HIGHLIGHT_BORDER_COLOR = 0xFFFFFFFF;
    private static final int SHADOW_BORDER_COLOR = 0xFF6F7488;
    private static final int HOVER_COLOR = 0xFFC1C8DE;
    private static final int KEYBOARD_SELECTED_COLOR = 0xFFAEB8D0;
    private static final int KEYBOARD_SELECTED_MARKER_COLOR = 0xFF5E6F99;
    private static final int SEPARATOR_COLOR = 0xFFB6BCCF;
    private static final int GROUP_SEPARATOR_COLOR = 0xFF9298AC;

    private static final int ACTION_BACKGROUND_COLOR = 0xFFDCE1EE;
    private static final int ACTION_HOVER_BACKGROUND_COLOR = 0xFFF0F3F9;
    private static final int ACTION_BORDER_LIGHT = 0xFFFFFFFF;
    private static final int ACTION_BORDER_DARK = 0xFF7A7F93;
    private static final int FAVORITE_ICON_COLOR = 0xFFE0B84C;
    private static final int FAVORITE_HOVER_COLOR = 0xFFFFD86A;
    private static final int FAVORITE_INACTIVE_COLOR = 0xFF8A8F9E;
    private static final int DELETE_ICON_COLOR = 0xFFC85E68;
    private static final int DELETE_HOVER_COLOR = 0xFFE9757D;
    private static final int CLEAR_ICON_COLOR = 0xFFC85E68;
    private static final int CLEAR_HOVER_COLOR = 0xFFE9757D;

    private RecentSearchOverlay() {
    }

    public static boolean shouldShow(final MEGuiTextField searchField) {
        return SearchHistoryStore.isEnabled()
                && searchField != null
                && searchField.getVisible()
                && searchField.isFocused()
                && !SearchHistoryStore.getVisibleEntries().isEmpty();
    }

    public static void render(
            final FontRenderer font,
            final MEGuiTextField searchField,
            final int mouseX,
            final int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        renderSearchClearButton(font, searchField, mouseX, mouseY);
        if (shouldShow(searchField)) {
            final GroupedEntries groupedEntries = groupEntries(SearchHistoryStore.getVisibleEntries());
            final int x = screenX(searchField, font);
            final int y = screenY(searchField, font);
            final int width = width(searchField, font);
            final int height = overlayHeight(groupedEntries);
            final String selectedValue = RecentSearchKeyboardNavigation.selectedValue(searchField);

            Gui.drawRect(x, y, x + width, y + height, BACKGROUND_COLOR);
            Gui.drawRect(x + 1, y + 1, x + width - 1, y + height - 1, INNER_BACKGROUND_COLOR);
            Gui.drawRect(x, y, x + width, y + 1, HIGHLIGHT_BORDER_COLOR);
            Gui.drawRect(x, y, x + 1, y + height, HIGHLIGHT_BORDER_COLOR);
            Gui.drawRect(x, y + height - 1, x + width, y + height, SHADOW_BORDER_COLOR);
            Gui.drawRect(x + width - 1, y, x + width, y + height, SHADOW_BORDER_COLOR);

            int rowY = y + PADDING;
            rowY = renderEntries(font, x, width, rowY, groupedEntries.favorites, selectedValue, mouseX, mouseY);
            if (groupedEntries.hasSeparator()) {
                Gui.drawRect(x + 3, rowY + 1, x + width - 4, rowY + 2, GROUP_SEPARATOR_COLOR);
                rowY += 4;
            }
            renderEntries(font, x, width, rowY, groupedEntries.recents, selectedValue, mouseX, mouseY);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static boolean isMouseOver(
            final MEGuiTextField searchField,
            final double mouseX,
            final double mouseY) {
        if (!shouldShow(searchField)) {
            return false;
        }

        final GroupedEntries groupedEntries = groupEntries(SearchHistoryStore.getVisibleEntries());
        final int x = screenX(searchField, null);
        final int y = screenY(searchField, null);
        final int width = width(searchField, null);
        final int height = overlayHeight(groupedEntries);
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static boolean isMouseOverSearchOrOverlay(
            final MEGuiTextField searchField,
            final double mouseX,
            final double mouseY) {
        return (isClearAllButtonVisible(searchField) && isSearchActionHovered(searchField, mouseX, mouseY))
                || (searchField != null
                && searchField.getVisible()
                && (searchField.isMouseIn((int) mouseX, (int) mouseY)
                || isMouseOver(searchField, mouseX, mouseY)));
    }

    public static ClickTarget getClickedTarget(
            final MEGuiTextField searchField,
            final double mouseX,
            final double mouseY) {
        if (isClearAllButtonVisible(searchField) && isSearchActionHovered(searchField, mouseX, mouseY)) {
            return new ClickTarget(ClickTargetType.CLEAR_ALL, null);
        }

        if (!shouldShow(searchField)) {
            return null;
        }

        final GroupedEntries groupedEntries = groupEntries(SearchHistoryStore.getVisibleEntries());
        final int x = screenX(searchField, null);
        final int y = screenY(searchField, null);
        final int width = width(searchField, null);
        final boolean showFavorite = SearchHistoryStore.isFavoritesEnabled();
        final boolean showDelete = SearchHistoryStore.isDeleteButtonsEnabled();

        int rowY = y + PADDING;
        ClickTarget target = getGroupClickTarget(
                groupedEntries.favorites, x, width, rowY, mouseX, mouseY, showFavorite, showDelete);
        if (target != null) {
            return target;
        }

        rowY += groupedEntries.favorites.size() * ROW_HEIGHT;
        if (groupedEntries.hasSeparator()) {
            rowY += 4;
        }
        return getGroupClickTarget(
                groupedEntries.recents, x, width, rowY, mouseX, mouseY, showFavorite, showDelete);
    }

    private static int renderEntries(
            final FontRenderer font,
            final int x,
            final int width,
            int rowY,
            final List<RecentSearchHistory.SearchEntry> entries,
            final String selectedValue,
            final int mouseX,
            final int mouseY) {
        final boolean showFavorite = SearchHistoryStore.isFavoritesEnabled();
        final boolean showDelete = SearchHistoryStore.isDeleteButtonsEnabled();
        for (final RecentSearchHistory.SearchEntry entry : entries) {
            drawEntryRow(
                    font, x, width, rowY, entry, selectedValue, mouseX, mouseY, showFavorite, showDelete);
            rowY += ROW_HEIGHT;
        }
        return rowY;
    }

    private static void drawEntryRow(
            final FontRenderer font,
            final int x,
            final int width,
            final int rowY,
            final RecentSearchHistory.SearchEntry entry,
            final String selectedValue,
            final int mouseX,
            final int mouseY,
            final boolean showFavorite,
            final boolean showDelete) {
        final ButtonLayout layout = buttonLayout(x, width, showFavorite, showDelete);
        final boolean hovered = isRowHovered(x, width, rowY, mouseX, mouseY);
        final boolean selected = entry.getValue().equals(selectedValue);
        if (selected) {
            Gui.drawRect(x + 1, rowY, x + width - 1, rowY + ROW_HEIGHT, KEYBOARD_SELECTED_COLOR);
            Gui.drawRect(x + 1, rowY + 1, x + 3, rowY + ROW_HEIGHT - 1, KEYBOARD_SELECTED_MARKER_COLOR);
        }
        if (hovered) {
            Gui.drawRect(x + 1, rowY, x + width - 1, rowY + ROW_HEIGHT, HOVER_COLOR);
        }

        final int textWidth = Math.max(0, width - 2 * PADDING - layout.textReserveWidth);
        final String value = font.trimStringToWidth(entry.getValue(), textWidth);
        font.drawString(value, x + PADDING, rowY + 2,
                hovered || selected ? TEXT_HOVER_COLOR : TEXT_COLOR);

        if (showFavorite) {
            final boolean favoriteHovered = isButtonHovered(layout.favoriteX, rowY + 1, mouseX, mouseY);
            drawFavoriteButton(layout.favoriteX, rowY + 1, entry.isFavorite(), favoriteHovered);
        }

        if (showDelete) {
            final boolean deleteHovered = isButtonHovered(layout.deleteX, rowY + 1, mouseX, mouseY);
            drawDeleteButton(layout.deleteX, rowY + 1, deleteHovered);
        }

        Gui.drawRect(x + 2, rowY + ROW_HEIGHT - 1, x + width - 3, rowY + ROW_HEIGHT, SEPARATOR_COLOR);
    }

    private static void renderSearchClearButton(
            final FontRenderer font,
            final MEGuiTextField searchField,
            final int mouseX,
            final int mouseY) {
        if (!isClearAllButtonVisible(searchField)) {
            return;
        }

        final int x = searchActionX(searchField, font);
        final int y = searchActionY(searchField, font);
        final boolean hovered = isSearchActionHovered(searchField, mouseX, mouseY);
        drawSmallButtonFrame(x + 1, y + 1, hovered);
        drawPixelX(x + 2, y + 2, hovered ? CLEAR_HOVER_COLOR : CLEAR_ICON_COLOR);
    }

    private static void drawFavoriteButton(
            final int x,
            final int y,
            final boolean favorite,
            final boolean hovered) {
        drawSmallButtonFrame(x, y, hovered);
        final int iconColor = hovered
                ? FAVORITE_HOVER_COLOR
                : favorite ? FAVORITE_ICON_COLOR : FAVORITE_INACTIVE_COLOR;
        drawPixelStar(x + 1, y + 1, iconColor);
    }

    private static void drawDeleteButton(final int x, final int y, final boolean hovered) {
        drawSmallButtonFrame(x, y, hovered);
        drawPixelX(x + 2, y + 2, hovered ? DELETE_HOVER_COLOR : DELETE_ICON_COLOR);
    }

    private static void drawSmallButtonFrame(final int x, final int y, final boolean hovered) {
        Gui.drawRect(x, y, x + ACTION_BUTTON_SIZE, y + ACTION_BUTTON_SIZE,
                hovered ? ACTION_HOVER_BACKGROUND_COLOR : ACTION_BACKGROUND_COLOR);
        Gui.drawRect(x, y, x + ACTION_BUTTON_SIZE, y + 1, ACTION_BORDER_LIGHT);
        Gui.drawRect(x, y, x + 1, y + ACTION_BUTTON_SIZE, ACTION_BORDER_LIGHT);
        Gui.drawRect(x, y + ACTION_BUTTON_SIZE - 1, x + ACTION_BUTTON_SIZE, y + ACTION_BUTTON_SIZE,
                ACTION_BORDER_DARK);
        Gui.drawRect(x + ACTION_BUTTON_SIZE - 1, y, x + ACTION_BUTTON_SIZE, y + ACTION_BUTTON_SIZE,
                ACTION_BORDER_DARK);
    }

    private static void drawPixelX(final int x, final int y, final int color) {
        for (int i = 0; i < 6; i++) {
            Gui.drawRect(x + i, y + i, x + i + 1, y + i + 1, color);
            Gui.drawRect(x + 5 - i, y + i, x + 6 - i, y + i + 1, color);
        }
    }

    private static void drawPixelStar(final int x, final int y, final int color) {
        Gui.drawRect(x + 3, y, x + 4, y + 1, color);
        Gui.drawRect(x + 3, y + 1, x + 4, y + 2, color);
        Gui.drawRect(x + 1, y + 2, x + 6, y + 3, color);
        Gui.drawRect(x + 2, y + 3, x + 5, y + 4, color);
        Gui.drawRect(x + 1, y + 4, x + 6, y + 5, color);
        Gui.drawRect(x + 1, y + 5, x + 3, y + 6, color);
        Gui.drawRect(x + 4, y + 5, x + 6, y + 6, color);
    }

    private static boolean isRowHovered(
            final int x,
            final int width,
            final int rowY,
            final int mouseX,
            final int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
    }

    private static boolean isButtonHovered(
            final int buttonX,
            final int buttonY,
            final double mouseX,
            final double mouseY) {
        return buttonX >= 0
                && mouseX >= buttonX
                && mouseX < buttonX + ACTION_BUTTON_SIZE
                && mouseY >= buttonY
                && mouseY < buttonY + ACTION_BUTTON_SIZE;
    }

    private static ClickTarget getGroupClickTarget(
            final List<RecentSearchHistory.SearchEntry> entries,
            final int x,
            final int width,
            int rowY,
            final double mouseX,
            final double mouseY,
            final boolean showFavorite,
            final boolean showDelete) {
        for (final RecentSearchHistory.SearchEntry entry : entries) {
            final ButtonLayout layout = buttonLayout(x, width, showFavorite, showDelete);
            if (showFavorite && isButtonHovered(layout.favoriteX, rowY + 1, mouseX, mouseY)) {
                return new ClickTarget(ClickTargetType.FAVORITE, entry.getValue());
            }
            if (showDelete && isButtonHovered(layout.deleteX, rowY + 1, mouseX, mouseY)) {
                return new ClickTarget(ClickTargetType.DELETE, entry.getValue());
            }
            if (mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                return new ClickTarget(ClickTargetType.ENTRY, entry.getValue());
            }
            rowY += ROW_HEIGHT;
        }
        return null;
    }

    private static GroupedEntries groupEntries(final List<RecentSearchHistory.SearchEntry> entries) {
        if (!SearchHistoryStore.isFavoritesEnabled()) {
            return new GroupedEntries(Collections.<RecentSearchHistory.SearchEntry>emptyList(), entries);
        }

        final List<RecentSearchHistory.SearchEntry> favorites = new ArrayList<>();
        final List<RecentSearchHistory.SearchEntry> recents = new ArrayList<>();
        for (final RecentSearchHistory.SearchEntry entry : entries) {
            if (entry.isFavorite()) {
                favorites.add(entry);
            } else {
                recents.add(entry);
            }
        }
        return new GroupedEntries(favorites, recents);
    }

    private static int overlayHeight(final GroupedEntries groupedEntries) {
        final int rowCount = groupedEntries.favorites.size() + groupedEntries.recents.size();
        final int separatorHeight = groupedEntries.hasSeparator() ? 4 : 0;
        return rowCount * ROW_HEIGHT + separatorHeight + 2 * PADDING;
    }

    private static ButtonLayout buttonLayout(
            final int x,
            final int width,
            final boolean showFavorite,
            final boolean showDelete) {
        final int right = x + width - PADDING;
        final int deleteX = showDelete ? right - ACTION_BUTTON_SIZE : -1;
        final int favoriteX = showFavorite
                ? (showDelete
                ? deleteX - TEXT_BUTTON_GAP - ACTION_BUTTON_SIZE
                : right - ACTION_BUTTON_SIZE)
                : -1;
        int textReserveWidth = 0;
        if (showFavorite) {
            textReserveWidth += ACTION_BUTTON_SIZE + TEXT_BUTTON_GAP;
        }
        if (showDelete) {
            textReserveWidth += ACTION_BUTTON_SIZE + TEXT_BUTTON_GAP;
        }
        return new ButtonLayout(favoriteX, deleteX, textReserveWidth);
    }

    private static boolean isClearAllButtonVisible(final MEGuiTextField searchField) {
        return SearchHistoryStore.isEnabled()
                && searchField != null
                && searchField.getVisible()
                && !SearchHistoryStore.getVisibleEntries().isEmpty();
    }

    private static boolean isSearchActionHovered(
            final MEGuiTextField searchField,
            final double mouseX,
            final double mouseY) {
        final int x = searchActionX(searchField, null);
        final int y = searchActionY(searchField, null);
        return mouseX >= x && mouseX < x + SEARCH_ACTION_HIT_SIZE
                && mouseY >= y && mouseY < y + SEARCH_ACTION_HIT_SIZE;
    }

    private static int searchActionX(final MEGuiTextField searchField, final FontRenderer font) {
        return fieldX(searchField) + outerFieldWidth(searchField, font)
                - SEARCH_ACTION_HIT_SIZE - SEARCH_ACTION_RIGHT_OFFSET;
    }

    private static int searchActionY(final MEGuiTextField searchField, final FontRenderer font) {
        return fieldY(searchField) + Math.max(0, (outerFieldHeight(searchField) - SEARCH_ACTION_HIT_SIZE) / 2);
    }

    private static int screenX(final MEGuiTextField searchField, final FontRenderer font) {
        return fieldX(searchField) - 1;
    }

    private static int screenY(final MEGuiTextField searchField, final FontRenderer font) {
        return fieldY(searchField) + outerFieldHeight(searchField) + 3;
    }

    private static int width(final MEGuiTextField searchField, final FontRenderer font) {
        return Math.max(40, outerFieldWidth(searchField, font) + 8);
    }

    private static int fieldX(final MEGuiTextField searchField) {
        return searchField.x - PADDING;
    }

    private static int fieldY(final MEGuiTextField searchField) {
        return searchField.y - PADDING;
    }

    private static int outerFieldWidth(final MEGuiTextField searchField, final FontRenderer font) {
        final int fontPadding = font == null ? 4 : font.getCharWidth('_');
        return searchField.width + 2 * PADDING + fontPadding;
    }

    private static int outerFieldHeight(final MEGuiTextField searchField) {
        return searchField.height + 2 * PADDING;
    }

    public static final class ClickTarget {
        private final ClickTargetType type;
        private final String value;

        public ClickTarget(final ClickTargetType type, final String value) {
            this.type = type;
            this.value = value;
        }

        public ClickTargetType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ClickTargetType {
        ENTRY,
        FAVORITE,
        CLEAR_ALL,
        DELETE
    }

    private static final class GroupedEntries {
        private final List<RecentSearchHistory.SearchEntry> favorites;
        private final List<RecentSearchHistory.SearchEntry> recents;

        private GroupedEntries(
                final List<RecentSearchHistory.SearchEntry> favorites,
                final List<RecentSearchHistory.SearchEntry> recents) {
            this.favorites = favorites;
            this.recents = recents;
        }

        private boolean hasSeparator() {
            return !favorites.isEmpty() && !recents.isEmpty();
        }
    }

    private static final class ButtonLayout {
        private final int favoriteX;
        private final int deleteX;
        private final int textReserveWidth;

        private ButtonLayout(final int favoriteX, final int deleteX, final int textReserveWidth) {
            this.favoriteX = favoriteX;
            this.deleteX = deleteX;
            this.textReserveWidth = textReserveWidth;
        }
    }
}
