package fr.poubone.att2.client.screen;

import fr.poubone.att2.client.data.AttributeDialogParser;
import fr.poubone.att2.client.data.AttributeEffects110;
import fr.poubone.att2.client.data.ScoreCache;
import fr.poubone.att2.client.data.StatCaps;
import fr.poubone.att2.client.data.StatUpgradeModel;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

public final class AttributePanelData110 {
    private AttributePanelData110() {}

    public static String t(String var0, Object... var1) {
        String var2 = ModLanguageManager.getString("screen.stat110." + var0);

        for (int var3 = 0; var3 < var1.length; var3++) {
            var2 = var2.replace("{" + var3 + "}", String.valueOf(var1[var3]));
        }

        return var2;
    }

    public static OptionalInt total(String var0) {
        OptionalInt var1 = StatUpgradeModel.total(var0);
        if (var1.isPresent()) {
            return var1;
        }

        Map<String, Integer> var2 = StatUpgradeModel.sources(var0);
        if (!var2.containsKey("base")) {
            return ScoreCache.get(var0 + "_TOT");
        }

        long var3 = 0L;

        for (int var6 : var2.values()) {
            var3 += var6;
        }

        return var3 >= -2147483648L && var3 <= 2147483647L
                ? OptionalInt.of((int) var3)
                : OptionalInt.empty();
    }

    public static AttributePanelData110.Row row(String var0, Player var1) {
        OptionalInt var2 = StatUpgradeModel.base(var0);
        OptionalInt var3 = total(var0);
        OptionalInt var4 = StatUpgradeModel.upgradeCost(var0);
        AttributeEffects110.Value var5 = AttributeEffects110.Value.unknown("sync");
        AttributeEffects110.Value var6 =
                var3.isPresent()
                        ? AttributeEffects110.value(var0, var3.getAsInt(), var1, false)
                        : var5;
        int var7 = var3.isPresent() ? var3.getAsInt() + 1 : 0;
        Map<String, Integer> var8 = StatUpgradeModel.sources(var0);
        if (var0.equals("SPD")
                && StatUpgradeModel.total(var0).orElse(Integer.MIN_VALUE) == 2
                && var8.containsKey("base")
                && var8.values().stream().mapToInt(Integer::intValue).sum() > 2) {
            var7 = 2;
        }

        AttributeEffects110.Value var9 =
                var2.isPresent() && var2.getAsInt() < StatCaps.maxBase(var0) && var3.isPresent()
                        ? AttributeEffects110.value(var0, var7, var1, true)
                        : var5;
        return new AttributePanelData110.Row(
                var0,
                var2,
                var3,
                StatCaps.maxBase(var0),
                var4,
                var6,
                var9,
                AttributeEffects110.delta(var6, var9),
                StatUpgradeModel.sources(var0));
    }

    public static String format(AttributeEffects110.Value var0, boolean var1) {
        if (!var0.known()) {
            return t(
                    var0.reason().equals("mode")
                            ? "mode_change"
                            : (var0.reason().equals("weapon") ? "weapon" : "unknown"));
        }

        String var2 = AttributeEffects110.number(var0);
        if (var1 && var0.amount() > 1.0E-8) {
            var2 = "+" + var2;
        }

        String var3 = var0.unit();

        String var4 =
                switch (var3) {
                    case "percent" -> var1 ? t("unit.pp") : "%";
                    case "attack" -> t("unit.attack");
                    case "health" -> t("unit.health");
                    case "dahal" -> t("unit.dahal");
                    case "seconds" -> t("unit.seconds");
                    default -> "";
                };
        return var2 + var4;
    }

    public static List<Component> tooltip(AttributePanelData110.Row var0) {
        ArrayList var1 = new ArrayList();
        var1.add(ModLanguageManager.get("stat.label." + var0.key.toLowerCase(Locale.ROOT)));
        String var2 =
                var0.maxed()
                        ? t("maxed")
                        : (var0.cost().isPresent() && var0.cost().getAsInt() > 0
                                ? t("tooltip.upgrade_cost", var0.cost().getAsInt())
                                : t("tooltip.cost_pending"));
        var1.add(Component.literal(var2));
        var1.add(Component.literal(t("sources")));
        if (var0.sources.isEmpty()) {
            var1.add(Component.literal(t("reading")));
        } else {
            for (String var4 : AttributeDialogParser.SOURCE_SUFFIXES) {
                if (var0.sources.containsKey(var4)) {
                    int var5 = var0.sources.get(var4);
                    String var6 = var5 > 0 ? "+" + var5 : String.valueOf(var5);
                    var1.add(
                            Component.literal(t("source." + var4) + "：" + var6)
                                    .withColor(
                                            var5 > 0 ? 5635925 : (var5 < 0 ? 16733525 : 11184810)));
                }
            }
        }

        if (var0.total.isPresent()) {
            var1.add(Component.literal(t("total_detail", var0.total.getAsInt())));
        }

        if (var0.key.equals("HUN") && var0.current.known()) {
            var1.add(Component.literal(t("hunger.mode." + var0.current.mode())));
        }

        return var1;
    }

    public record Row(
            String key,
            OptionalInt base,
            OptionalInt total,
            int cap,
            OptionalInt cost,
            AttributeEffects110.Value current,
            AttributeEffects110.Value next,
            AttributeEffects110.Value delta,
            Map<String, Integer> sources) {
        public boolean maxed() {
            return this.base.isPresent() && this.base.getAsInt() >= this.cap;
        }

        public boolean ready(int var1) {
            return StatUpgradeModel.hasData()
                    && this.base.isPresent()
                    && this.total.isPresent()
                    && !this.maxed()
                    && this.cost.isPresent()
                    && this.cost.getAsInt() > 0
                    && var1 >= this.cost.getAsInt();
        }
    }
}
