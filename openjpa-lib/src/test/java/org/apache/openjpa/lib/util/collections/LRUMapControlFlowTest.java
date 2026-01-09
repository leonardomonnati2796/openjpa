/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.lib.util.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

/**
 * Control-flow guided tests for LRUMap: hit eviction paths and promotion logic.
 */
public class LRUMapControlFlowTest {

    @Test
    public void evictsLeastRecentlyUsedEntryOnOverflow() {
        LRUMap<String, Integer> map = new LRUMap<>(2);
        map.put("a", 1);
        map.put("b", 2);

        map.put("c", 3); // should evict "a"

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

        map.get("a"); // promotes a
        map.put("c", 3); // evicts b

        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    @Test
    public void scanUntilRemovableSkipsNonRemovableEntries() {
        RecordingLRUMap<String, Integer> map = new RecordingLRUMap<>(2, true);
        map.put("a", 1);
        map.put("b", 2);

        map.put("c", 3); // first candidate rejected, second accepted

        assertEquals(Arrays.asList("a", "b"), map.seenKeys);
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    private static final class RecordingLRUMap<K, V> extends LRUMap<K, V> {
        final java.util.List<K> seenKeys = new java.util.ArrayList<>();

        RecordingLRUMap(int maxSize, boolean scanUntilRemovable) {
            super(maxSize, maxSize, DEFAULT_LOAD_FACTOR, scanUntilRemovable);
        }

        @Override
        protected boolean removeLRU(AbstractLinkedMap.LinkEntry<K, V> entry) {
            seenKeys.add(entry.getKey());
            return seenKeys.size() > 1; // evict second candidate
        }
    }
}
