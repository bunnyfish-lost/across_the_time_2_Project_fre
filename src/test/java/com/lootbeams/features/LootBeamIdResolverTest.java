package com.lootbeams.features;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Map;

class LootBeamIdResolverTest {
    @Test
    void chaosScaleCoinOverridesItsGenericUnknownRarity() {
        Map<String, Object> known = Map.of("unk", new Object(), "esc", new Object());
        assertEquals("esc", LootBeamIdResolver.firstCached(known, "", "", "esc", "unk"));
    }
}
