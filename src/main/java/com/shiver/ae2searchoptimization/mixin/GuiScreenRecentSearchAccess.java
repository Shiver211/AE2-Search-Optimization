/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Access to the 1.12.2 screen button list. */
@Mixin(GuiScreen.class)
public interface GuiScreenRecentSearchAccess {

    @Accessor("buttonList")
    List<GuiButton> ae2searchoptimization$getButtonList();
}
