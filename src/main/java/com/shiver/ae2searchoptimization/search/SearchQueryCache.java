package com.shiver.ae2searchoptimization.search;

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
        if (lastPattern != null && lastPatternFlags == flags && regex.equals(lastPatternText)) {
            return lastPattern;
        }

        Pattern compiled;
        try {
            compiled = Pattern.compile(regex, flags);
        } catch (final PatternSyntaxException ignored) {
            // This is the same fallback used by AE2's original search code.
            compiled = Pattern.compile(Pattern.quote(regex), flags);
        }

        lastPatternText = regex;
        lastPatternFlags = flags;
        lastPattern = compiled;
        return compiled;
    }

    public static String[] split(final String value, final String expression) {
        if (lastSplit != null && value.equals(lastSplitText) && expression.equals(lastSplitExpression)) {
            return lastSplit;
        }

        lastSplitText = value;
        lastSplitExpression = expression;
        lastSplit = value.split(expression);
        return lastSplit;
    }
}
