/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.mixin;

import com.shiver.ae2searchoptimization.recentsearch.RecentSearchOverlay;
import com.shiver.ae2searchoptimization.recentsearch.RecentSearchScreenAccess;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents container item tooltips from covering the recent-search panel. */
@Mixin(GuiContainer.class)
public abstract class MixinGuiContainerRecentSearch {

    @Inject(method = "renderHoveredToolTip", at = @At("HEAD"), cancellable = true, remap = true)
    private void ae2searchoptimization$hideRecentSearchTooltip(
            final int mouseX,
            final int mouseY,
            final CallbackInfo callbackInfo) {
        if (!((Object) this instanceof RecentSearchScreenAccess)) {
            return;
        }

        final RecentSearchScreenAccess access = (RecentSearchScreenAccess) (Object) this;
        if (access.isRecentSearchSettingsOpen()
                || RecentSearchOverlay.isMouseOverSearchOrOverlay(
                access.getRecentSearchField(), mouseX, mouseY)) {
            callbackInfo.cancel();
        }
    }
}
