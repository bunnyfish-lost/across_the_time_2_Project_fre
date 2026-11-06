package fr.poubone.att2.client.miner;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public final class PrisonerArt1121 {
    public static final Identifier SHADOW = id("shadow.png");
    public static final Identifier NOVA = id("nova.png");
    public static final Identifier SEAL = id("seal.png");
    public static final Identifier HALO = id("halo.png");
    public static final Identifier TRADE = id("trade.png");
    public static final Identifier ROOT = id("root.png");
    public static final Identifier RESIN = id("resin.png");
    public static final Identifier EXTRACTOR = id("extractor.png");
    public static final Identifier STAR = id("star.png");
    private static final Identifier[] TREES = new Identifier[28];

    private static Identifier id(String var0) {
        return Identifier.fromNamespaceAndPath("att2", "textures/gui/prisoner/" + var0);
    }

    private PrisonerArt1121() {}

    public static void texture(
            GuiGraphics var0,
            Identifier var1,
            int var2,
            int var3,
            int var4,
            int var5,
            int var6,
            int var7,
            int var8) {
        if (var4 > 0 && var5 > 0) {
            var0.blit(
                    RenderPipelines.GUI_TEXTURED,
                    var1,
                    var2,
                    var3,
                    0.0F,
                    0.0F,
                    var4,
                    var5,
                    var6,
                    var7,
                    var6,
                    var7,
                    var8);
        }
    }

    public static void icon(
            GuiGraphics var0, Identifier var1, int var2, int var3, int var4, int var5) {
        texture(
                var0,
                var1,
                var2,
                var3,
                var4,
                var4,
                16,
                16,
                Math.max(0, Math.min(255, var5)) << 24 | 16777215);
    }

    public static void item(GuiGraphics var0, ItemStack var1, int var2, int var3, int var4) {
        if (var1 != null && !var1.isEmpty() && var4 > 0) {
            var0.pose().pushMatrix();
            var0.pose().translate(var2, var3);
            var0.pose().scale(var4 / 16.0F, var4 / 16.0F);
            var0.renderItem(var1, 0, 0);
            var0.pose().popMatrix();
        }
    }

    public static void line(
            GuiGraphics var0,
            float var1,
            float var2,
            float var3,
            float var4,
            float var5,
            int var6) {
        float var7 = var3 - var1;
        float var8 = var4 - var2;
        float var9 = (float) Math.hypot(var7, var8);
        if (!(var9 < 0.1) && !(var5 <= 0.0F)) {
            var0.pose().pushMatrix();
            var0.pose().translate(var1, var2);
            var0.pose().rotate((float) Math.atan2(var8, var7));
            var0.fill(
                    0,
                    -Math.max(1, (int) var5) / 2,
                    Math.max(1, Math.round(var9)),
                    Math.max(1, (int) var5) / 2 + 1,
                    var6);
            var0.pose().popMatrix();
        }
    }

    public static void frame(GuiGraphics var0, int var1, int var2, int var3, int var4, int var5) {
        if (var3 > 0 && var4 > 0) {
            var0.fill(var1, var2, var1 + var3, var2 + 1, var5);
            var0.fill(var1, var2 + var4 - 1, var1 + var3, var2 + var4, var5);
            var0.fill(var1, var2, var1 + 1, var2 + var4, var5);
            var0.fill(var1 + var3 - 1, var2, var1 + var3, var2 + var4, var5);
        }
    }

    public static void tree(
            GuiGraphics var0,
            int var1,
            int var2,
            int var3,
            int var4,
            float var5,
            float var6,
            boolean var7) {
        if (var3 >= 1 && var4 >= 1) {
            float var8 = Math.min(var3 / 256.0F, var4 / 320.0F);
            int var9 = Math.max(1, Math.round(256.0F * var8));
            int var10 = Math.max(1, Math.round(320.0F * var8));
            int var11 = var1 + (var3 - var9) / 2;
            int var12 = var2 + var4 - var10;
            float var13 = Math.max(0.0F, Math.min(27.0F, var5));
            long var14 = PrisonerTrade1121.now();
            var0.enableScissor(var1, var2, var1 + var3, var2 + var4);

            try {
                for (int var16 = 0; var16 < 13; var16++) {
                    int var17 = var1 + 4 + (var16 * 83 + 17) % Math.max(1, var3 - 8);
                    int var18 = var2 + 3 + (var16 * 41 + 19) % Math.max(1, var4 - 10);
                    var0.fill(var17, var18, var17 + 1, var18 + 1, 1350531990);
                }

                if (var7 && var13 >= 26.99) {
                    int var40 = (int) (38.0 + 8.0 * Math.sin(var14 / 2300.0));
                    texture(
                            var0,
                            HALO,
                            var11,
                            var12,
                            var9,
                            var10,
                            256,
                            320,
                            var40 << 24 | 16777215);
                }

                int var41 = (int) Math.ceil(var13);
                if (var7 && var13 > 0.0F) {
                    int var42 = var12 + Math.round((285.0F - 240.0F * var13 / 27.0F) * var8);
                    var0.enableScissor(
                            var11, Math.max(var12, var42 - 3), var11 + var9, var12 + var10);

                    try {
                        texture(var0, TREES[var41], var11, var12, var9, var10, 256, 320, -1);
                    } finally {
                        var0.disableScissor();
                    }
                } else {
                    texture(
                            var0,
                            TREES[var41],
                            var11,
                            var12,
                            var9,
                            var10,
                            256,
                            320,
                            var7 ? -1 : 1886417016);
                }

                int var43 = Math.max(7, Math.round(20.0F * var8));
                icon(
                        var0,
                        ROOT,
                        var11 + var9 / 2 - var43 / 2,
                        var12 + Math.round(285.0F * var8) - var43 / 2,
                        var43,
                        var7 ? 220 : 90);
                if (var7 && var13 > 0.0F) {
                    int var44 = var12 + Math.round((285.0F - 240.0F * var13 / 27.0F) * var8);
                    icon(
                            var0,
                            STAR,
                            var11 + var9 / 2 - 3,
                            var44 - 4,
                            Math.max(3, Math.round(8.0F * var8)),
                            210);

                    for (int var19 = 3; var19 <= Math.floor(var13); var19++) {
                        double var20 = var19 % 2 == 0 ? -1.0 : 1.0;
                        double var22 = Math.min(79.36, 22.0 + (27 - var19) * 2.65);
                        double var24 =
                                128.0 + Math.sin(var19 * 0.81) * 4.0 + Math.sin(var19 * 0.24) * 7.0;
                        double var26 = 285.0 - 240 * var19 / 27.0;
                        double var28 =
                                Math.max(
                                        285.0F - 240.0F * var13 / 27.0F + 5.0F,
                                        var26 - (12 + var19 % 4 * 6));
                        int var30 = var11 + (int) Math.round((var24 + var20 * var22) * var8);
                        int var31 = var12 + (int) Math.round(var28 * var8);
                        int var32 =
                                (int)
                                        (125.0
                                                + 80.0
                                                        * (0.5
                                                                + 0.5
                                                                        * Math.sin(
                                                                                var14 / 1900.0
                                                                                        + var19)));
                        icon(
                                var0,
                                STAR,
                                var30 - 2,
                                var31 - 2,
                                Math.max(3, Math.round(6.0F * var8)),
                                var32);
                    }

                    for (int var47 = 0; var47 < 4; var47++) {
                        double var50 = (var14 / 850.0 + var47 * 6.5) % 27.0;
                        if (!(var50 > var13)) {
                            int var53 =
                                    var11
                                            + (int)
                                                    ((128.0
                                                                    + Math.sin(var50 * 0.81) * 4.0
                                                                    + Math.sin(var50 * 0.24) * 7.0)
                                                            * var8);
                            int var23 = var12 + (int) ((285.0 - 240.0 * var50 / 27.0) * var8);
                            var0.fill(var53, var23, var53 + 1, var23 + 2, -1177435929);
                        }
                    }
                }

                if (var7 && var13 >= 26.99) {
                    for (int var45 = 0; var45 < 8; var45++) {
                        double var48 = (var14 / 12000.0 + var45 * 0.137) % 1.0;
                        int var21 = var11 + (int) ((128.0 + Math.sin(var45 * 2.1) * 65.0) * var8);
                        int var54 = var12 + (int) ((240 - var45 * 17 - var48 * 30.0) * var8);
                        int var56 = (int) (130.0 * Math.sin(var48 * Math.PI));
                        var0.fill(var21, var54, var21 + 1, var54 + 1, var56 << 24 | 13747434);
                    }
                }

                if (var6 > 0.0F) {
                    int var46 = var11 + var9 / 2;
                    int var49 = var12 + (int) (45.0F * var8);
                    float var51 = (1.0F - var6) * Math.min(var3 * 0.42F, var4 * 0.31F);
                    int var52 = (int) (var6 * 160.0F) << 24 | 14146815;

                    for (int var55 = 0; var55 < 2; var55++) {
                        float var57 = var51 * (var55 == 0 ? 1.0F : 0.65F);

                        for (int var58 = 0; var58 < 32; var58++) {
                            double var25 = var58 * Math.PI * 2.0 / 32.0;
                            double var27 = (var58 + 1) * Math.PI * 2.0 / 32.0;
                            line(
                                    var0,
                                    var46 + (float) Math.cos(var25) * var57,
                                    var49 + (float) Math.sin(var25) * var57,
                                    var46 + (float) Math.cos(var27) * var57,
                                    var49 + (float) Math.sin(var27) * var57,
                                    1.0F,
                                    var52);
                        }
                    }
                }
            } finally {
                var0.disableScissor();
            }
        }
    }

    static {
        for (int var0 = 0; var0 < TREES.length; var0++) {
            TREES[var0] = id("tree/" + String.format(Locale.ROOT, "%02d", var0) + ".png");
        }
    }
}
