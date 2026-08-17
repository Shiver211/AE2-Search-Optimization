package com.shiver.ae2searchoptimization.mixin;

import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.api.storage.data.IAEItemStack;
import com.shiver.ae2searchoptimization.AE2SearchOptimizationConfig;
import com.shiver.ae2searchoptimization.search.TooltipSearchIndex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiMEMonitorable.class, remap = false)
public abstract class MixinGuiMEMonitorable {

    @Inject(method = "postUpdate", at = @At("TAIL"))
    private void ae2searchoptimization$queueTooltipIndex(final List<IAEItemStack> stacks, final CallbackInfo callbackInfo) {
        TooltipSearchIndex.queueAll(stacks);
    }

    /**
     * updateScreen is named func_73876_c in the obfuscated client.  This
     * injection must be remapped; otherwise the handler is present in the
     * transformed class but is never called outside the development runtime.
     */
    @Inject(method = "updateScreen", at = @At("HEAD"), remap = true)
    private void ae2searchoptimization$processTooltipIndex(final CallbackInfo callbackInfo) {
        TooltipSearchIndex.process(AE2SearchOptimizationConfig.getTooltipIndexBudgetNanos());
    }
}
