package com.shiver.ae2searchoptimization.recentsearch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecentSearchKeyboardNavigationTest {

    @Test
    void startsAtTheFirstOrLastEntryAndWrapsAround() {
        assertEquals(0, RecentSearchKeyboardNavigation.nextIndex(-1, 3, 1));
        assertEquals(2, RecentSearchKeyboardNavigation.nextIndex(-1, 3, -1));
        assertEquals(0, RecentSearchKeyboardNavigation.nextIndex(2, 3, 1));
        assertEquals(2, RecentSearchKeyboardNavigation.nextIndex(0, 3, -1));
        assertEquals(-1, RecentSearchKeyboardNavigation.nextIndex(0, 0, 1));
    }
}
