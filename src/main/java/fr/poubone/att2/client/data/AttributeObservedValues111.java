package fr.poubone.att2.client.data;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.BossEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class AttributeObservedValues111 {
    private static final Map<String, AttributeObservedValues111.Seen> values = new HashMap<>();

    private AttributeObservedValues111() {}

    public static void reset() {
        values.clear();
    }

    public static OptionalDouble get(String var0) {
        AttributeObservedValues111.Seen var1 = values.get(var0);
        return var1 != null && System.nanoTime() - var1.time < 4000000000L
                ? OptionalDouble.of(var1.value)
                : OptionalDouble.empty();
    }

    public static void capture(Map<?, ? extends BossEvent> var0) {
        if (var0 != null) {
            for (BossEvent var2 : var0.values()) {
                if (var2 != null) {
                    observe(var2.getName());
                }
            }
        }
    }

    public static void observe(Component var0) {
        if (var0 != null && MapStatBar.hasDetailedValues(var0)) {
            walk(var0);
        }
    }

    private static OptionalDouble numeric(Object var0) {
        String var1;
        if (var0 instanceof Component var2) {
            if (var2.getContents() instanceof ScoreContents var3) {
                Optional var8 = var3.name().right();
                if (var8.isEmpty()) {
                    return OptionalDouble.empty();
                }

                OptionalInt var5 = ScoreCache.getHolder(var3.objective(), (String) var8.get());
                return var5.isPresent()
                        ? OptionalDouble.of(var5.getAsInt())
                        : OptionalDouble.empty();
            }

            var1 = var2.getString();
        } else {
            var1 = String.valueOf(var0);
        }

        try {
            double var7 = Double.parseDouble(ChatFormatting.stripFormatting(var1).strip());
            return Double.isFinite(var7) ? OptionalDouble.of(var7) : OptionalDouble.empty();
        } catch (NumberFormatException var6) {
            return OptionalDouble.empty();
        }
    }

    private static void walk(Component var0) {
        if (var0.getContents() instanceof TranslatableContents var1) {
            String var11 = var1.getKey();
            Object[] var3 = var1.getArgs();
            String var4 =
                    var11.equals("att2.stat.display.str")
                            ? "STR"
                            : (var11.startsWith("att2.stat.display.crt.value") ? "CRT" : null);
            if (var4 != null && var3.length > 0) {
                OptionalDouble var5 = numeric(var3[0]);
                if (var5.isPresent()) {
                    values.put(
                            var4,
                            new AttributeObservedValues111.Seen(
                                    var5.getAsDouble(), System.nanoTime()));
                }
            }

            for (Object var8 : var3) {
                if (var8 instanceof Component var9) {
                    walk(var9);
                }
            }
        }

        for (Component var12 : var0.getSiblings()) {
            walk(var12);
        }
    }

    private record Seen(double value, long time) {}
}
