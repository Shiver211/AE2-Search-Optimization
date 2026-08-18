/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.mixin;

import appeng.client.gui.AEBaseGui;
import com.shiver.ae2searchoptimization.recentsearch.RecentSearchOverlay;
import com.shiver.ae2searchoptimization.recentsearch.RecentSearchScreenAccess;
import com.shiver.ae2searchoptimization.recentsearch.RecentSearchSettingsOverlay;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the recent-search controls above AE2's normal tooltip layer. */
@Mixin(value = AEBaseGui.class, remap = false)
public abstract class MixinAEBaseGuiRecentSearch {

    @Inject(
            method = "drawScreen",
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/gui/AEBaseGui;renderHoveredToolTip(II)V",
                    shift = At.Shift.BEFORE))
    private void ae2searchoptimization$renderRecentSearch(
            final int mouseX,
            final int mouseY,
            final float partialTicks,
            final CallbackInfo callbackInfo) {
        if (!((Object) this instanceof RecentSearchScreenAccess)) {
            return;
        }

        final RecentSearchScreenAccess access = (RecentSearchScreenAccess) (Object) this;
        final GuiScreen screen = (GuiScreen) (Object) this;
        if (access.isRecentSearchSettingsOpen()) {
            RecentSearchSettingsOverlay.render(screen, access, mouseX, mouseY);
        } else {
            RecentSearchOverlay.render(
                    Minecraft.getMinecraft().fontRenderer,
                    access.getRecentSearchField(),
                    mouseX,
                    mouseY);
        }
    }
}
