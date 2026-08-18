package com.shiver.ae2searchoptimization.mixin;

import appeng.client.me.ItemRepo;
import com.shiver.ae2searchoptimization.AE2SearchOptimizationConfig;
import com.shiver.ae2searchoptimization.search.SearchQueryCache;
import com.shiver.ae2searchoptimization.search.TooltipSearchIndex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.regex.Pattern;

@Mixin(value = ItemRepo.class, remap = false)
public abstract class MixinItemRepo {

    @Shadow
    private String searchString;

    @Shadow
    private boolean resort;

    @Unique
    private long ae2searchoptimization$seenTooltipGeneration = -1L;

    @Inject(method = "updateView", at = @At("HEAD"))
    private void ae2searchoptimization$refreshWhenTooltipIndexChanges(final CallbackInfo callbackInfo) {
        if (!AE2SearchOptimizationConfig.isSearchOptimizationEnabled()) {
            return;
        }

        final long generation = TooltipSearchIndex.getGeneration();
        if (TooltipSearchIndex.isEnabled()
                && searchString != null
                && !searchString.isEmpty()
                && ae2searchoptimization$seenTooltipGeneration != generation) {
            resort = true;
        }
        ae2searchoptimization$seenTooltipGeneration = generation;
    }

    @Redirect(
            method = "addIAE",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/regex/Pattern;compile(Ljava/lang/String;I)Ljava/util/regex/Pattern;"
            )
    )
    private Pattern ae2searchoptimization$cachePattern(final String regex, final int flags) {
        return SearchQueryCache.compile(regex, flags);
    }

    @Redirect(
            method = "addIAE",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;split(Ljava/lang/String;)[Ljava/lang/String;"
            )
    )
    private String[] ae2searchoptimization$cacheTerms(final String value, final String expression) {
        return SearchQueryCache.split(value, expression);
    }

    @Redirect(
            method = "addIAE",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/util/Platform;getTooltip(Ljava/lang/Object;)Ljava/util/List;"
            )
    )
    private List<String> ae2searchoptimization$lookupTooltip(final Object object) {
        return TooltipSearchIndex.getOrQueue(object);
    }
}
