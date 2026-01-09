package org.apache.openjpa.lib.util.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.openjpa.lib.util.collections.AbstractLinkedMap.LinkEntry;
import org.junit.Test;

public class LRUMapTest {

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMaxSizeBelowOne() {
        new LRUMap<>(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInitialSizeGreaterThanMax() {
        new LRUMap<>(1, 2);
    }

    @Test
    public void evictsLeastRecentlyUsedEntryOnOverflow() {
        LRUMap<String, Integer> map = new LRUMap<>(2);
        map.put("a", 1);
        map.put("b", 2);

        map.put("c", 3);

        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
        assertEquals(2, map.size());
    }

    @Test
    public void getPromotesEntryBeforeEviction() {
        LRUMap<String, Integer> map = new LRUMap<>(2);
        map.put("a", 1);
        map.put("b", 2);

        map.get("a");
        map.put("c", 3);

        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    @Test
    public void scanUntilRemovableSkipsNonRemovableEntries() {
        RecordingLRUMap<String, Integer> map = new RecordingLRUMap<>(2, true);
        map.put("a", 1);
        map.put("b", 2);

        map.put("c", 3);

        assertEquals(Arrays.asList("a", "b"), map.getSeenKeys());
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
        assertEquals(2, map.size());
    }

    private static final class RecordingLRUMap<K, V> extends LRUMap<K, V> {
        private final List<K> seenKeys = new ArrayList<>();

        RecordingLRUMap(int maxSize, boolean scanUntilRemovable) {
            super(maxSize, maxSize, DEFAULT_LOAD_FACTOR, scanUntilRemovable);
        }

        @Override
        protected boolean removeLRU(LinkEntry<K, V> entry) {
            seenKeys.add(entry.getKey());
            return seenKeys.size() > 1;
        }

        List<K> getSeenKeys() {
            return seenKeys;
        }
    }
}
