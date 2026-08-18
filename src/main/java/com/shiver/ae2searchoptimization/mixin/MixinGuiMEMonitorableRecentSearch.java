/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.mixin;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.client.me.ItemRepo;
import appeng.core.AEConfig;
import appeng.integration.Integrations;
import appeng.util.Platform;
import com.shiver.ae2searchoptimization.recentsearch.RecentSearchKeyboardNavigation;
import com.shiver.ae2searchoptimization.recentsearch.RecentSearchOverlay;
import com.shiver.ae2searchoptimization.recentsearch.RecentSearchScreenAccess;
import com.shiver.ae2searchoptimization.recentsearch.RecentSearchSettingsButton;
import com.shiver.ae2searchoptimization.recentsearch.RecentSearchSettingsOverlay;
import com.shiver.ae2searchoptimization.recentsearch.SearchHistoryStore;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.GuiButton;

import java.util.List;

/** Adds recent-search interaction to every item-based AE2 terminal. */
@Mixin(value = GuiMEMonitorable.class, remap = false)
public abstract class MixinGuiMEMonitorableRecentSearch implements RecentSearchScreenAccess {

    @Shadow
    private MEGuiTextField searchField;

    @Shadow
    protected ItemRepo repo;

    @Unique
    private boolean ae2searchoptimization$settingsOpen;

    @Unique
    private boolean ae2searchoptimization$skipSearchRecord;

    @Unique
    private RecentSearchSettingsButton ae2searchoptimization$settingsButton;

