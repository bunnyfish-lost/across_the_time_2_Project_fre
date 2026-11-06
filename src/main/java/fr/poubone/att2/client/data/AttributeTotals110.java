package fr.poubone.att2.client.data;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AttributeTotals110 {
    private AttributeTotals110() {}

    public static Map<String, Integer> parse(List<AttributeDialogParser.Action> var0) {
        ArrayList var1 = new ArrayList();
        if (var0 != null) {
            for (AttributeDialogParser.Action var3 : var0) {
                if (var3 != null) {
                    visit(var3.label(), var1);
                }
            }
        }

        if (var1.size() != AttributeDialogParser.CONSCIENCE_ORDER.size()) {
            return Map.of();
        }

        HashMap var4 = new HashMap();

        for (int var5 = 0; var5 < var1.size(); var5++) {
            var4.put(AttributeDialogParser.CONSCIENCE_ORDER.get(var5), (Integer) var1.get(var5));
        }

        return Map.copyOf(var4);
    }

    private static void visit(Component var0, List<Integer> var1) {
        if (var0 != null) {
            if (var0.getContents() instanceof TranslatableContents var2) {
                if (var2.getKey().equals("consciousness.stat.point") && var2.getArgs().length > 0) {
                    Object var10 = var2.getArgs()[0];
                    String var4 =
                            var10 instanceof Component var5
                                    ? var5.getString()
                                    : String.valueOf(var10);

                    try {
                        var1.add(Integer.parseInt(ChatFormatting.stripFormatting(var4).strip()));
                    } catch (NumberFormatException var8) {
                    }
                }

                for (Object var6 : var2.getArgs()) {
                    if (var6 instanceof Component var7) {
                        visit(var7, var1);
                    }
                }
            }

            for (Component var12 : var0.getSiblings()) {
                visit(var12, var1);
            }
        }
    }
}
