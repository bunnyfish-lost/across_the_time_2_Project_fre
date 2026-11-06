package fr.poubone.att2.client.shop;

import fr.poubone.att2.client.hud.HUDConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class MenuScale207 {
    private static final Map<Screen, MenuScale207.Metrics> METRICS = new WeakHashMap<>();

    private MenuScale207() {}

    public static MenuScale207.Metrics metrics(int var0, int var1, double var2, boolean var4) {
        float var5 = var4 ? 1.0F : (float) (2.0 / Math.max(1.0, var2));
        return new MenuScale207.Metrics(
                var5,
                var0,
                var1,
                Math.max(1, Math.round(var0 / var5)),
                Math.max(1, Math.round(var1 / var5)));
    }

    public static void prepare(Screen var0) {
        MenuScale207.Metrics var1 = METRICS.get(var0);
        int var2 = var0.width;
        int var3 = var0.height;
        if (var1 != null && var2 == var1.logicalWidth() && var3 == var1.logicalHeight()) {
            var2 = var1.guiWidth();
            var3 = var1.guiHeight();
        }

        Minecraft var4 = Minecraft.getInstance();
        double var5 =
                var4 != null && var4.getWindow() != null ? var4.getWindow().getGuiScale() : 2.0;
        MenuScale207.Metrics var7 = metrics(var2, var3, var5, HUDConfig.get().shopFollowGuiScale);
        METRICS.put(var0, var7);
        var0.width = var7.logicalWidth();
        var0.height = var7.logicalHeight();
    }

    public static void reset(Screen var0) {
        METRICS.remove(var0);
    }

    public static float factor(Screen var0) {
        MenuScale207.Metrics var1 = METRICS.get(var0);
        return var1 == null ? 1.0F : var1.scale();
    }

    public static int coordinate(Screen var0, int var1) {
        return (int) Math.floor(var1 / factor(var0));
    }

    public static double coordinate(Screen var0, double var1) {
        return var1 / factor(var0);
    }

    public static MouseButtonEvent event(Screen var0, MouseButtonEvent var1) {
        float var2 = factor(var0);
        return var2 == 1.0F
                ? var1
                : new MouseButtonEvent(var1.x() / var2, var1.y() / var2, var1.buttonInfo());
    }

    public static void begin(Screen var0, GuiGraphics var1) {
        var1.pose().pushMatrix();
        float var2 = factor(var0);
        var1.pose().scale(var2, var2);
    }

    public static void end(GuiGraphics var0) {
        var0.pose().popMatrix();
    }

    public static void tooltip(
            GuiGraphics var0,
            Font var1,
            List<Component> var2,
            Optional<TooltipComponent> var3,
            int var4,
            int var5,
            Identifier var6,
            Screen var7) {
        float var8 = factor(var7);
        var0.pose().pushMatrix();
        var0.pose().scale(1.0F / var8, 1.0F / var8);
        var0.setTooltipForNextFrame(
                var1, var2, var3, Math.round(var4 * var8), Math.round(var5 * var8), var6);
        var0.pose().popMatrix();
    }

    public static void tooltipSimple(
            GuiGraphics var0,
            Font var1,
            List<Component> var2,
            Optional<TooltipComponent> var3,
            int var4,
            int var5,
            Screen var6) {
        float var7 = factor(var6);
        var0.pose().pushMatrix();
        var0.pose().scale(1.0F / var7, 1.0F / var7);
        var0.setTooltipForNextFrame(
                var1, var2, var3, Math.round(var4 * var7), Math.round(var5 * var7));
        var0.pose().popMatrix();
    }

    public static void tooltipItem(
            GuiGraphics var0, Font var1, ItemStack var2, int var3, int var4, Screen var5) {
        float var6 = factor(var5);
        var0.pose().pushMatrix();
        var0.pose().scale(1.0F / var6, 1.0F / var6);
        var0.setTooltipForNextFrame(var1, var2, Math.round(var3 * var6), Math.round(var4 * var6));
        var0.pose().popMatrix();
    }

    public static void comparison(
            GuiGraphics var0,
            List<Component> var1,
            Player var2,
            ShopComparison.Kind var3,
            ItemStack var4,
            int var5,
            int var6,
            int var7,
            int var8,
            Screen var9) {
        float var10 = factor(var9);
        var0.pose().pushMatrix();
        var0.pose().scale(1.0F / var10, 1.0F / var10);
        ShopComparison.render(
                var0,
                var1,
                var2,
                var3,
                var4,
                Math.round(var5 * var10),
                Math.round(var6 * var10),
                Math.round(var7 * var10),
                Math.round(var8 * var10));
        var0.pose().popMatrix();
    }

    public static ShopViewport fit(int var0, int var1, int var2, int var3) {
        Minecraft var4 = Minecraft.getInstance();
        double var5 =
                var4 != null && var4.getWindow() != null ? var4.getWindow().getGuiScale() : 1.0;
        return ShopViewport.fit(
                var0, var1, var2, var3, 100, var5, HUDConfig.get().shopFollowGuiScale);
    }

    public record Metrics(
            float scale, int guiWidth, int guiHeight, int logicalWidth, int logicalHeight) {}
}
