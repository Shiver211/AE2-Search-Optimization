/*
 * Ported from AE2 Recent Search (MIT License, Copyright (c) 2026 zh5112).
 */
package com.shiver.ae2searchoptimization.recentsearch;

import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

/** AE2-styled button used to open the in-terminal recent-search settings. */
public final class RecentSearchSettingsButton extends GuiImgButton {

    private static final ResourceLocation STATES_TEXTURE =
            new ResourceLocation("appliedenergistics2", "textures/guis/states.png");
    private static final ResourceLocation BUTTON_ICON =
            new ResourceLocation("ae2searchoptimization", "textures/gui/settings_button.png");

    public RecentSearchSettingsButton(final int x, final int y) {
        super(x, y, Settings.ACTIONS, ActionItems.WRENCH);
    }

    @Override
    public void drawButton(final Minecraft mc, final int mouseX, final int mouseY, final float partialTicks) {
        if (!this.visible) {
            return;
        }

        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        // Draw standard AE2 16x16 button background plate
        mc.renderEngine.bindTexture(STATES_TEXTURE);
        drawTexturedModalRect(this.x, this.y, 240, 240, 16, 16);

        // Draw custom recent search settings icon
        mc.renderEngine.bindTexture(BUTTON_ICON);
        drawModalRectWithCustomSizedTexture(this.x, this.y, 0, 0, 16, 16, 16, 16);

        this.mouseDragged(mc, mouseX, mouseY);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public String getMessage() {
        return I18n.translateToLocal("ae2searchoptimization.recent_search.settings.title");
    }
}
