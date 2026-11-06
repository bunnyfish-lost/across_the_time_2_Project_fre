package fr.poubone.att2.client.rune;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.data.Att2Triggers;

import net.minecraft.client.Minecraft;

import java.lang.ref.WeakReference;

public final class RuneMapDetails207 {
    private static WeakReference<Object> world = new WeakReference<>(null);
    private static boolean requested;

    private RuneMapDetails207() {}

    public static void onTab(RuneCatalog.Tab var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var1 != null
                && var1.level != null
                && var1.player != null
                && !FlashbackCompat.isInReplay()) {
            if (world.get() != var1.level) {
                world = new WeakReference<>(var1.level);
                requested = false;
            }

            if (var0 == RuneCatalog.Tab.WORDS
                    && !requested
                    && !RuneCatalog.words().stream()
                            .allMatch(
                                    var0x -> RuneItemDetails.hasCaptured(RuneItems.word(var0x)))) {
                requested = true;
                Att2Triggers.send(341);
            }
        }
    }

    public static void onClosed() {
        if (requested) {
            Att2Triggers.cancel(341);
        }

        requested = false;
    }
}
