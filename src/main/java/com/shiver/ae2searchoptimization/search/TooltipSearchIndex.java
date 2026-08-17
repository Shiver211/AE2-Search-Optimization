package com.shiver.ae2searchoptimization.search;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;
import appeng.util.Platform;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Main-thread tooltip index shared by every ItemRepo in the current client
 * world. Missing entries are queued and generated in small time slices so a
 * search never performs a full tooltip scan in one GUI update.
 */
public final class TooltipSearchIndex {

    private static final Map<IAEItemStack, List<String>> CACHE = new HashMap<>();
    private static final LinkedHashMap<IAEItemStack, IAEItemStack> PENDING = new LinkedHashMap<>();

    private static Object contextWorld;
    private static String contextLanguage;
    private static boolean contextAdvancedTooltips;
    private static boolean contextInitialized;
    private static long generation;

    private TooltipSearchIndex() {
    }

    public static boolean isEnabled() {
        return AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_TOOLTIPS) != YesNo.NO;
    }

    public static long getGeneration() {
        ensureContext();
        return generation;
    }

    /**
     * Called from ItemRepo's tooltip lookup. An uncached tooltip is deliberately
     * represented by an empty list until the queued main-thread work completes.
     */
    public static List<String> getOrQueue(final Object object) {
        ensureContext();

        if (!(object instanceof IAEItemStack)) {
            return Collections.emptyList();
        }

        final IAEItemStack stack = (IAEItemStack) object;
        final List<String> cached = CACHE.get(stack);
        if (cached != null) {
            return cached;
        }

        if (!PENDING.containsKey(stack)) {
            PENDING.put(stack, stack);
        }
        return Collections.emptyList();
    }

    /**
     * Pre-warms the index when an item batch arrives for a terminal. The same
     * queue is also populated lazily by getOrQueue for newly seen searches.
     */
    public static void queueAll(final Iterable<IAEItemStack> stacks) {
        ensureContext();
        if (!isEnabled()) {
            return;
        }

        for (final IAEItemStack stack : stacks) {
            if (stack != null && !CACHE.containsKey(stack) && !PENDING.containsKey(stack)) {
                PENDING.put(stack, stack);
            }
        }
    }

    /**
     * Processes queued tooltip calls for at most the supplied number of
     * nanoseconds. This method is only called from the client GUI thread.
     */
    public static void process(final long budgetNanos) {
        ensureContext();
        if (!isEnabled() || PENDING.isEmpty()) {
            return;
        }

        final long deadline = System.nanoTime() + budgetNanos;
        boolean changed = false;
        int processed = 0;
        final Iterator<Map.Entry<IAEItemStack, IAEItemStack>> iterator = PENDING.entrySet().iterator();

        // Always attempt one entry, even if the first callback itself exceeds
        // the budget. Otherwise a single slow item could permanently starve.
        while (iterator.hasNext() && (processed == 0 || System.nanoTime() < deadline)) {
            final IAEItemStack stack = iterator.next().getValue();
            iterator.remove();
            processed++;

            if (!CACHE.containsKey(stack)) {
                final List<String> tooltip = Platform.getTooltip(stack);
                CACHE.put(stack, Collections.unmodifiableList(new ArrayList<>(tooltip)));
                changed = true;
            }
        }

        if (changed) {
            generation++;
        }
    }

    private static void ensureContext() {
        final Minecraft minecraft = Minecraft.getMinecraft();
        final Object world = minecraft.world;
        final String language = minecraft.getLanguageManager().getCurrentLanguage().getLanguageCode();
        final boolean advancedTooltips = minecraft.gameSettings.advancedItemTooltips;

        if (!contextInitialized
                || contextWorld != world
                || !Objects.equals(contextLanguage, language)
                || contextAdvancedTooltips != advancedTooltips) {
            CACHE.clear();
            PENDING.clear();
            contextWorld = world;
            contextLanguage = language;
            contextAdvancedTooltips = advancedTooltips;
            contextInitialized = true;
            generation++;
        }
    }
}
