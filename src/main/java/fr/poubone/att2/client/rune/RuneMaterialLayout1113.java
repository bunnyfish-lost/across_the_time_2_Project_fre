package fr.poubone.att2.client.rune;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class RuneMaterialLayout1113 {
    private RuneMaterialLayout1113() {}

    public static RuneMaterialLayout1113.Columns columns(int var0, int var1, int var2) {
        var0 = Math.max(64, var0);
        int var3 = Math.min(24, Math.max(12, var0 / 8));
        int var4 = var3 + 3;
        int var5 = Math.min(Math.max(24, var2), Math.max(12, var0 / 3));
        int var6 = var0 - var5;
        int var7 = var0 / 2;
        int var8 =
                Math.max(
                        1,
                        Math.min(
                                Math.max(24, var1),
                                Math.min(var0 / 3, 2 * Math.max(1, var6 - 4 - var7))));
        int var9 = var7 - var8 / 2;
        int var10 = Math.min(16, Math.max(1, var9 - 4 - var4));
        int var11 = var4 + var10 + 5;
        return new RuneMaterialLayout1113.Columns(
                var3, var4, var10, var11, Math.max(0, var9 - 4 - var11), var9, var8, var6, var5);
    }

    public static void number(
            GuiGraphics var0,
            Font var1,
            String var2,
            int var3,
            int var4,
            int var5,
            int var6,
            boolean var7,
            boolean var8) {
        if (var5 > 0) {
            int var9 = var1.width(var2);
            float var10 = Math.min(1.0F, (float) var5 / Math.max(1, var9));
            float var11 = var9 * var10;
            float var12 = var3 + (var7 ? (var5 - var11) / 2.0F : var5 - var11);
            if (var10 == 1.0F) {
                var0.drawString(var1, var2, Math.round(var12), var4, var6, var8);
            } else {
                var0.pose().pushMatrix();
                var0.pose().translate(var12, var4 + (9.0F - 9.0F * var10) / 2.0F);
                var0.pose().scale(var10, var10);
                var0.drawString(var1, var2, 0, 0, var6, var8);
                var0.pose().popMatrix();
            }
        }
    }

    public record Columns(
            int ordinalWidth,
            int materialLeft,
            int iconSize,
            int nameLeft,
            int nameWidth,
            int pouchLeft,
            int pouchWidth,
            int countLeft,
            int countWidth) {}
}
