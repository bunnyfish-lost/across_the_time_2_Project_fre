package fr.poubone.att2.client.quest;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.network.chat.Component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class QuestTrackingStoreTest {
    @TempDir Path directory;

    private QuestInfo quest(int number) {
        return new QuestInfo(
                QuestType.SIDE,
                number,
                Component.literal("任务 " + number),
                QuestStatus.STARTED,
                1000 + number);
    }

    @Test
    void aSinglePinnedQuestOverridesLastTrackedQuestAndSurvivesReload() {
        Path file = directory.resolve("tracking.json");
        var store = new QuestTrackingStore(file);
        var first = quest(1);
        var second = quest(2);
        store.recordLast("world-a", first);
        store.togglePin("world-a", second);
        assertEquals(second.key(), store.target("world-a", key -> null).key());
        var loaded = new QuestTrackingStore(file);
        assertTrue(loaded.isPinned("world-a", second));
        loaded.togglePin("world-a", first);
        assertFalse(loaded.isPinned("world-a", second));
        assertTrue(loaded.isPinned("world-a", first));
        loaded.togglePin("world-a", first);
        assertNull(loaded.pinned("world-a", key -> null));
        assertEquals(first.key(), loaded.target("world-a", key -> null).key());
    }

    @Test
    void worldProfilesDoNotSharePinnedOrLastTrackedQuests() {
        var store = new QuestTrackingStore(directory.resolve("tracking.json"));
        store.togglePin("world-a", quest(3));
        assertNull(store.pinned("world-b", key -> null));
        assertNull(store.target("world-b", key -> null));
    }

    @Test
    void liveQuestDataTakesPriorityOverSavedSnapshot() {
        var store = new QuestTrackingStore(directory.resolve("tracking.json"));
        store.togglePin("world-a", quest(4));
        var updated =
                new QuestInfo(
                        QuestType.SIDE,
                        4,
                        Component.literal("更新后的任务"),
                        QuestStatus.COMPLETED,
                        1004);
        assertSame(updated, store.pinned("world-a", key -> updated));
    }
}
