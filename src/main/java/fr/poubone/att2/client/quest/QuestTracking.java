package fr.poubone.att2.client.quest;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;

import java.util.Locale;

public final class QuestTracking {
    private static final QuestTrackingStore STORE =
            new QuestTrackingStore(
                    FabricLoader.getInstance().getConfigDir().resolve("att2_quest_tracking.json"));
    private static long lastQuickTrack;

    private QuestTracking() {}

    private static String scope() {
        Minecraft var0 = Minecraft.getInstance();
        if (var0 != null && var0.player != null) {
            String var1;
            if (var0.getSingleplayerServer() != null) {
                var1 =
                        "local:"
                                + var0.getSingleplayerServer()
                                        .getWorldPath(LevelResource.ROOT)
                                        .toAbsolutePath()
                                        .normalize();
            } else {
                if (var0.getCurrentServer() == null) {
                    return null;
                }

                var1 = "server:" + var0.getCurrentServer().ip.toLowerCase(Locale.ROOT);
            }

            return var1 + "|" + var0.player.getUUID();
        } else {
            return null;
        }
    }

    public static QuestInfo pinned() {
        String var0 = scope();
        return var0 == null ? null : STORE.pinned(var0, QuestModel.get()::findByKey);
    }

    public static boolean isPinned(QuestInfo var0) {
        String var1 = scope();
        return var1 != null && STORE.isPinned(var1, var0);
    }

    public static void togglePin(QuestInfo var0) {
        String var1 = scope();
        if (var1 != null && !FlashbackCompat.isInReplay()) {
            STORE.togglePin(var1, var0);
        }
    }

    public static boolean track(QuestInfo var0) {
        String var1 = scope();
        if (var1 != null && var0 != null && !FlashbackCompat.isInReplay()) {
            int var2 = var0.isMain() ? 1068 : var0.progressTrigger();
            if (var2 <= 0) {
                return false;
            }

            QuestModel.get().requestTracking(var0);
            STORE.recordLast(var1, var0);
            return true;
        } else {
            return false;
        }
    }

    public static void quickTrack() {
        Minecraft var0 = Minecraft.getInstance();
        String var1 = scope();
        if (var1 != null && var0.screen == null && !FlashbackCompat.isInReplay()) {
            long var2 = System.currentTimeMillis();
            if (var2 - lastQuickTrack >= 250L) {
                lastQuickTrack = var2;
                QuestInfo var4 = STORE.target(var1, QuestModel.get()::findByKey);
                if (!track(var4)) {
                    var0.player.displayClientMessage(
                            ModLanguageManager.get(
                                    var4 == null
                                            ? "quest_book.track.none"
                                            : "quest_book.track.unavailable"),
                            true);
                }
            }
        }
    }
}
