package fr.poubone.att2.client.hud;

import fr.poubone.att2.client.data.MapStatBar;
import fr.poubone.att2.client.data.StatManager;
import fr.poubone.att2.client.util.ModTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;

public class StatIconsDisplay {
    private static final int[] LAST_VALUES = new int[9];
    private static boolean primed;

    public static void reset() {
        primed = false;
    }

    public static void render(GuiGraphics var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var1.player != null && var1.level != null) {
            Font var2 = var1.font;
            HUDConfig var3 = HUDConfig.get();
            HudLayout.Box var4 = HudLayout.box("stats");
            float var5 = HudLayout.contentScale("stats", 126, 40);
            float var6 = HudLayout.slot("stats").gap;
            StatIconsLayout var7 = StatIconsLayout.of(var5, var6, var2.width("-99"));
            int var8 = var7.iconSize();
            List<String> var9 = StatManager.STAT_KEYS.stream().filter(var3::isStatVisible).toList();
            if (!var9.isEmpty()) {
                boolean var10 = MapStatBar.sawDetailedValues();
                boolean var11 = MapStatBar.sawPointTotals();
                boolean var12 = var11 || var10;
                LinkedHashMap var13 = new LinkedHashMap();
                int var14 = var2.width("-99");
                if (var10) {
                    for (String var16 : var9) {
                        Component var17 = MapStatBar.detailedValue(var16);
                        Component var18 =
                                var17 == null ? Component.literal("...") : valueOnly(var17);
                        var13.put(var16, var18);
                        var14 = Math.max(var14, var2.width(var18));
                    }
                }

                int var32 = var7.textOffsetX() + var14;
                int var33 =
                        StatIconsLayout.columns(
                                var9.size(),
                                var4.w(),
                                var4.h(),
                                var32,
                                var7.spacingY(),
                                var3.statsLayoutMode);
                int var34 = (var9.size() + var33 - 1) / var33;
                int var35 =
                        var33 <= 1
                                ? var32
                                : Math.max(
                                        var32,
                                        Math.min(
                                                var7.spacingX(),
                                                Math.max(var32, (var4.w() - var32) / (var33 - 1))));
                int var19 =
                        var34 <= 1
                                ? var7.iconSize()
                                : Math.min(
                                        var7.spacingY(),
                                        Math.max(
                                                var7.iconSize(),
                                                (var4.h() - var7.iconSize()) / (var34 - 1)));
                int var20 = var4.x();
                int var21 = var4.y();

                for (int var22 = 0; var22 < var9.size(); var22++) {
                    String var23 = (String) var9.get(var22);
                    int var24 = StatManager.STAT_KEYS.indexOf(var23);
                    int var25 = StatManager.total(var23);
                    int var26 = var22 % var33;
                    int var27 = var22 / var33;
                    int var28 = var20 + var26 * var35;
                    int var29 = var21 + var27 * var19;
                    if (var11 && primed && var25 != LAST_VALUES[var24]) {
                        int var30 = var25 - LAST_VALUES[var24];
                        String var31 = (var30 > 0 ? "+" : "") + var30;
                        HudFx.floatingText(
                                var28 + var7.textOffsetX() + 4,
                                var29 - 2,
                                var31,
                                var30 > 0 ? 5635925 : 16733525);
                    }

                    LAST_VALUES[var24] = var25;
                    if (var23.equals("DAR")) {
                        int var36 = (int) (System.currentTimeMillis() / 100L % 32L);
                        var0.blit(
                                RenderPipelines.GUI_TEXTURED,
                                ModTextures.DAR_SPRITE,
                                var28,
                                var29,
                                0.0F,
                                var36 * 32.0F,
                                var8,
                                var8,
                                32,
                                32,
                                32,
                                1024);
                    } else {
                        Identifier var37 = ModTextures.STAT_ICONS.get(var23);
                        if (var37 != null) {
                            var0.blit(
                                    RenderPipelines.GUI_TEXTURED,
                                    var37,
                                    var28,
                                    var29,
                                    0.0F,
                                    0.0F,
                                    var8,
                                    var8,
                                    var8,
                                    var8);
                        }
                    }

                    Component var38 =
                            !var12
                                    ? Component.literal("...")
                                    : (var10
                                            ? (Component) var13.get(var23)
                                            : Component.literal(String.valueOf(var25)));
                    var0.drawString(
                            var2,
                            var38,
                            var28 + var7.textOffsetX(),
                            var29,
                            !var12 || (var10 ? !StatsSettings207.isNegative(var38) : var25 >= 0)
                                    ? -1
                                    : -171,
                            false);
                }

                primed = var11;
            }
        }
    }

    private static Component valueOnly(Component var0) {
        String var1 = var0.getString().replaceAll("§.", "").replace('：', ':');
        int var2 = var1.indexOf(58);
        if (var2 < 0) {
            return Component.literal("...");
        }

        var1 = var1.substring(var2 + 1);
        int var3 = var1.indexOf(124);
        if (var3 >= 0) {
            var1 = var1.substring(0, var3);
        }

        return Component.literal(var1.trim());
    }
}
