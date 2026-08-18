/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.recentsearch;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import net.minecraft.util.text.translation.I18n;

/** AE2-styled button used to open the in-terminal recent-search settings. */
public final class RecentSearchSettingsButton extends GuiImgButton {

    public RecentSearchSettingsButton(final int x, final int y) {
        super(x, y, Settings.ACTIONS, ActionItems.WRENCH);
    }

    @Override
    public String getMessage() {
        return I18n.translateToLocal("ae2searchoptimization.recent_search.settings.title");
    }
}
