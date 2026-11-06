package fr.poubone.att2.client.gambling;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;

import java.util.ArrayList;
import java.util.Optional;

public final class CasinoOptionText {
    private CasinoOptionText() {}

    public static Component name(Component var0) {
        if (var0 == null) {
            return Component.empty();
        }

        ArrayList var1 = new ArrayList();
        ArrayList var2 = new ArrayList();
        StringDecomposer.iterateFormatted(
                var0,
                Style.EMPTY,
                (var2x, var3x, var4x) -> {
                    var1.add(var4x);
                    var2.add(var3x.withClickEvent(null).withHoverEvent(null));
                    return true;
                });
        int var3 = 0;
        int var4 = var1.size();

        while (var3 < var4 && space((Integer) var1.get(var3))) {
            var3++;
        }

        while (var4 > var3 && space((Integer) var1.get(var4 - 1))) {
            var4--;
        }

        if (var3 < var4 && (Integer) var1.get(var3) == 60) {
            var3++;

            while (var3 < var4 && space((Integer) var1.get(var3))) {
                var3++;
            }
        }

        if (var4 > var3 && (Integer) var1.get(var4 - 1) == 62) {
            var4--;

            while (var4 > var3 && space((Integer) var1.get(var4 - 1))) {
                var4--;
            }
        }

        MutableComponent var5 = Component.empty();

        while (var3 < var4) {
            Style var6 = (Style) var2.get(var3);
            StringBuilder var7 = new StringBuilder();

            do {
                var7.appendCodePoint((Integer) var1.get(var3++));
            } while (var3 < var4 && var6.equals(var2.get(var3)));

            var5.append(Component.literal(var7.toString()).withStyle(var6));
        }

        return var5;
    }

    private static boolean space(int var0) {
        return Character.isWhitespace(var0) || Character.isSpaceChar(var0);
    }

    public static Component fit(Font var0, Component var1, int var2) {
        if (var0.width(var1) <= var2) {
            return var1;
        }

        FormattedText var3 = var0.substrByWidth(var1, Math.max(0, var2 - var0.width("…")));
        MutableComponent var4 = Component.empty();
        var3.visit(
                (var1x, var2x) -> {
                    var4.append(Component.literal(var2x).withStyle(var1x));
                    return Optional.empty();
                },
                Style.EMPTY);
        return var4.append(Component.literal("…"));
    }
}
