/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.recentsearch;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.translation.I18n;

/** In-place settings page so the AE2 container remains open. */
public final class RecentSearchSettingsOverlay {

    private static final int PANEL_WIDTH = 140;
    private static final int PANEL_HEIGHT = 114;
    private static final int ROW_X = 10;
    private static final int ROW_WIDTH = 120;
    private static final int ROW_HEIGHT = 14;
    private static final int ROW_GAP = 3;
    private static final int FIRST_ROW_OFFSET = 22;
    private static final int BACK_ROW = 4;

    private static final int PANEL_BACKGROUND = 0xFFE1E4F0;
    private static final int PANEL_INNER_BACKGROUND = 0xFFD2D6E6;
    private static final int HIGHLIGHT_BORDER_COLOR = 0xFFFFFFFF;
    private static final int SHADOW_BORDER_COLOR = 0xFF6F7488;
    private static final int GROUP_SEPARATOR_COLOR = 0xFF9298AC;

    private static final int BUTTON_BACKGROUND = 0xFFDCE1EE;
    private static final int BUTTON_HOVER = 0xFFF0F3F9;
    private static final int BUTTON_BORDER_LIGHT = 0xFFFFFFFF;
    private static final int BUTTON_BORDER_DARK = 0xFF7A7F93;
    private static final int TITLE_COLOR = 0xFF303040;
    private static final int TEXT_COLOR = 0xFF303040;
    private static final int TEXT_HOVER_COLOR = 0xFF101020;

    private RecentSearchSettingsOverlay() {
    }

