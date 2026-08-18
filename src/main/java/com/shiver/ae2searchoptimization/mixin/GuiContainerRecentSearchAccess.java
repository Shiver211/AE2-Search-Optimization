/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.mixin;

import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Access to the legacy container position used by AE2's side toolbar. */
@Mixin(GuiContainer.class)
public interface GuiContainerRecentSearchAccess {

    @Accessor("guiLeft")
    int ae2searchoptimization$getGuiLeft();

    @Accessor("guiTop")
    int ae2searchoptimization$getGuiTop();
}
