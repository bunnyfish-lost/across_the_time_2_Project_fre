package fr.poubone.att2.client.quest;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class QuestJournalArt {
    public static final int INK = -12966894;
    public static final int INK_SOFT = -9548244;
    public static final int INK_FAINT = -1771155924;
    public static final int RULE = -1433769412;
    public static final int RULE_FAINT = 1519020604;
    public static final int ROW_HOVER = 578440990;
    public static final int ROW_SELECTED = 1116494370;
    public static final int CRIMSON = -7463132;
    public static final int CREAM = -793924;
    public static final int GOLD = -3562150;
    public static final int GOLD_HI = -1124454;
    public static final int GOLD_LO = -8758750;
    public static final int GOLD_SOFT = -4679070;
    public static final int LEATHER = -13755378;
    public static final int LEATHER_HI = -11126756;
    public static final int LEATHER_LO = -15463419;
    private static final Identifier PAGE_RIGHT =
            Identifier.fromNamespaceAndPath("att2", "textures/quest_book/parchment_right.png");
    private static final Identifier PAGE_LEFT =
            Identifier.fromNamespaceAndPath("att2", "textures/quest_book/parchment_left.png");
    private static final Identifier ARROW_FORWARD =
            Identifier.withDefaultNamespace("widget/page_forward");
    private static final Identifier ARROW_FORWARD_HI =
            Identifier.withDefaultNamespace("widget/page_forward_highlighted");
    private static final Identifier ARROW_BACKWARD =
            Identifier.withDefaultNamespace("widget/page_backward");
    private static final Identifier ARROW_BACKWARD_HI =
            Identifier.withDefaultNamespace("widget/page_backward_highlighted");
    private static final int PAGE_W = 292;
    private static final int PAGE_H = 360;
    private static final int SRC_BORDER = 16;
    private static final int DST_BORDER = 8;

    private QuestJournalArt() {}

    public static int statusColor(QuestStatus var0) {
        return switch (var0) {
            case STARTED -> -5741553;
            case COMPLETED -> -12686554;
            case FAILED -> -6675432;
            case UNKNOWN -> -8754600;
        };
    }

    public static int ribbonColor(QuestType var0, boolean var1, boolean var2) {
        int var3 =
                switch (var0) {
                    case MAIN -> var1 ? -4704710 : -7463132;
                    case SIDE -> var1 ? -11764161 : -13673942;
                    case DAILY -> var1 ? -12752236 : -14399637;
                };
        if (!var1) {
            var3 = scale(var3, 0.8F);
        }

        return var2 ? lighten(var3, 26) : var3;
    }

    public static int scale(int var0, float var1) {
        int var2 = Math.min(255, (int) ((var0 >> 16 & 0xFF) * var1));
        int var3 = Math.min(255, (int) ((var0 >> 8 & 0xFF) * var1));
        int var4 = Math.min(255, (int) ((var0 & 0xFF) * var1));
        return var0 & 0xFF000000 | var2 << 16 | var3 << 8 | var4;
    }

    public static int lighten(int var0, int var1) {
        int var2 = Math.min(255, (var0 >> 16 & 0xFF) + var1);
        int var3 = Math.min(255, (var0 >> 8 & 0xFF) + var1);
        int var4 = Math.min(255, (var0 & 0xFF) + var1);
        return var0 & 0xFF000000 | var2 << 16 | var3 << 8 | var4;
    }

    public static int withAlpha(int var0, int var1) {
        return var1 << 24 | var0 & 16777215;
    }

    public static void hGradient(
            GuiGraphics var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        int var7 = var3 - var1;

        for (int var8 = 0; var8 < var7; var8++) {
            float var9 = var7 <= 1 ? 0.0F : (float) var8 / (var7 - 1);
            var0.fill(var1 + var8, var2, var1 + var8 + 1, var4, mix(var5, var6, var9));
        }
    }

    public static int mix(int var0, int var1, float var2) {
        int var3 = var0 >>> 24;
        int var4 = var0 >> 16 & 0xFF;
        int var5 = var0 >> 8 & 0xFF;
        int var6 = var0 & 0xFF;
        int var7 = var1 >>> 24;
        int var8 = var1 >> 16 & 0xFF;
        int var9 = var1 >> 8 & 0xFF;
        int var10 = var1 & 0xFF;
        return (int) (var3 + (var7 - var3) * var2) << 24
                | (int) (var4 + (var8 - var4) * var2) << 16
                | (int) (var5 + (var9 - var5) * var2) << 8
                | (int) (var6 + (var10 - var6) * var2);
    }

    public static void cover(GuiGraphics var0, int var1, int var2, int var3, int var4) {
        var0.fill(var1 + 3, var2 + 4, var1 + var3 + 3, var2 + var4 + 4, 1845493760);
        var0.fill(var1, var2, var1 + var3, var2 + var4, -15463419);
        var0.fill(var1 + 1, var2 + 1, var1 + var3 - 1, var2 + var4 - 1, -13755378);
        var0.fill(var1 + 1, var2 + 1, var1 + var3 - 1, var2 + 2, -11126756);
        var0.fill(var1 + 1, var2 + 1, var1 + 2, var2 + var4 - 1, -11126756);
        var0.fill(var1 + 1, var2 + var4 - 2, var1 + var3 - 1, var2 + var4 - 1, -15463419);

        for (int var5 = var1 + 6; var5 < var1 + var3 - 6; var5 += 4) {
            var0.fill(var5, var2 + 3, var5 + 2, var2 + 4, -9745882);
            var0.fill(var5, var2 + var4 - 4, var5 + 2, var2 + var4 - 3, -9745882);
        }

        corner(var0, var1, var2, 1, 1);
        corner(var0, var1 + var3 - 1, var2, -1, 1);
        corner(var0, var1, var2 + var4 - 1, 1, -1);
        corner(var0, var1 + var3 - 1, var2 + var4 - 1, -1, -1);
    }

    private static void corner(GuiGraphics var0, int var1, int var2, int var3, int var4) {
        for (int var5 = 0; var5 < 9; var5++) {
            for (int var6 = 0; var6 < 9 - var5; var6++) {
                int var7 =
                        var5 != 0 && var6 != 0
                                ? (var5 + var6 >= 7 ? -8758750 : -3562150)
                                : -1124454;
                int var8 = var1 + var3 * var5;
                int var9 = var2 + var4 * var6;
                var0.fill(var8, var9, var8 + 1, var9 + 1, var7);
            }
        }
    }

    public static void page(
            GuiGraphics var0, int var1, int var2, int var3, int var4, boolean var5) {
        var0.fill(var1, var2 + 2, var1 + var3, var2 + var4 + 2, -4680358);
        var0.fill(var1, var2 + 1, var1 + var3, var2 + var4 + 1, -2836868);
        byte var6 = 8;
        byte var7 = 16;
        int[] var8 = new int[] {var1, var1 + var6, var1 + var3 - var6};
        int[] var9 = new int[] {var6, var3 - 2 * var6, var6};
        int[] var10 = new int[] {var2, var2 + var6, var2 + var4 - var6};
        int[] var11 = new int[] {var6, var4 - 2 * var6, var6};
        int[] var12 = new int[] {0, var7, 292 - var7};
        int[] var13 = new int[] {var7, 292 - 2 * var7, var7};
        int[] var14 = new int[] {0, var7, 360 - var7};
        int[] var15 = new int[] {var7, 360 - 2 * var7, var7};

        for (int var16 = 0; var16 < 3; var16++) {
            for (int var17 = 0; var17 < 3; var17++) {
                if (var9[var17] > 0 && var11[var16] > 0) {
                    var0.blit(
                            RenderPipelines.GUI_TEXTURED,
                            var5 ? PAGE_LEFT : PAGE_RIGHT,
                            var8[var17],
                            var10[var16],
                            var12[var17],
                            var14[var16],
                            var9[var17],
                            var11[var16],
                            var13[var17],
                            var15[var16],
                            292,
                            360);
                }
            }
        }
    }

    public static void spine(GuiGraphics var0, int var1, int var2, int var3) {
        hGradient(var0, var1, var2, var1 + 5, var3, -14412278, -15857660);
        hGradient(var0, var1 + 5, var2, var1 + 10, var3, -15857660, -14412278);

        for (int var4 = var2 + 6; var4 < var3 - 6; var4 += 6) {
            var0.fill(var1 + 4, var4, var1 + 6, var4 + 3, -8758736);
        }
    }

    public static void gutterShadow(GuiGraphics var0, int var1, int var2, int var3, boolean var4) {
        if (var4) {
            hGradient(var0, var1 - 18, var2 + 6, var1, var2 + var3 - 6, 3941130, 1513890570);
        } else {
            hGradient(var0, var1, var2 + 6, var1 + 18, var2 + var3 - 6, 1513890570, 3941130);
        }
    }

    public static void ornamentRule(GuiGraphics var0, int var1, int var2, int var3, int var4) {
        int var5 = (var1 + var2) / 2;
        var0.fill(var1, var3, var5 - 5, var3 + 1, var4);
        var0.fill(var5 + 6, var3, var2, var3 + 1, var4);
        int[] var6 = new int[] {1, 3, 5, 3, 1};

        for (int var7 = 0; var7 < var6.length; var7++) {
            int var8 = var6[var7];
            var0.fill(
                    var5 - var8 / 2,
                    var3 - 2 + var7,
                    var5 - var8 / 2 + var8,
                    var3 - 1 + var7,
                    -9548244);
        }
    }

    public static void dottedRule(GuiGraphics var0, int var1, int var2, int var3, int var4) {
        for (int var5 = var1; var5 < var2; var5 += 3) {
            var0.fill(var5, var3, var5 + 1, var3 + 1, var4);
        }
    }

    public static void circle(GuiGraphics var0, int var1, int var2, int var3, int var4) {
        for (int var5 = -var3; var5 < var3; var5++) {
            float var6 = var5 + 0.5F;
            int var7 =
                    (int) Math.floor(Math.sqrt(Math.max(0.0F, var3 * var3 - var6 * var6)) + 0.15F);
            if (var7 > 0) {
                var0.fill(var1 - var7, var2 + var5, var1 + var7, var2 + var5 + 1, var4);
            }
        }
    }

    public static void seal(GuiGraphics var0, int var1, int var2, QuestStatus var3) {
        int var4 = statusColor(var3);
        circle(var0, var1, var2, 5, scale(var4, 0.65F));
        circle(var0, var1, var2, 4, var4);
        switch (var3) {
            case COMPLETED:
                int[][] var10 = new int[][] {{-2, 0}, {-1, 1}, {0, 0}, {1, -1}, {2, -2}};

                for (int[] var9 : var10) {
                    var0.fill(
                            var1 + var9[0],
                            var2 + var9[1],
                            var1 + var9[0] + 1,
                            var2 + var9[1] + 1,
                            -793924);
                }
                break;
            case FAILED:
                for (int var5 = -2; var5 <= 1; var5++) {
                    var0.fill(var1 + var5, var2 + var5, var1 + var5 + 1, var2 + var5 + 1, -793924);
                    var0.fill(var1 + var5, var2 - var5 - 1, var1 + var5 + 1, var2 - var5, -793924);
                }
                break;
            default:
                var0.fill(var1 - 1, var2 - 1, var1 + 1, var2 + 1, -922752066);
        }
    }

    public static void closeSeal(GuiGraphics var0, int var1, int var2, boolean var3) {
        circle(var0, var1 + 1, var2 + 2, 8, 1509949440);
        circle(var0, var1, var2, 8, -10874859);
        circle(var0, var1, var2, 7, var3 ? -5036498 : -7463132);
        circle(var0, var1 - 2, var2 - 2, 2, 687865855);

        for (int var4 = -3; var4 <= 2; var4++) {
            var0.fill(var1 + var4, var2 + var4, var1 + var4 + 1, var2 + var4 + 1, -793924);
            var0.fill(var1 + var4, var2 - var4 - 1, var1 + var4 + 1, var2 - var4, -793924);
        }
    }

    public static int ribbon(
            GuiGraphics var0,
            Font var1,
            int var2,
            int var3,
            int var4,
            QuestType var5,
            String var6,
            boolean var7,
            boolean var8) {
        int var9 = var7 ? 22 : 17;
        int var10 = ribbonColor(var5, var7, var8);
        var0.fill(var2, var3, var2 + var4, var3 + var9, var10);
        var0.fill(var2, var3, var2 + 1, var3 + var9, lighten(var10, 40));
        var0.fill(var2 + var4 - 1, var3, var2 + var4, var3 + var9, scale(var10, 0.6F));

        for (int var11 = var2 + 3; var11 < var2 + var4 - 3; var11 += 3) {
            var0.fill(var11, var3 + 2, var11 + 1, var3 + 3, var7 ? 1526722760 : 855634120);
        }

        int var12 = var7 ? -793924 : -1427713366;
        if (var1.width(var6) > var4 - 6) {
            var6 = var1.plainSubstrByWidth(var6, var4 - 6 - var1.width("…")) + "…";
        }

        var0.drawString(var1, var6, var2 + (var4 - var1.width(var6)) / 2, var3 + 6, var12, false);
        return var9;
    }

    public static void ribbonTail(
            GuiGraphics var0,
            int var1,
            int var2,
            int var3,
            int var4,
            QuestType var5,
            boolean var6,
            boolean var7) {
        int var8 = ribbonColor(var5, var6, var7);
        var0.fill(var1 + 1, var2, var1 + var3 - 1, var2 + var4, var8);
        int var9 = var1 + var3 / 2;

        for (int var10 = 0; var10 < 4; var10++) {
            var0.fill(
                    var9 - 4 + var10,
                    var2 + var4 - 4 + var10,
                    var9 + 4 - var10,
                    var2 + var4 - 3 + var10,
                    -13755378);
        }
    }

    public static int stamp(
            GuiGraphics var0, Font var1, int var2, int var3, String var4, int var5) {
        int var6 = var1.width(var4) + 10;
        int var7 = var2 - var6;
        var0.fill(var7, var3, var7 + var6, var3 + 14, withAlpha(var5, 26));
        var0.fill(var7, var3, var7 + var6, var3 + 1, var5);
        var0.fill(var7, var3 + 13, var7 + var6, var3 + 14, var5);
        var0.fill(var7, var3, var7 + 1, var3 + 14, var5);
        var0.fill(var7 + var6 - 1, var3, var7 + var6, var3 + 14, var5);
        var0.fill(var7 + 2, var3 + 2, var7 + var6 - 2, var3 + 3, withAlpha(var5, 90));
        var0.drawString(var1, var4, var7 + 5, var3 + 3, var5, false);
        return var6;
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
            boolean var8,
            boolean var9) {
        var0.fill(var2, var3 + 1, var2 + var4, var3 + var5 + 1, 1006632960);
        var0.fill(var2, var3, var2 + var4, var3 + var5, -15463419);
        int var10 = !var8 ? -11913948 : (var7 ? -9812956 : -11127012);
        var0.fill(var2 + 1, var3 + 1, var2 + var4 - 1, var3 + var5 - 1, var10);
        var0.fill(
                var2 + 1,
                var3 + 1,
                var2 + var4 - 1,
                var3 + 2,
                !var8 ? -10861518 : (var7 ? -7708618 : -9154516));
        int var11 = var9 ? 10 : 0;
        if (var1.width(var6) > var4 - 8 - var11) {
            var6 = var1.plainSubstrByWidth(var6, var4 - 8 - var11 - var1.width("…")) + "…";
        }

        int var12 = var1.width(var6);
        int var13 = var2 + (var4 - var12 - var11) / 2 + var11;
        int var14 = var3 + (var5 - 8) / 2;
        if (var9) {
            int var15 = var13 - 10;
            var0.fill(var15, var14, var15 + 7, var14 + 8, var8 ? -793924 : -6649232);
            var0.fill(var15 + 1, var14 + 1, var15 + 6, var14 + 7, var8 ? -7463132 : -9811392);
            var0.fill(var15 + 3, var14 + 1, var15 + 4, var14 + 7, -3562150);
        }

        var0.drawString(var1, var6, var13, var14, var8 ? -793924 : -6649232, false);
    }

    public static void checkbox(GuiGraphics var0, int var1, int var2, boolean var3, boolean var4) {
        var0.fill(var1, var2, var1 + 9, var2 + 9, var4 ? -12966894 : -9548244);
        var0.fill(var1 + 1, var2 + 1, var1 + 8, var2 + 8, -1322080);
        if (var3) {
            int var5 = -12686554;
            int[][] var6 = new int[][] {{2, 4}, {3, 5}, {4, 6}, {5, 5}, {6, 4}, {7, 3}, {7, 2}};

            for (int[] var10 : var6) {
                var0.fill(
                        var1 + var10[0],
                        var2 + var10[1],
                        var1 + var10[0] + 1,
                        var2 + var10[1] + 2,
                        var5);
            }
        }
    }

    public static void magnifier(GuiGraphics var0, int var1, int var2, int var3) {
        var0.fill(var1 + 1, var2, var1 + 5, var2 + 1, var3);
        var0.fill(var1 + 1, var2 + 6, var1 + 5, var2 + 7, var3);
        var0.fill(var1, var2 + 1, var1 + 1, var2 + 6, var3);
        var0.fill(var1 + 5, var2 + 1, var1 + 6, var2 + 6, var3);
        var0.fill(var1 + 5, var2 + 6, var1 + 7, var2 + 8, var3);
        var0.fill(var1 + 7, var2 + 8, var1 + 9, var2 + 10, var3);
    }

    public static void progressBar(GuiGraphics var0, int var1, int var2, int var3, float var4) {
        var0.fill(var1, var2, var1 + var3, var2 + 6, -9548244);
        var0.fill(var1 + 1, var2 + 1, var1 + var3 - 1, var2 + 5, -1783410);
        int var5 = (int) ((var3 - 2) * Math.max(0.0F, Math.min(1.0F, var4)));
        if (var5 > 0) {
            var0.fill(var1 + 1, var2 + 1, var1 + 1 + var5, var2 + 5, -5741553);
            var0.fill(var1 + 1, var2 + 1, var1 + 1 + var5, var2 + 2, -3110358);
        }
    }

    public static void pageArrow(GuiGraphics var0, int var1, int var2, boolean var3, boolean var4) {
        Identifier var5 =
                var3
                        ? (var4 ? ARROW_FORWARD_HI : ARROW_FORWARD)
                        : (var4 ? ARROW_BACKWARD_HI : ARROW_BACKWARD);
        var0.blitSprite(RenderPipelines.GUI_TEXTURED, var5, var1, var2, 23, 13);
    }
}
