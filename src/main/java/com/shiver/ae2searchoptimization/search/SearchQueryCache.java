package com.shiver.ae2searchoptimization.search;

import com.shiver.ae2searchoptimization.AE2SearchOptimizationConfig;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Caches the two query operations that AE2 performs once for every item.
 * ItemRepo still owns the matching semantics; this class only removes the
 * repeated compilation and splitting work.
 */
public final class SearchQueryCache {

    private static String lastPatternText;
    private static int lastPatternFlags;
    private static Pattern lastPattern;

    private static String lastSplitText;
    private static String lastSplitExpression;
    private static String[] lastSplit;

    private SearchQueryCache() {
    }

    public static Pattern compile(final String regex, final int flags) {
        final boolean enabled = AE2SearchOptimizationConfig.isSearchOptimizationEnabled();
        if (enabled && lastPattern != null && lastPatternFlags == flags && regex.equals(lastPatternText)) {
            return lastPattern;
        }

        Pattern compiled;
        try {
            compiled = Pattern.compile(regex, flags);
        } catch (final PatternSyntaxException ignored) {
            // This is the same fallback used by AE2's original search code.
            compiled = Pattern.compile(Pattern.quote(regex), flags);
        }

        if (enabled) {
            lastPatternText = regex;
            lastPatternFlags = flags;
            lastPattern = compiled;
        }
        return compiled;
    }

    public static String[] split(final String value, final String expression) {
        final boolean enabled = AE2SearchOptimizationConfig.isSearchOptimizationEnabled();
        if (enabled && lastSplit != null && value.equals(lastSplitText) && expression.equals(lastSplitExpression)) {
            return lastSplit;
        }

        final String[] split = value.split(expression);
        if (enabled) {
            lastSplitText = value;
            lastSplitExpression = expression;
            lastSplit = split;
        }
        return split;
    }
}
