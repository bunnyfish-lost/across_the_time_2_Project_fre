package fr.poubone.att2.client.rune;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CraftSeriesLogicTest {
    @Test
    void synthSendsEqualQty() {
        assertEquals(10, CraftSeriesLogic.triggerSends(10, false));
        assertEquals(0, CraftSeriesLogic.triggerSends(0, false));
    }

    @Test
    void wordsSendsArmPlusQty() {
        assertEquals(4, CraftSeriesLogic.triggerSends(3, true)); // 1 arm + 3 crafts
        assertEquals(0, CraftSeriesLogic.triggerSends(0, true));
    }
}
