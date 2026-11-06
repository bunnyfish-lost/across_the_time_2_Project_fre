package fr.poubone.att2.client.hud;

import fr.poubone.att2.client.data.StatManager;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;

public final class StatsSettings207 {
    private StatsSettings207() {}

    public static void defaults(HUDConfig var0) {
        var0.visibleStatKeys = new ArrayList<>(StatManager.STAT_KEYS);
        var0.shopFollowGuiScale = true;
    }

    public static void normalize(HUDConfig var0) {
        if (var0.visibleStatKeys == null) {
            var0.visibleStatKeys = new ArrayList<>(StatManager.STAT_KEYS);
        } else {
            var0.visibleStatKeys =
                    new ArrayList<>(
                            new LinkedHashSet<>(
                                    var0.visibleStatKeys.stream()
                                            .filter(Objects::nonNull)
                                            .filter(StatManager.STAT_KEYS::contains)
                                            .toList()));
        }

        if (var0.statsDisplayMode < 0 || var0.statsDisplayMode > 1) {
            var0.statsDisplayMode = 0;
        }

        if (var0.statsLayoutMode < 0 || var0.statsLayoutMode > 2) {
            var0.statsLayoutMode = 0;
        }
    }

    public static boolean visible(HUDConfig var0, String var1) {
        return var0.visibleStatKeys != null && var0.visibleStatKeys.contains(var1);
    }

    public static void setVisible(HUDConfig var0, String var1, boolean var2) {
        if (var1 != null && StatManager.STAT_KEYS.contains(var1)) {
            if (var0.visibleStatKeys == null) {
                var0.visibleStatKeys = new ArrayList<>();
            }

            if (var2) {
                if (!var0.visibleStatKeys.contains(var1)) {
                    var0.visibleStatKeys.add(var1);
                }
            } else {
                var0.visibleStatKeys.remove(var1);
            }
        }
    }

    public static boolean isNegative(Component var0) {
        if (var0 == null) {
            return false;
        }

        String var1 = var0.getString();
        if (var1.indexOf(167) >= 0) {
            var1 = var1.replaceAll("§.", "");
        }

        var1 = var1.stripLeading();
        if (!var1.isEmpty() && "-−－".indexOf(var1.charAt(0)) >= 0) {
            int var2 = 1;

            while (var2 < var1.length() && Character.isWhitespace(var1.charAt(var2))) {
                var2++;
            }

            return var2 < var1.length()
                    && (Character.isDigit(var1.charAt(var2))
                            || var1.charAt(var2) == '.'
                                    && var2 + 1 < var1.length()
                                    && Character.isDigit(var1.charAt(var2 + 1)));
        } else {
            return false;
        }
    }
}