    public static void render(
            final GuiScreen screen,
            final RecentSearchScreenAccess access,
            final int mouseX,
            final int mouseY) {
        final FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        final int panelX = panelX(screen, access);
        final int panelY = panelY(screen, access);

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Gui.drawRect(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, PANEL_BACKGROUND);
        Gui.drawRect(panelX + 1, panelY + 1, panelX + PANEL_WIDTH - 1, panelY + PANEL_HEIGHT - 1,
                PANEL_INNER_BACKGROUND);
        Gui.drawRect(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, HIGHLIGHT_BORDER_COLOR);
        Gui.drawRect(panelX, panelY, panelX + 1, panelY + PANEL_HEIGHT, HIGHLIGHT_BORDER_COLOR);
        Gui.drawRect(panelX, panelY + PANEL_HEIGHT - 1, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, SHADOW_BORDER_COLOR);
        Gui.drawRect(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, SHADOW_BORDER_COLOR);

        final String title = I18n.translateToLocal("ae2searchoptimization.recent_search.settings.title");
        final int titleX = panelX + (PANEL_WIDTH - font.getStringWidth(title)) / 2;
        font.drawString(title, titleX, panelY + 6, TITLE_COLOR);
        Gui.drawRect(panelX + 8, panelY + 17, panelX + PANEL_WIDTH - 8, panelY + 18, GROUP_SEPARATOR_COLOR);

        drawButton(font, panelX, panelY, 0,
                I18n.translateToLocal(SearchHistoryStore.isEnabled()
                        ? "ae2searchoptimization.recent_search.button.enabled_on"
                        : "ae2searchoptimization.recent_search.button.enabled_off"),
                mouseX, mouseY);
        drawButton(font, panelX, panelY, 1,
                toggleText(SearchHistoryStore.isFavoritesEnabled(), "favorites"), mouseX, mouseY);
        drawButton(font, panelX, panelY, 2,
                toggleText(SearchHistoryStore.isKeyboardNavigationEnabled(), "keyboard_navigation"), mouseX, mouseY);
        drawButton(font, panelX, panelY, 3,
                toggleText(SearchHistoryStore.isApplyOnClick(), "apply"), mouseX, mouseY);
        drawButton(font, panelX, panelY, BACK_ROW,
                I18n.translateToLocal("gui.back"), mouseX, mouseY);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static boolean handleClick(
            final GuiScreen screen,
            final RecentSearchScreenAccess access,
            final int mouseX,
            final int mouseY,
            final int mouseButton) {
        if (mouseButton != 0) {
            return true;
        }

        final int panelX = panelX(screen, access);
        final int panelY = panelY(screen, access);
        if (!contains(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, mouseX, mouseY)) {
            access.setRecentSearchSettingsOpen(false);
            return true;
        }

        for (int row = 0; row <= BACK_ROW; row++) {
            if (!contains(panelX + ROW_X, rowY(panelY, row), ROW_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
                continue;
            }

            switch (row) {
                case 0:
                    SearchHistoryStore.setEnabled(!SearchHistoryStore.isEnabled());
                    break;
                case 1:
                    SearchHistoryStore.setFavoritesEnabled(!SearchHistoryStore.isFavoritesEnabled());
                    break;
                case 2:
                    SearchHistoryStore.setKeyboardNavigationEnabled(!SearchHistoryStore.isKeyboardNavigationEnabled());
                    break;
                case 3:
                    SearchHistoryStore.setApplyOnClick(!SearchHistoryStore.isApplyOnClick());
                    break;
                case BACK_ROW:
                    access.setRecentSearchSettingsOpen(false);
                    break;
                default:
                    break;
            }
            return true;
        }
        return true;
    }

    private static void drawButton(
            final FontRenderer font,
            final int panelX,
            final int panelY,
            final int row,
            final String text,
            final int mouseX,
            final int mouseY) {
        final int x = panelX + ROW_X;
        final int y = rowY(panelY, row);
        final boolean hovered = contains(x, y, ROW_WIDTH, ROW_HEIGHT, mouseX, mouseY);
        Gui.drawRect(x, y, x + ROW_WIDTH, y + ROW_HEIGHT, hovered ? BUTTON_HOVER : BUTTON_BACKGROUND);
        Gui.drawRect(x, y, x + ROW_WIDTH, y + 1, BUTTON_BORDER_LIGHT);
        Gui.drawRect(x, y, x + 1, y + ROW_HEIGHT, BUTTON_BORDER_LIGHT);
        Gui.drawRect(x, y + ROW_HEIGHT - 1, x + ROW_WIDTH, y + ROW_HEIGHT, BUTTON_BORDER_DARK);
        Gui.drawRect(x + ROW_WIDTH - 1, y, x + ROW_WIDTH, y + ROW_HEIGHT, BUTTON_BORDER_DARK);

        final int textX = x + (ROW_WIDTH - font.getStringWidth(text)) / 2;
        final int textY = y + (ROW_HEIGHT - 8) / 2;
        font.drawString(text, textX, textY, hovered ? TEXT_HOVER_COLOR : TEXT_COLOR);
    }

    private static String toggleText(final boolean enabled, final String key) {
        return I18n.translateToLocal("ae2searchoptimization.recent_search.button." + key
                + (enabled ? "_on" : "_off"));
    }

    private static int rowY(final int panelY, final int row) {
        if (row == BACK_ROW) {
            return panelY + PANEL_HEIGHT - ROW_HEIGHT - 7;
        }
        return panelY + FIRST_ROW_OFFSET + row * (ROW_HEIGHT + ROW_GAP);
    }

    private static int panelX(final GuiScreen screen, final RecentSearchScreenAccess access) {
        final int buttonX = access != null ? access.getRecentSearchSettingsButtonX() : -1;
        if (buttonX >= 0) {
            final int x = buttonX + 16 + 3;
            return Math.max(2, Math.min(screen.width - PANEL_WIDTH - 2, x));
        }
        return (screen.width - PANEL_WIDTH) / 2;
    }

    private static int panelY(final GuiScreen screen, final RecentSearchScreenAccess access) {
        final int buttonY = access != null ? access.getRecentSearchSettingsButtonY() : -1;
        if (buttonY >= 0) {
            final int centeredY = buttonY + 8 - PANEL_HEIGHT / 2;
            return Math.max(2, Math.min(screen.height - PANEL_HEIGHT - 2, centeredY));
        }
        return (screen.height - PANEL_HEIGHT) / 2;
    }

    private static boolean contains(
            final int x,
            final int y,
            final int width,
            final int height,
            final int mouseX,
            final int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
