package com.shiver.ae2searchoptimization;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/** Client-side performance settings for the tooltip index. */
public final class AE2SearchOptimizationConfig {

    private static final String PERFORMANCE_CATEGORY = "performance";
    private static final String TOOLTIP_INDEX_BUDGET_MILLIS = "tooltipIndexBudgetMillis";

    private static int tooltipIndexBudgetMillis = 10;

    private AE2SearchOptimizationConfig() {
    }

    public static void load(final File configFile) {
        final Configuration configuration = new Configuration(configFile);
        configuration.load();

        tooltipIndexBudgetMillis = configuration.getInt(
                TOOLTIP_INDEX_BUDGET_MILLIS,
                PERFORMANCE_CATEGORY,
                10,
                1,
                1000,
                "Maximum client-thread time spent generating tooltip entries per tick, in milliseconds.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static long getTooltipIndexBudgetNanos() {
        return tooltipIndexBudgetMillis * 1_000_000L;
    }
}
