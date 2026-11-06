package fr.poubone.att2.client.data;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Locale;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class AttributeEffects110 {
    private AttributeEffects110() {}

    public static AttributeEffects110.Value value(
            String var0, int var1, Player var2, boolean var3) {
        return switch (var0) {
            case "STR" -> strength(var1, var2, var3);
            case "CRT" -> critical(var1, var2, var3);
            case "RES" -> new AttributeEffects110.Value(5.0 * var1, "percent", "", 0);
            case "SPD" -> new AttributeEffects110.Value(10.0 * var1, "percent", "", 0);
            case "HER" -> new AttributeEffects110.Value(var1 / 10.0, "health", "", 0);
            case "DAR" ->
                    new AttributeEffects110.Value(
                            var1 <= 0 ? 1.0 : StatEffectText.dahalPerSecond(var1), "dahal", "", 0);
            case "LUC" -> new AttributeEffects110.Value(var1 * 0.5, "number", "", 0);
            case "HAS" -> attackSpeed(var2, var3);
            case "HUN" ->
                    new AttributeEffects110.Value(
                            hungerSeconds(var1), "seconds", "", hungerMode(var1));
            default -> AttributeEffects110.Value.unknown("sync");
        };
    }

    public static AttributeEffects110.Value delta(
            AttributeEffects110.Value var0, AttributeEffects110.Value var1) {
        if (!var0.known() || !var1.known()) {
            return AttributeEffects110.Value.unknown("sync");
        } else {
            return var0.unit.equals(var1.unit) && var0.mode == var1.mode
                    ? new AttributeEffects110.Value(
                            var1.amount - var0.amount, var0.unit, "", var0.mode)
                    : AttributeEffects110.Value.unknown("mode");
        }
    }

    public static double projected(AttributeInstance var0, double var1) {
        AttributeInstance var3 = new AttributeInstance(var0.getAttribute(), var0x -> {});
        var3.setBaseValue(var1);

        for (AttributeModifier var5 : var0.getModifiers()) {
            var3.addTransientModifier(var5);
        }

        return var3.getValue();
    }

    private static AttributeEffects110.Value attackSpeed(Player var0, boolean var1) {
        if (var0 == null) {
            return AttributeEffects110.Value.unknown("weapon");
        }

        AttributeInstance var2 = var0.getAttribute(Attributes.ATTACK_SPEED);
        return var2 == null
                ? AttributeEffects110.Value.unknown("weapon")
                : new AttributeEffects110.Value(
                        var1 ? projected(var2, var2.getBaseValue() + 0.2) : var2.getValue(),
                        "attack",
                        "",
                        0);
    }

    private static AttributeEffects110.Value strength(int var0, Player var1, boolean var2) {
        OptionalDouble var3 = AttributeObservedValues111.get("STR");
        if (!var2 && var3.isPresent()) {
            return new AttributeEffects110.Value(Math.floor(var3.getAsDouble()), "integer", "", 0);
        }

        if (var1 == null) {
            return AttributeEffects110.Value.unknown("weapon");
        }

        ItemStack var4 = var1.getMainHandItem();
        String var5 = itemId(var4);
        if (!var5.equals("minecraft:bow") && !var5.equals("minecraft:crossbow")) {
            StatEffectText.WeaponStats var12 = AttributeWeapon111.of(var1);
            if (var12 == null) {
                return AttributeEffects110.Value.unknown("weapon");
            }

            double var13 = AttributeWeapon111.strengthValue(var1, var0, var12);
            if (var2 && var3.isPresent()) {
                double var15 = AttributeWeapon111.strengthValue(var1, var0 - 1, var12);
                var13 = var3.getAsDouble() + var13 - var15;
            }

            return new AttributeEffects110.Value(var13, "integer", "", 0);
        } else {
            int var6 = 0;
            ItemEnchantments var7 = var4.get(DataComponents.ENCHANTMENTS);
            if (var7 != null) {
                for (var var9 : var7.entrySet()) {
                    if (((Holder) var9.getKey()).is(Identifier.withDefaultNamespace("power"))) {
                        var6 = var9.getIntValue();
                    }
                }
            }

            double var14 = (long) Math.max(1, var0) * (var6 + 1) * 125L / 100L;
            double var10 = (long) Math.max(1, var0 - 1) * (var6 + 1) * 125L / 100L;
            return new AttributeEffects110.Value(
                    var2 && var3.isPresent() ? var3.getAsDouble() + var14 - var10 : var14,
                    "integer",
                    "",
                    0);
        }
    }

    public static OptionalInt criticalFactor(Player var0, int var1) {
        if (var0 == null) {
            return OptionalInt.empty();
        }

        ItemStack var2 = var0.getMainHandItem();
        SpellLauncherTracker.LauncherState var3 = SpellLauncherTracker.parse(var2);
        if (var3 == null) {
            String var7 = itemId(var2);
            if (var7.endsWith("_sword")) {
                return OptionalInt.of(5);
            } else if (var7.endsWith("_shovel")) {
                return OptionalInt.of(6);
            } else {
                return !var7.endsWith("_axe")
                                && !var7.endsWith("_spear")
                                && !var7.endsWith("_pickaxe")
                                && !var7.endsWith("_hoe")
                        ? OptionalInt.of(0)
                        : OptionalInt.of(4);
            }
        } else {
            OptionalInt var4 =
                    ScoreCache.getHolder("Critical", "SPELL" + var3.spellId + "_CRITICAL");
            if (var4.isPresent()) {
                return var4;
            }

            OptionalDouble var5 = AttributeObservedValues111.get("CRT");
            if (var5.isPresent() && var1 > 0 && var5.getAsDouble() % var1 == 0.0) {
                return OptionalInt.of((int) var5.getAsDouble() / var1);
            }

            OptionalInt var6 = ScoreCache.readLive("CRT_DATA");
            return var6.isPresent() && var1 > 0 && var6.getAsInt() % var1 == 0
                    ? OptionalInt.of(var6.getAsInt() / var1)
                    : OptionalInt.empty();
        }
    }

    private static AttributeEffects110.Value critical(int var0, Player var1, boolean var2) {
        if (var0 <= 0) {
            return new AttributeEffects110.Value(0.0, "percent", "", 0);
        }

        OptionalInt var3 = criticalFactor(var1, var2 ? var0 - 1 : var0);
        return var3.isEmpty()
                ? AttributeEffects110.Value.unknown("weapon")
                : new AttributeEffects110.Value(
                        var0 <= 0 ? 0.0 : (double) var0 * var3.getAsInt(), "percent", "", 0);
    }

    private static String itemId(ItemStack var0) {
        return var0 != null && !var0.isEmpty()
                ? BuiltInRegistries.ITEM.getKey(var0.getItem()).toString()
                : "";
    }

    public static int hungerMode(int var0) {
        return var0 <= 5 ? -1 : (var0 <= 7 ? 0 : 1);
    }

    public static double hungerSeconds(int var0) {
        if (var0 <= -10) {
            return 1.5;
        }

        if (var0 <= -8) {
            return 2.0;
        }

        if (var0 <= -6) {
            return 2.5;
        }

        return switch (var0) {
            case -5 -> 2.75;
            case -4 -> 3.25;
            case -3 -> 4.0;
            case -2 -> 5.0;
            case -1 -> 6.25;
            case 0 -> 7.5;
            case 1 -> 8.75;
            case 2 -> 10.0;
            case 3 -> 11.25;
            case 4 -> 12.5;
            case 5 -> 15.0;
            case 6, 7 -> 0.0;
            case 8 -> 125.0;
            case 9 -> 112.5;
            case 10 -> 100.0;
            case 11 -> 87.5;
            case 12 -> 75.0;
            case 13 -> 62.5;
            case 14 -> 56.0;
            case 15 -> 50.0;
            default -> 40.0;
        };
    }

    public static String number(AttributeEffects110.Value var0) {
        if (!var0.known()) {
            return "";
        }

        return switch (var0.unit) {
            case "integer", "dahal" -> String.format(Locale.ROOT, "%.0f", var0.amount);
            case "health" -> String.format(Locale.ROOT, "%.2f", var0.amount);
            case "attack" ->
                    String.format(Locale.ROOT, "%.2f", var0.amount)
                            .replaceAll("0+$", "")
                            .replaceAll("\\.$", "");
            default ->
                    String.format(Locale.ROOT, "%.2f", var0.amount)
                            .replaceAll("0+$", "")
                            .replaceAll("\\.$", "");
        };
    }

    public record Value(double amount, String unit, String reason, int mode) {
        public boolean known() {
            return Double.isFinite(this.amount);
        }

        public static AttributeEffects110.Value unknown(String var0) {
            return new AttributeEffects110.Value(Double.NaN, "", var0, 0);
        }
    }
}
