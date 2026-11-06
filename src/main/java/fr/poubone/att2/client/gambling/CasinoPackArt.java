package fr.poubone.att2.client.gambling;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class CasinoPackArt {
    public static final int INK = -13229295;
    public static final int MUTED = -9022672;
    public static final int CREAM = -1255768;
    public static final int RED = -7921890;
    private static final Identifier BUTTON =
            Identifier.fromNamespaceAndPath("minecraft", "widget/button");
    private static final Identifier HOVER =
            Identifier.fromNamespaceAndPath("minecraft", "widget/button_highlighted");
    private static final Identifier DISABLED =
            Identifier.fromNamespaceAndPath("minecraft", "widget/button_disabled");
    private static final Identifier PAPER =
            Identifier.fromNamespaceAndPath("att2", "textures/casino/wager_paper.png");
    private static final Identifier COIN =
            Identifier.fromNamespaceAndPath("att2", "textures/casino/chronoton.png");
    private static final Identifier PAGE =
            Identifier.fromNamespaceAndPath("att2", "textures/quest_book/parchment_right.png");

    private CasinoPackArt() {}

    public static void chrome(
            GuiGraphics var0, int var1, int var2, int var3, int var4, boolean var5, boolean var6) {
        var0.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                !var6 ? DISABLED : (var5 ? HOVER : BUTTON),
                var1,
                var2,
                var3,
                var4);
    }

    public static void button(
            GuiGraphics var0,
            Font var1,
            int var2,
            int var3,
            int var4,
            int var5,
            String var6,
            boolean var7,
            boolean var8) {
        chrome(var0, var2, var3, var4, var5, var7, var8);
        String var9 = var6;
        if (var1.width(var9) > var4 - 14) {
            var9 = var1.plainSubstrByWidth(var9, Math.max(1, var4 - 14 - var1.width("…"))) + "…";
        }

        var0.drawString(
                var1,
                var9,
                var2 + (var4 - var1.width(var9)) / 2,
                var3 + (var5 - 9) / 2 + 2,
                var8 ? -1255768 : -6123148,
                false);
    }

    public static void sheet(GuiGraphics var0, int var1, int var2, int var3, int var4) {
        var0.blit(
                RenderPipelines.GUI_TEXTURED,
                PAPER,
                var1,
                var2,
                0.0F,
                0.0F,
                var3,
                var4,
                256,
                256,
                256,
                256);
    }

    public static void coin(GuiGraphics var0, int var1, int var2, int var3) {
        var0.blit(
                RenderPipelines.GUI_TEXTURED,
                COIN,
                var1,
                var2,
                0.0F,
                0.0F,
                var3,
                var3,
                64,
                64,
                64,
                64);
    }

    public static void page(GuiGraphics var0, int var1, int var2, int var3, int var4) {
        int[] var5 = new int[] {var1, var1 + 8, var1 + var3 - 8};
        int[] var6 = new int[] {var2, var2 + 8, var2 + var4 - 8};
        int[] var7 = new int[] {8, var3 - 16, 8};
        int[] var8 = new int[] {8, var4 - 16, 8};
        int[] var9 = new int[] {0, 16, 276};
        int[] var10 = new int[] {0, 16, 344};
        int[] var11 = new int[] {16, 260, 16};
        int[] var12 = new int[] {16, 328, 16};

        for (int var13 = 0; var13 < 3; var13++) {
            for (int var14 = 0; var14 < 3; var14++) {
                if (var7[var14] > 0 && var8[var13] > 0) {
                    var0.blit(
                            RenderPipelines.GUI_TEXTURED,
                            PAGE,
                            var5[var14],
                            var6[var13],
                            var9[var14],
                            var10[var13],
                            var7[var14],
                            var8[var13],
                            var11[var14],
                            var12[var13],
                            292,
                            360);
                }
            }
        }
    }
}
