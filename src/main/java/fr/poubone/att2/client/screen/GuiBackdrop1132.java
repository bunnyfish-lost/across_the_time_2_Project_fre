package fr.poubone.att2.client.screen;

import fr.poubone.att2.client.hud.HUDConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import org.joml.Matrix3x2fStack;

public final class GuiBackdrop1132 {
    private GuiBackdrop1132() {}

    public static boolean hide(Screen var0) {
        if (var0 != null && var0.getClass().getName().startsWith("fr.poubone.att2.client.")) {
            Minecraft var1 = Minecraft.getInstance();
            return var1 != null && var1.level != null && HUDConfig.get().disableGrayBackground;
        } else {
            return false;
        }
    }

    private static boolean darkTranslucent(int var0) {
        int var1 = var0 >>> 24;
        return var1 > 0
                && var1 < 255
                && (var0 >> 16 & 0xFF) <= 64
                && (var0 >> 8 & 0xFF) <= 64
                && (var0 & 0xFF) <= 64;
    }

    public static boolean scrim(Screen var0, int var1, int var2, int var3, int var4, int var5) {
        return var0 != null
                && var1 == 0
                && var2 == 0
                && var3 == var0.width
                && var4 == var0.height
                && darkTranslucent(var5)
                && hide(var0);
    }

    private static boolean rootSpace(GuiGraphics var0) {
        Matrix3x2fStack var1 = var0.pose();
        return Math.abs(var1.m20()) < 0.01F
                && Math.abs(var1.m21()) < 0.01F
                && Math.abs(var1.m01()) < 0.001F
                && Math.abs(var1.m10()) < 0.001F;
    }

    public static void fill(
            GuiGraphics var0, int var1, int var2, int var3, int var4, int var5, Screen var6) {
        if (!scrim(var6, var1, var2, var3, var4, var5) || !rootSpace(var0)) {
            var0.fill(var1, var2, var3, var4, var5);
        }
    }

    public static void gradient(
            GuiGraphics var0,
            int var1,
            int var2,
            int var3,
            int var4,
            int var5,
            int var6,
            Screen var7) {
        if (!scrim(var7, var1, var2, var3, var4, var5)
                || !darkTranslucent(var6)
                || !rootSpace(var0)) {
            var0.fillGradient(var1, var2, var3, var4, var5, var6);
        }
    }

    public static void blur(GuiGraphics var0, Screen var1) {
        if (!hide(var1)) {
            var0.blurBeforeThisStratum();
        }
    }
}
