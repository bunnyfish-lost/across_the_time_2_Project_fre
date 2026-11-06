package fr.poubone.att2.client.rune;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.data.Att2Triggers;
import fr.poubone.att2.client.data.ScoreCache;
import fr.poubone.att2.client.hud.HUDConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

public final class RuneCostSync1125 {
    private static final Map<Integer, RuneCostSync1125.Seen> COSTS = new HashMap<>();
    private static final Set<Integer> OWN_READS = new HashSet<>();
    private static Object world;
    private static boolean opened;
    private static long quietUntil;
    private static final int[] MENUS = new int[] {336, 337, 338};

    private RuneCostSync1125() {}

    private static boolean scope() {
        Minecraft var0 = Minecraft.getInstance();
        if (var0 != null && var0.level != null && var0.player != null) {
            if (world != var0.level) {
                reset();
                world = var0.level;
            }

            return !FlashbackCompat.isInReplay();
        } else {
            return false;
        }
    }

    public static void reset() {
        COSTS.clear();
        OWN_READS.clear();
        world = null;
        opened = false;
        quietUntil = 0L;
    }

    public static void onOpened() {
        if (scope() && HUDConfig.get().runeMenuEnabled && !opened) {
            opened = true;
            long var0 = System.nanoTime() - 2000000000L;
            COSTS.values().removeIf(var2x -> var2x.at < var0);

            for (int var2 = 0; var2 < 3; var2++) {
                int var3 = var2;
                boolean var4 =
                        RuneCatalog.allRunes().stream()
                                .filter(var1 -> var1.level() / 9 == var3)
                                .anyMatch(
                                        var0x ->
                                                price(var0x, false).isEmpty()
                                                        || price(var0x, true).isEmpty());
                if (var4) {
                    OWN_READS.add(MENUS[var2]);
                    Att2Triggers.send(MENUS[var2]);
                }
            }

            if (!OWN_READS.isEmpty()) {
                quietUntil = System.nanoTime() + 8000000000L;
            }
        }
    }

    public static void onClosed() {
        for (int var1 : OWN_READS) {
            Att2Triggers.cancel(var1);
        }

        opened = false;
        COSTS.clear();
    }

    public static boolean onMessage(Component var0, boolean var1) {
        if (var1 && var0 != null && scope()) {
            try {
                Map<Integer, RuneCostParser1125.Cost> var2 =
                        RuneCostParser1125.parse(
                                var0, var0x -> ScoreCache.readHolder("PRICES", var0x));
                long var3 = System.nanoTime();
                var2.forEach(
                        (var2x, var3x) -> COSTS.put(var2x, new RuneCostSync1125.Seen(var3x, var3)));
                if (!opened && var3 <= quietUntil && !OWN_READS.isEmpty()) {
                    if (var2.keySet().stream()
                            .map(RuneCostParser1125::byTrigger)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .anyMatch(var0x -> OWN_READS.contains(MENUS[var0x.level() / 9]))) {
                        return true;
                    }

                    String var5 = var0.getString().strip();
                    if (var5.startsWith("⚙") && var5.contains("???")) {
                        for (RuneCatalog.RuneDef var7 : RuneCatalog.allRunes()) {
                            if (OWN_READS.contains(MENUS[var7.level() / 9])
                                    && var5.toLowerCase(Locale.ROOT)
                                            .contains("<" + var7.id() + ">")) {
                                return true;
                            }
                        }
                    }
                }
            } catch (RuntimeException var8) {
            }

            return false;
        } else {
            return false;
        }
    }

    public static OptionalInt price(RuneCatalog.RuneDef var0, boolean var1) {
        if (var0 != null && scope()) {
            String var2 = var1 ? var0.escPriceHolder() : var0.priceHolder();
            OptionalInt var3 = valid(ScoreCache.readHolder("PRICES", var2));
            if (var3.isPresent()) {
                return var3;
            } else {
                RuneCostSync1125.Seen var4 = COSTS.get(var0.craftTrigger());
                if (var4 != null) {
                    return var1 ? var4.cost.esc() : var4.cost.powder();
                } else {
                    return valid(ScoreCache.getHolder("PRICES", var2));
                }
            }
        } else {
            return OptionalInt.empty();
        }
    }

    private static OptionalInt valid(OptionalInt var0) {
        return var0.isPresent() && var0.getAsInt() >= 0 ? var0 : OptionalInt.empty();
    }

    public static String state(RuneCatalog.RuneDef var0) {
        OptionalInt var1 = ScoreCache.getHolder("RUNE", var0.unlockHolder());
        if (var1.isEmpty()) {
            return "解锁状态未同步";
        } else if (var1.getAsInt() < 1) {
            return "符文未解锁";
        } else {
            return !price(var0, false).isEmpty() && !price(var0, true).isEmpty() ? "" : "价格未同步";
        }
    }

    private record Seen(RuneCostParser1125.Cost cost, long at) {}
}
