package com.lootbeams.managers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class TooltipManagerTest {

    @Test
    void pickupParticleWithoutATrackedStackDoesNotCrash() {
        assertDoesNotThrow(() -> TooltipManager.onEntityRenderEnd(null));
        assertDoesNotThrow(() -> TooltipManager.onEntityRender(null));
    }
}
