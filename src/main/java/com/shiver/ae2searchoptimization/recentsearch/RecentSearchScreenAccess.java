/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.recentsearch;

import appeng.client.gui.widgets.MEGuiTextField;

/** Access shared by the GUI mixins without exposing AE2 implementation fields. */
public interface RecentSearchScreenAccess {

    MEGuiTextField getRecentSearchField();

    boolean isRecentSearchSettingsOpen();

    void setRecentSearchSettingsOpen(boolean open);

    int getRecentSearchSettingsButtonX();

    int getRecentSearchSettingsButtonY();
}
