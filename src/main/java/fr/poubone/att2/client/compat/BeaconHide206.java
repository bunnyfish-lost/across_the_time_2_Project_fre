package fr.poubone.att2.client.compat;

import fr.poubone.att2.client.teleport.TeleportAnimationConfig;

public final class BeaconHide206 {
    private static int sinceRestored = Integer.MAX_VALUE;

    private BeaconHide206() {}

    public static void tick() {
        if (sinceRestored < Integer.MAX_VALUE) {
            sinceRestored++;
        }
    }

    public static void changed(boolean var0, boolean var1) {
        if (var1 && !var0) {
            sinceRestored = 0;
        }
    }

    public static boolean hide(boolean var0, boolean var1) {
        return TeleportAnimationConfig.isEffectEnabled() && (var0 && var1 || sinceRestored <= 60);
    }
}
