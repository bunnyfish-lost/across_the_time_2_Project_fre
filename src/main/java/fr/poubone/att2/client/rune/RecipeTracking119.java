package fr.poubone.att2.client.rune;

import com.mojang.blaze3d.platform.InputConstants.Type;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.hud.HudLayout;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.AllowGame;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RecipeTracking119 {
    public static final String HUD_ID = "recipe_tracking";
    public static final long FLASH_PERIOD_NANOS = 3000000000L;
    private static RuneWorkshopRecipes119.Recipe tracked;
    private static List<RuneWorkshopRecipes119.Row> rows = List.of();
    private static Object world;
    private static long epoch;
    private static int[] stock = new int[0];
    private static int[] pouchStock = new int[0];
    private static int tickCount;
    private static boolean initialized;
    private static KeyMapping cancelKey;

    private RecipeTracking119() {}

    public static void initialize(Category var0) {
        if (!initialized) {
            initialized = true;
            cancelKey =
                    KeyBindingHelper.registerKeyBinding(
                            new KeyMapping(
                                    "key.att2.cancel_recipe_tracking", Type.KEYSYM, -1, var0));
            ClientTickEvents.END_CLIENT_TICK.register(
                    (EndTick)
                            var0x -> {
                                tick(var0x);
                                RuneWordCraft119.tick(var0x);
                            });
            ClientReceiveMessageEvents.ALLOW_GAME.register(
                    (AllowGame) (var0x, var1) -> RuneWordCraft119.onMessage(var0x, var1));
        }
    }

    public static KeyMapping cancelKey() {
        return cancelKey;
    }

    public static RuneWorkshopRecipes119.Recipe tracked() {
        return tracked;
    }

    public static boolean isTracked(String var0) {
        return tracked != null && tracked.key().equals(var0);
    }

    public static boolean track(RuneWorkshopRecipes119.Recipe var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var0 != null
                && var0.trackable()
                && var1 != null
                && var1.player != null
                && var1.level != null
                && !FlashbackCompat.isInReplay()) {
            tracked = var0;
            rows = var0.rows();
            world = var1.level;
            epoch = System.nanoTime();
            stock = new int[rows.size()];
            pouchStock = new int[rows.size()];
            tickCount = 0;
            refresh(var1.player);
            return true;
        } else {
            return false;
        }
    }

    public static void clear() {
        tracked = null;
        rows = List.of();
        world = null;
        stock = new int[0];
        pouchStock = new int[0];
    }

    public static boolean onKey(KeyEvent var0) {
        if (tracked != null && cancelKey != null && cancelKey.matches(var0)) {
            clear();
            return true;
        } else {
            return false;
        }
    }

    public static void tick(Minecraft var0) {
        if (tracked != null) {
            if (var0 == null
                    || var0.player == null
                    || var0.level == null
                    || var0.level != world
                    || FlashbackCompat.isInReplay()) {
                clear();
            } else if (cancelKey != null && cancelKey.consumeClick()) {
                clear();
            } else {
                if (++tickCount % 5 == 0) {
                    refresh(var0.player);
                }
            }
        }
    }

    private static void refresh(LocalPlayer var0) {
        for (int var1 = 0; var1 < rows.size(); var1++) {
            stock[var1] = rows.get(var1).material().owned(var0);
            pouchStock[var1] = RunePouchStock1113.count(rows.get(var1).material(), var0);
        }
    }

    public static List<Integer> orders(ItemStack var0) {
        if (tracked != null && var0 != null && !var0.isEmpty()) {
            ArrayList var1 = new ArrayList();

            for (RuneWorkshopRecipes119.Row var3 : rows) {
                if (var3.material().matches(var0)) {
                    var1.addAll(var3.orders());
                }
            }

            Collections.sort(var1);
            return List.copyOf(var1);
        } else {
            return List.of();
        }
    }

    public static int orderAt(List<Integer> var0, long var1) {
        return var0.isEmpty()
                ? 0
                : (Integer) var0.get((int) (Math.max(0L, var1) / 3000000000L % var0.size()));
    }

    public static float pulse(long var0) {
        double var2 = Math.max(0L, var0) % 3000000000L / 3.0E9;
        return (float) (var2 < 0.75 ? 0.08 + 0.92 * Math.sin(Math.PI * var2 / 0.75) : 0.08);
    }

    public static void renderSlot(GuiGraphics var0, Slot var1) {
        if (tracked != null && var1 != null && !FlashbackCompat.isInReplay()) {
            List var2 = orders(var1.getItem());
            if (!var2.isEmpty()) {
                Minecraft var3 = Minecraft.getInstance();
                if (var3 != null && var3.font != null) {
                    long var4 = System.nanoTime() - epoch;
                    int var6 = Math.max(0, Math.min(255, Math.round(255.0F * pulse(var4))));
                    int var7 = var6 << 24 | 14862462;
                    int var8 = var1.x;
                    int var9 = var1.y;
                    var0.fill(var8 - 1, var9 - 1, var8 + 17, var9, var7);
                    var0.fill(var8 - 1, var9 + 16, var8 + 17, var9 + 17, var7);
                    var0.fill(var8 - 1, var9, var8, var9 + 16, var7);
                    var0.fill(var8 + 16, var9, var8 + 17, var9 + 16, var7);
                    String var10 = Integer.toString(orderAt(var2, var4));
                    int var11 = var3.font.width(var10);
                    int var12 = var8 + 8 - var11 / 2;
                    int var13 = var9 + 4;
                    var0.fill(
                            var12 - 2,
                            var13 - 1,
                            var12 + var11 + 2,
                            var13 + 10,
                            Math.round(var6 * 0.85F) << 24 | 1381133);
                    var0.drawString(var3.font, var10, var12, var13, var7, true);
                }
            }
        }
    }

    public static ItemStack materialIcon(RuneWorkshopRecipes119.Material var0, LocalPlayer var1) {
        if (var1 != null) {
            for (int var2 = 0; var2 < var1.getInventory().getContainerSize(); var2++) {
                ItemStack var3 = var1.getInventory().getItem(var2);
                if (var0.matches(var3)) {
                    ItemStack var4 = var3.copy();
                    var4.setCount(1);
                    return var4;
                }
            }
        }

        return var0.icon();
    }

    public static void render(GuiGraphics var0, boolean var1) {
        Minecraft var2 = Minecraft.getInstance();
        if (var2 != null
                && var2.player != null
                && var2.level != null
                && var2.font != null
                && !FlashbackCompat.isInReplay()) {
            RuneWorkshopRecipes119.Recipe var3 = tracked;
            if (var3 == null) {
                if (!var1) {
                    return;
                }

                var3 =
                        RuneWorkshopRecipes119.category(1).stream()
                                .filter(var0x -> var0x.key().equals("w:16"))
                                .findFirst()
                                .orElse(null);
            }

            if (var3 != null) {
                List<RuneWorkshopRecipes119.Row> var4 = var3 == tracked ? rows : var3.rows();
                Font var5 = var2.font;
                short var6 = 180;
                int var7 = 48 + var4.size() * 21;
                HudLayout.Box var8 = HudLayout.box("recipe_tracking");
                float var9 = HudLayout.slot("recipe_tracking").scale;
                float var10 = Math.min((float) var8.w() / var6, (float) var8.h() / var7);
                float var11 = Math.max(0.05F, var10);
                var0.pose().pushMatrix();
                var0.pose().translate(var8.x(), var8.y());
                var0.pose().scale(var11, var11);
                var0.fill(0, 0, var6, var7, -803924975);
                var0.fill(0, 0, 2, var7, -7769274);
                boolean var12 =
                        var4.stream()
                                .anyMatch(var0x -> RunePouchStock1113.isRune(var0x.material()));
                byte var13 = 96;
                byte var14 = 30;
                if (var12) {
                    RunePouchArt1115.render(var0, var13 + (var14 - 16) / 2, 7, 16);
                }

                var0.renderItem(RuneItemDetails.resolve(var3.icon()), 7, 7);
                var0.drawString(
                        var5,
                        Component.literal(
                                        var5.plainSubstrByWidth(
                                                var3.name().getString(),
                                                var12 ? var13 - 35 : var6 - 37))
                                .setStyle(var3.name().getStyle()),
                        29,
                        10,
                        -1914754,
                        true);
                boolean var15 = true;

                for (int var16 = 0; var16 < var4.size(); var16++) {
                    RuneWorkshopRecipes119.Row var17 = (RuneWorkshopRecipes119.Row) var4.get(var16);
                    int var18 =
                            var3 == tracked && stock.length > var16
                                    ? stock[var16]
                                    : var17.material().owned(var2.player);
                    int var19 = 29 + var16 * 21;
                    ItemStack var20 = materialIcon(var17.material(), var2.player);
                    var0.renderItem(var20, 7, var19);
                    String var21 = var17.material().name().getString();
                    String var22 = var17.orders().toString().replace("[", "").replace("]", "");
                    String var23 = var5.plainSubstrByWidth(var21, var12 ? var13 - 35 : 87);
                    var0.drawString(var5, var23, 29, var19 + 2, -1514797, true);
                    var0.drawString(var5, var22, 29, var19 + 11, -5002865, false);
                    String var24 = var18 + " / " + var17.needed();
                    boolean var25 = var18 >= var17.needed();
                    var15 &= var25;
                    if (var12) {
                        int var26 =
                                var3 == tracked && pouchStock.length > var16
                                        ? pouchStock[var16]
                                        : RunePouchStock1113.count(var17.material(), var2.player);
                        if (var26 >= 0) {
                            RuneMaterialLayout1113.number(
                                    var0,
                                    var5,
                                    Integer.toString(var26),
                                    var13,
                                    var19 + 6,
                                    var14,
                                    var26 > 0 ? -1514797 : -5593704,
                                    true,
                                    true);
                        }

                        RuneMaterialLayout1113.number(
                                var0,
                                var5,
                                var24,
                                var13 + var14 + 6,
                                var19 + 6,
                                var6 - 8 - (var13 + var14 + 6),
                                var25 ? -5650281 : -1065342,
                                false,
                                true);
                    } else {
                        var0.drawString(
                                var5,
                                var24,
                                var6 - 8 - var5.width(var24),
                                var19 + 6,
                                var25 ? -5650281 : -1065342,
                                true);
                    }
                }

                var0.drawString(
                        var5,
                        var1 && tracked == null ? "配方追踪 · 预览" : (var15 ? "材料已齐 · 继续追踪" : "准备材料中"),
                        7,
                        var7 - 10,
                        var15 ? -5650281 : -5593704,
                        false);
                var0.pose().popMatrix();
            }
        }
    }
}