    @Inject(method = "initGui", at = @At("TAIL"))
    private void ae2searchoptimization$addSettingsButton(final CallbackInfo callbackInfo) {
        if (ae2searchoptimization$settingsButton == null) {
            ae2searchoptimization$settingsButton = new RecentSearchSettingsButton(
                    ae2searchoptimization$guiLeft() - 18,
                    ae2searchoptimization$guiTop() + 8);
        }

        final List<GuiButton> buttons = ae2searchoptimization$buttonList();
        if (!buttons.contains(ae2searchoptimization$settingsButton)) {
            ae2searchoptimization$settingsButton.x = ae2searchoptimization$guiLeft() - 18;
            ae2searchoptimization$settingsButton.y = ae2searchoptimization$settingsButtonY();
            buttons.add(ae2searchoptimization$settingsButton);
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    private void ae2searchoptimization$openSettings(final GuiButton button, final CallbackInfo callbackInfo) {
        if (button != ae2searchoptimization$settingsButton) {
            return;
        }

        ae2searchoptimization$settingsOpen = true;
        RecentSearchKeyboardNavigation.clear(searchField);
        if (searchField != null) {
            searchField.setFocused(false);
        }
        callbackInfo.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void ae2searchoptimization$handleMouse(
            final int mouseX,
            final int mouseY,
            final int mouseButton,
            final CallbackInfo callbackInfo) {
        if (ae2searchoptimization$settingsOpen) {
            RecentSearchSettingsOverlay.handleClick(
                    (net.minecraft.client.gui.GuiScreen) (Object) this,
                    this,
                    mouseX,
                    mouseY,
                    mouseButton);
            callbackInfo.cancel();
            return;
        }

        final RecentSearchOverlay.ClickTarget target =
                RecentSearchOverlay.getClickedTarget(searchField, mouseX, mouseY);
        if (target != null) {
            if (mouseButton == 0) {
                ae2searchoptimization$handleTarget(target);
            }
            callbackInfo.cancel();
            return;
        }

        if (RecentSearchOverlay.isMouseOver(searchField, mouseX, mouseY)) {
            callbackInfo.cancel();
            return;
        }

        if (searchField != null && searchField.isFocused()) {
            if (searchField.isMouseIn(mouseX, mouseY)) {
                RecentSearchKeyboardNavigation.clear(searchField);
            } else {
                RecentSearchKeyboardNavigation.clear(searchField);
                ae2searchoptimization$recordCurrentSearch();
            }
        }
    }

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    private void ae2searchoptimization$handleKeyboard(
            final char character,
            final int keyCode,
            final CallbackInfo callbackInfo) {
        if (ae2searchoptimization$settingsOpen) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                ae2searchoptimization$settingsOpen = false;
            }
            callbackInfo.cancel();
            return;
        }

        if (searchField == null) {
            return;
        }

        if (!searchField.isFocused()) {
            final int direction = keyCode == Keyboard.KEY_DOWN ? 1 : keyCode == Keyboard.KEY_UP ? -1 : 0;
            if (direction != 0
                    && RecentSearchKeyboardNavigation.activateAndMoveSelection(searchField, direction)) {
                callbackInfo.cancel();
            }
            return;
        }

        if (keyCode == Keyboard.KEY_DOWN
                && RecentSearchKeyboardNavigation.moveSelection(searchField, 1)) {
            callbackInfo.cancel();
            return;
        }

        if (keyCode == Keyboard.KEY_UP
                && RecentSearchKeyboardNavigation.moveSelection(searchField, -1)) {
            callbackInfo.cancel();
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            final RecentSearchOverlay.ClickTarget target =
                    RecentSearchKeyboardNavigation.selectedTarget(searchField);
            if (target != null) {
                ae2searchoptimization$skipSearchRecord = false;
                ae2searchoptimization$handleTarget(target);
                callbackInfo.cancel();
                return;
            }

            ae2searchoptimization$skipSearchRecord = false;
            ae2searchoptimization$recordCurrentSearch();
            return;
        }

        if (keyCode != Keyboard.KEY_ESCAPE) {
            ae2searchoptimization$skipSearchRecord = false;
        }
        RecentSearchKeyboardNavigation.clear(searchField);
    }

    @Inject(method = "onGuiClosed", at = @At("HEAD"))
    private void ae2searchoptimization$recordOnClose(final CallbackInfo callbackInfo) {
        RecentSearchKeyboardNavigation.clear(searchField);
        ae2searchoptimization$recordCurrentSearch();
        ae2searchoptimization$settingsOpen = false;
    }

    @Override
    public MEGuiTextField getRecentSearchField() {
        return searchField;
    }

    @Override
    public boolean isRecentSearchSettingsOpen() {
        return ae2searchoptimization$settingsOpen;
    }

    @Override
    public void setRecentSearchSettingsOpen(final boolean open) {
        ae2searchoptimization$settingsOpen = open;
        if (!open) {
            RecentSearchKeyboardNavigation.clear(searchField);
        }
    }

    @Override
    public int getRecentSearchSettingsButtonX() {
        return ae2searchoptimization$settingsButton != null
                ? ae2searchoptimization$settingsButton.x
                : ae2searchoptimization$guiLeft() - 18;
    }

    @Override
    public int getRecentSearchSettingsButtonY() {
        return ae2searchoptimization$settingsButton != null
                ? ae2searchoptimization$settingsButton.y
                : ae2searchoptimization$settingsButtonY();
    }

    @Unique
    private int ae2searchoptimization$settingsButtonY() {
        final int left = ae2searchoptimization$guiLeft();
        int y = ae2searchoptimization$guiTop() + 8;
        for (final GuiButton button : ae2searchoptimization$buttonList()) {
            if (button == ae2searchoptimization$settingsButton || button.x >= left) {
                continue;
            }
            y = Math.max(y, button.y + 20);
        }
        return y;
    }

    @Unique
    private int ae2searchoptimization$guiLeft() {
        return ((GuiContainerRecentSearchAccess) (Object) this).ae2searchoptimization$getGuiLeft();
    }

    @Unique
    private int ae2searchoptimization$guiTop() {
        return ((GuiContainerRecentSearchAccess) (Object) this).ae2searchoptimization$getGuiTop();
    }

    @Unique
    private List<GuiButton> ae2searchoptimization$buttonList() {
        return ((GuiScreenRecentSearchAccess) (Object) this).ae2searchoptimization$getButtonList();
    }

    @Unique
    private void ae2searchoptimization$recordCurrentSearch() {
        if (!ae2searchoptimization$skipSearchRecord && searchField != null) {
            SearchHistoryStore.record(searchField.getText());
        }
    }

    @Unique
    private void ae2searchoptimization$handleTarget(final RecentSearchOverlay.ClickTarget target) {
        if (target.getType() == RecentSearchOverlay.ClickTargetType.CLEAR_ALL) {
            SearchHistoryStore.clear();
            ae2searchoptimization$skipSearchRecord = true;
            RecentSearchKeyboardNavigation.clear(searchField);
            if (searchField != null) {
                searchField.setFocused(true);
            }
            return;
        }

        if (target.getType() == RecentSearchOverlay.ClickTargetType.DELETE) {
            SearchHistoryStore.remove(target.getValue());
            RecentSearchKeyboardNavigation.clear(searchField);
            return;
        }

        if (target.getType() == RecentSearchOverlay.ClickTargetType.FAVORITE) {
            SearchHistoryStore.toggleFavoriteForSearch(target.getValue());
            RecentSearchKeyboardNavigation.clear(searchField);
            if (searchField != null) {
                searchField.setFocused(true);
            }
            return;
        }

        SearchHistoryStore.record(target.getValue());
        RecentSearchKeyboardNavigation.clear(searchField);
        if (searchField == null) {
            return;
        }

        searchField.setText(target.getValue());
        if (SearchHistoryStore.isApplyOnClick()) {
            repo.setSearchString(target.getValue());
            ae2searchoptimization$syncExternalSearch(target.getValue());
            searchField.setFocused(false);
        } else {
            searchField.setFocused(true);
        }
    }

    @Unique
    private void ae2searchoptimization$syncExternalSearch(final String value) {
        if (!SearchHistoryStore.isSyncExternalSearch()
                || !Platform.isModLoaded("jei")
                || !Integrations.jei().isEnabled()) {
            return;
        }

        final Enum searchMode = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);
        if (searchMode == SearchBoxMode.JEI_AUTOSEARCH
                || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH
                || searchMode == SearchBoxMode.JEI_AUTOSEARCH_KEEP
                || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH_KEEP) {
            Integrations.jei().setSearchText(value);
        }
    }
}
