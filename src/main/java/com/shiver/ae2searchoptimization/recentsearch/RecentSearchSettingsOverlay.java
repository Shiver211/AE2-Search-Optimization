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

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 240;
    private static final int ROW_X = 20;
    private static final int ROW_WIDTH = 200;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 4;
    private static final int FIRST_ROW_OFFSET = 34;
    private static final int BACK_ROW = 7;

    private static final int PANEL_BACKGROUND = 0xFF2C2C36;
    private static final int PANEL_INNER_BACKGROUND = 0xFF3C3C4A;
    private static final int BUTTON_BACKGROUND = 0xFF5B5B6B;
    private static final int BUTTON_HOVER = 0xFF77778A;
    private static final int BUTTON_BORDER_LIGHT = 0xFF9C9CAD;
    private static final int BUTTON_BORDER_DARK = 0xFF24242D;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int TEXT_COLOR = 0xFFE8E8EE;

    private RecentSearchSettingsOverlay() {
    }

    public static void render(
            final GuiScreen screen,
            final RecentSearchScreenAccess access,
            final int mouseX,
            final int mouseY) {
        final FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        final int panelX = (screen.width - PANEL_WIDTH) / 2;
        final int panelY = (screen.height - PANEL_HEIGHT) / 2;

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Gui.drawRect(panelX - 4, panelY - 4, panelX + PANEL_WIDTH + 4, panelY + PANEL_HEIGHT + 4,
                0x99000000);
        Gui.drawRect(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, PANEL_BACKGROUND);
        Gui.drawRect(panelX + 2, panelY + 2, panelX + PANEL_WIDTH - 2, panelY + PANEL_HEIGHT - 2,
                PANEL_INNER_BACKGROUND);
        font.drawString(I18n.translateToLocal("ae2searchoptimization.recent_search.settings.title"),
                panelX + 10, panelY + 10, TITLE_COLOR);

        drawButton(font, panelX, panelY, 0,
                I18n.translateToLocal(SearchHistoryStore.isEnabled()
                        ? "ae2searchoptimization.recent_search.button.enabled_on"
                        : "ae2searchoptimization.recent_search.button.enabled_off"),
                mouseX, mouseY);
        drawButton(font, panelX, panelY, 1,
                I18n.translateToLocal("ae2searchoptimization.recent_search.button.clear"),
                mouseX, mouseY);
        drawButton(font, panelX, panelY, 2,
                toggleText(SearchHistoryStore.isDeleteButtonsEnabled(), "delete_buttons"), mouseX, mouseY);
        drawButton(font, panelX, panelY, 3,
                toggleText(SearchHistoryStore.isFavoritesEnabled(), "favorites"), mouseX, mouseY);
        drawButton(font, panelX, panelY, 4,
                toggleText(SearchHistoryStore.isKeyboardNavigationEnabled(), "keyboard_navigation"), mouseX, mouseY);
        drawButton(font, panelX, panelY, 5,
                toggleText(SearchHistoryStore.isApplyOnClick(), "apply"), mouseX, mouseY);
        drawButton(font, panelX, panelY, 6,
                toggleText(SearchHistoryStore.isSyncExternalSearch(), "sync_external"), mouseX, mouseY);
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

        final int panelX = (screen.width - PANEL_WIDTH) / 2;
        final int panelY = (screen.height - PANEL_HEIGHT) / 2;
        if (!contains(panelX - 4, panelY - 4, PANEL_WIDTH + 8, PANEL_HEIGHT + 8, mouseX, mouseY)) {
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
                    SearchHistoryStore.clear();
                    break;
                case 2:
                    SearchHistoryStore.setDeleteButtonsEnabled(!SearchHistoryStore.isDeleteButtonsEnabled());
                    break;
                case 3:
                    SearchHistoryStore.setFavoritesEnabled(!SearchHistoryStore.isFavoritesEnabled());
                    break;
                case 4:
                    SearchHistoryStore.setKeyboardNavigationEnabled(!SearchHistoryStore.isKeyboardNavigationEnabled());
                    break;
                case 5:
                    SearchHistoryStore.setApplyOnClick(!SearchHistoryStore.isApplyOnClick());
                    break;
                case 6:
                    SearchHistoryStore.setSyncExternalSearch(!SearchHistoryStore.isSyncExternalSearch());
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
        font.drawString(text, x + 6, y + 6, TEXT_COLOR);
    }

    private static String toggleText(final boolean enabled, final String key) {
        return I18n.translateToLocal("ae2searchoptimization.recent_search.button." + key
                + (enabled ? "_on" : "_off"));
    }

    private static int rowY(final int panelY, final int row) {
        if (row == BACK_ROW) {
            return panelY + PANEL_HEIGHT - ROW_HEIGHT - 10;
        }
        return panelY + FIRST_ROW_OFFSET + row * (ROW_HEIGHT + ROW_GAP);
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
