package fr.poubone.att2.client.data;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public final class StatUpgradeModel {
    private static int version;
    private static boolean listening;
    private static boolean received;
    private static int skillPoints;
    private static final Map<String, Integer> totals = new HashMap<>();
    private static final Map<String, Integer> costs = new HashMap<>();
    private static final Map<String, Integer> bases = new HashMap<>();
    private static final Map<String, Map<String, Integer>> sources = new HashMap<>();

    public static int version() {
        return version;
    }

    public static OptionalInt total(String var0) {
        return received && totals.containsKey(var0)
                ? OptionalInt.of(totals.get(var0))
                : OptionalInt.empty();
    }

    private StatUpgradeModel() {}

    public static void open() {
        listening = true;
        received = false;
        skillPoints = 0;
        costs.clear();
        bases.clear();
        sources.clear();
        totals.clear();
        version++;
        Att2Triggers.send(2313);
    }

    public static void close() {
        listening = false;
    }

    public static void reset() {
        version++;
        listening = false;
        received = false;
        skillPoints = 0;
        totals.clear();
        costs.clear();
        bases.clear();
        sources.clear();
    }

    public static boolean isListening() {
        return listening;
    }

    public static boolean hasData() {
        return received;
    }

    public static int skillPoints() {
        return skillPoints;
    }

    public static OptionalInt upgradeCost(String var0) {
        return received && var0 != null && costs.containsKey(var0)
                ? OptionalInt.of(costs.get(var0))
                : OptionalInt.empty();
    }

    public static OptionalInt base(String var0) {
        return received && var0 != null && bases.containsKey(var0)
                ? OptionalInt.of(bases.get(var0))
                : OptionalInt.empty();
    }

    public static Map<String, Integer> sources(String var0) {
        return received && var0 != null ? sources.getOrDefault(var0, Map.of()) : Map.of();
    }

    public static boolean onDialog(Holder<Dialog> var0) {
        if (listening && var0 != null) {
            Dialog var1 = (Dialog) var0.value();
            if (var1 instanceof MultiActionDialog var2) {
                ArrayList var3 = new ArrayList();
                MutableComponent var4 = Component.empty();

                for (ActionButton var6 : var2.actions()) {
                    Component var7 = var6.button().label();
                    Component var8 = var6.button().tooltip().orElse(null);
                    if (var8 != null && DialogComponents.hasKey(var7, "consciousness.stat.point")) {
                        var4 = var4.copy().append(var8);
                    }

                    int var9 = triggerOf(var6);
                    if (var9 >= 0) {
                        var3.add(new AttributeDialogParser.Action(var7, var9));
                    }
                }

                MutableComponent var10 = Component.empty();

                for (DialogBody var12 : var2.common().body()) {
                    if (var12 instanceof PlainMessage var13) {
                        var10 = var10.copy().append(var13.contents());
                    }
                }

                return applyIfListening(var2.common().title(), var3, var10, var4);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean applyIfListening(
            Component var0, List<AttributeDialogParser.Action> var1) {
        return applyIfListening(var0, var1, Component.empty(), Component.empty());
    }

    public static boolean applyIfListening(
            Component var0, List<AttributeDialogParser.Action> var1, Component var2) {
        return applyIfListening(var0, var1, var2, Component.empty());
    }

    public static boolean applyIfListening(
            Component var0,
            List<AttributeDialogParser.Action> var1,
            Component var2,
            Component var3) {
        if (!listening) {
            return false;
        }

        Optional var4 = AttributeDialogParser.parse(var0, var1);
        if (var4.isEmpty()) {
            return false;
        }

        skillPoints = ((AttributeDialogParser.Snapshot) var4.get()).skillPoints();
        totals.clear();
        totals.putAll(AttributeTotals110.parse(var1));
        costs.clear();
        costs.putAll(((AttributeDialogParser.Snapshot) var4.get()).costs());
        bases.clear();
        Map var5 = AttributeDialogParser.parseBases(var3);
        if (var5.isEmpty() && var2 != null && !var2.getString().isEmpty()) {
            var5 = AttributeDialogParser.parseBases(var2);
        }

        bases.putAll(var5);
        sources.clear();
        sources.putAll(AttributeDialogParser.parseSources(var3));
        received = true;
        version++;
        return true;
    }

    private static int triggerOf(ActionButton var0) {
        return var0.action().isPresent()
                        && var0.action().get() instanceof StaticAction var2
                        && var2.value() instanceof RunCommand var1
                ? Att2Triggers.parseTriggerCommand(var1.command())
                : -1;
    }
}
