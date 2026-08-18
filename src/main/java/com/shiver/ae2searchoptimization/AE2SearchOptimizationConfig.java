package com.shiver.ae2searchoptimization;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/** Client-side performance settings for AE2 search optimizations. */
public final class AE2SearchOptimizationConfig {

    private static final String PERFORMANCE_CATEGORY = "performance";
    private static final String SEARCH_OPTIMIZATION_ENABLED = "searchOptimizationEnabled";
    private static final String TOOLTIP_INDEX_BUDGET_MILLIS = "tooltipIndexBudgetMillis";
    private static final String RECENT_SEARCH_CATEGORY = "recentSearch";
    private static final String MAX_VISIBLE_RECENT_ENTRIES = "maxVisibleEntries";

    private static boolean searchOptimizationEnabled = true;
    private static int tooltipIndexBudgetMillis = 10;
    private static int maxVisibleRecentEntries = 10;

    private AE2SearchOptimizationConfig() {
    }

    public static void load(final File configFile) {
        final Configuration configuration = new Configuration(configFile);
        configuration.load();

        searchOptimizationEnabled = configuration.getBoolean(
                SEARCH_OPTIMIZATION_ENABLED,
                PERFORMANCE_CATEGORY,
                true,
                "Enable AE2 search query caching and tooltip indexing optimizations.");

        tooltipIndexBudgetMillis = configuration.getInt(
                TOOLTIP_INDEX_BUDGET_MILLIS,
                PERFORMANCE_CATEGORY,
                10,
                1,
                1000,
                "Maximum client-thread time spent generating tooltip entries per tick, in milliseconds.");

        maxVisibleRecentEntries = configuration.getInt(
                MAX_VISIBLE_RECENT_ENTRIES,
                RECENT_SEARCH_CATEGORY,
                10,
                1,
                Integer.MAX_VALUE,
                "Maximum number of recent search entries shown in an AE2 terminal.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static boolean isSearchOptimizationEnabled() {
        return searchOptimizationEnabled;
    }

    public static long getTooltipIndexBudgetNanos() {
        return tooltipIndexBudgetMillis * 1_000_000L;
    }

    public static int getMaxVisibleRecentEntries() {
        return maxVisibleRecentEntries;
    }
}
