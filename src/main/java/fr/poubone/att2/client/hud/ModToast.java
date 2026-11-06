package fr.poubone.att2.client.hud;

import fr.poubone.att2.client.quest.QuestBookSkin;
import fr.poubone.att2.client.quest.QuestStatus;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import org.joml.Matrix3x2fStack;

public final class ModToast {
    private static final int FADE_IN = 4;
    private static final int HOLD = 70;
    private static final int FADE_OUT = 12;
    private static final int LIFE = 86;
    private static Component title;
    private static Component subtitle;
    private static ModToast.Kind kind = ModToast.Kind.INFO;
    private static int ticks = -1;
    private static boolean att2$previewCity;
    private static int screenKind;
    private static int life = 86;
    private static int visibleFrom = 4;
    private static int visibleUntil = 74;

    private static boolean screenBanner() {
        return screenKind != 0 || subtitle != null;
    }

    static boolean screenEnabled() {
        return att2$previewCity
                || (screenKind == 3
                        ? HUDConfig.get().takeoverOtherTitles
                        : (screenKind == 2
                                ? HUDConfig.get().takeoverLevelTitles
                                : (screenKind == 1
                                        ? HUDConfig.get().takeoverCityTitles
                                        : subtitle == null || HUDConfig.get().takeoverCityTitles)));
    }

    private ModToast() {}

    public static void reset() {
        title = null;
        subtitle = null;
        kind = ModToast.Kind.INFO;
        ticks = -1;
        screenKind = 0;
    }

    public static void showError(Component var0) {
        show(var0, null, ModToast.Kind.ERROR);
    }

    public static void showOk(Component var0) {
        show(var0, null, ModToast.Kind.OK);
    }

    public static void showInfo(Component var0) {
        show(var0, null, ModToast.Kind.INFO);
    }

    public static boolean ownedScreenBanner() {
        return screenKind != 0 && isActive();
    }

    public static void clearScreenBanner() {
        if (screenKind != 0) {
            reset();
        }
    }

    public static void showOtherBanner(
            Component var0, Component var1, int var2, int var3, int var4) {
        show(var0, var1, ModToast.Kind.INFO);
        screenKind = 3;
        updateOtherTiming(var2, var3, var4);
    }

    public static void updateOtherTiming(int var0, int var1, int var2) {
        if (screenKind == 3) {
            visibleFrom = Math.max(0, var0);
            visibleUntil = (int) Math.min(2147483647L, (long) visibleFrom + Math.max(0, var1));
            life = (int) Math.min(2147483647L, (long) visibleUntil + Math.max(0, var2));
            life = Math.max(1, life);
            visibleFrom = 0;
            visibleUntil = life;
        }
    }

    public static void showCityBanner(Component var0, Component var1) {
        show(var0, var1, ModToast.Kind.INFO);
        screenKind = 1;
    }

    public static void showLevelBanner(Component var0, Component var1) {
        show(var0, var1, ModToast.Kind.INFO);
        screenKind = 2;
    }

    public static void setScreenSubtitle(Component var0) {
        if (screenKind != 0 && isActive()) {
            subtitle = var0;
        }
    }

    public static void showBanner(Component var0, Component var1) {
        show(var0, var1, ModToast.Kind.INFO);
    }

    public static void show(Component var0, Component var1, ModToast.Kind var2) {
        if (var0 != null) {
            screenKind = 0;
            life = 86;
            visibleFrom = 4;
            visibleUntil = 74;
            title = var0;
            subtitle = var1;
            kind = var2 == null ? ModToast.Kind.INFO : var2;
            ticks = 0;
        }
    }

    public static void tick() {
        if (ticks >= 0 && ++ticks >= life) {
            reset();
        }
    }

    public static boolean isActive() {
        return ticks >= 0 && title != null;
    }

    public static void render(GuiGraphics var0) {
        if (screenEnabled() && isActive() && visible()) {
            Minecraft var1 = Minecraft.getInstance();
            Font var2 = var1.font;
            String var3 = title.getString();
            String var4 = subtitle == null ? "" : subtitle.getString();
            int var5 = var2.width(var3);
            int var6 = var4.isEmpty() ? 0 : var2.width(var4);
            int var7 = Math.max(var5, var6);
            byte var8 = 18;
            int var9 =
                    att2$cityWidth(
                            Math.min(
                                    var1.getWindow().getGuiScaledWidth() - 24,
                                    Math.max(160, var7 + var8 * 2)));
            int var10 = att2$cityHeight(var4.isEmpty() ? 28 : 40);
            int var11 = att2$cityX((var1.getWindow().getGuiScaledWidth() - var9) / 2);
            int var12 = att2$cityY(12);
            att2$fillMaybe(
                    var0, var11 + 3, var12 + 4, var11 + var9 + 3, var12 + var10 + 4, 1342177280);
            att2$panelMaybe(var0, "cover", var11, var12, var9, var10, 100, 100, 6);
            byte var13 = 4;
            att2$panelMaybe(
                    var0,
                    "page",
                    var11 + var13,
                    var12 + var13,
                    var9 - 2 * var13,
                    var10 - 2 * var13,
                    100,
                    100,
                    4);

            int var14 =
                    switch (kind) {
                        case ERROR -> QuestBookSkin.statusColor(QuestStatus.FAILED);
                        case OK -> QuestBookSkin.statusColor(QuestStatus.COMPLETED);
                        case INFO -> -4216453;
                    };
            att2$fillMaybe(
                    var0,
                    var11 + var13 + 2,
                    var12 + var13 + 2,
                    var11 + var9 - var13 - 2,
                    var12 + var13 + 4,
                    var14);
            int var15 = -13096159;
            int var16 = var11 + (var9 - var5) / 2;
            int var17 = var4.isEmpty() ? var12 + (var10 - 8) / 2 : var12 + 9;
            att2$drawCityText(var0, var2, var3, var16, var17, var15, false);
            if (!var4.isEmpty()) {
                att2$drawCityText(
                        var0, var2, var4, var11 + (var9 - var6) / 2, var12 + 22, -9413049, false);
            }
        }
    }

    private static boolean visible() {
        return ticks < 0 ? false : ticks >= visibleFrom && ticks < visibleUntil;
    }

    private static int att2$cityWidth(int var0) {
        return !screenBanner() ? var0 : HudLayout.box("city").w();
    }

    private static int att2$cityHeight(int var0) {
        return !screenBanner() ? var0 : HudLayout.box("city").h();
    }

    private static int att2$cityX(int var0) {
        return !screenBanner() ? var0 : HudLayout.box("city").x();
    }

    private static int att2$cityY(int var0) {
        return !screenBanner() ? var0 : HudLayout.box("city").y();
    }

    private static void att2$drawCityText(
            GuiGraphics var0, Font var1, String var2, int var3, int var4, int var5, boolean var6) {
        if (screenKind == 3) {
            HudLayout.Box var12 = HudLayout.box("city");
            Component var13 =
                    HUDConfig.get().cityUseOriginalTextColors
                            ? att2$originalTextComponent(var5)
                            : Component.literal(var2);
            int var14 = Math.max(1, var1.width(var13));
            float var15 =
                    Math.min(
                            HudLayout.slot("city").scale,
                            (float) Math.max(1, var12.w() - 32) / var14);
            Matrix3x2fStack var16 = var0.pose();
            var16.pushMatrix();
            var16.translate(var12.x() + var12.w() / 2.0F, var4);
            var16.scale(var15, var15);
            var0.drawString(
                    var1,
                    var13,
                    -var14 / 2,
                    0,
                    HUDConfig.get().cityUseOriginalTextColors ? -1 : var5,
                    var6);
            var16.popMatrix();
        } else {
            if (!screenBanner()) {
                att2$drawColorText(var0, var1, var2, var3, var4, var5, var6);
            } else {
                float var7 = HudLayout.slot("city").scale;
                HudLayout.Box var8 = HudLayout.box("city");
                float var9 = var8.x() + var8.w() / 2;
                float var10 = var8.y() + var8.h() / 2;
                Matrix3x2fStack var11 = var0.pose();
                var11.pushMatrix();
                var11.translate(var9, var10);
                var11.scale(var7, var7);
                var11.translate(-var9, -var10);
                att2$drawColorText(var0, var1, var2, var3, var4, var5, var6);
                var11.popMatrix();
            }
        }
    }

    private static boolean att2$isTransparent() {
        return !screenBanner() ? false : HUDConfig.get().cityTransparentBackground;
    }

    private static void att2$fillMaybe(
            GuiGraphics var0, int var1, int var2, int var3, int var4, int var5) {
        if (!att2$isTransparent()) {
            var0.fill(var1, var2, var3, var4, var5);
        }
    }

    private static void att2$panelMaybe(
            GuiGraphics var0,
            String var1,
            int var2,
            int var3,
            int var4,
            int var5,
            int var6,
            int var7,
            int var8) {
        if (!att2$isTransparent()) {
            QuestBookSkin.panel(var0, var1, var2, var3, var4, var5, var6, var7, var8);
        }
    }

    private static void att2$drawColorText(
            GuiGraphics var0, Font var1, String var2, int var3, int var4, int var5, boolean var6) {
        if (screenBanner() && HUDConfig.get().cityUseOriginalTextColors) {
            var0.drawString(var1, att2$originalTextComponent(var5), var3, var4, -1, var6);
        } else {
            var0.drawString(var1, var2, var3, var4, var5, var6);
        }
    }

    public static void renderCityPreview(GuiGraphics var0) {
        Component var1 = title;
        Component var2 = subtitle;
        ModToast.Kind var3 = kind;
        int var4 = ticks;
        int var5 = screenKind;
        int var6 = life;
        int var7 = visibleFrom;
        int var8 = visibleUntil;
        screenKind = 1;
        life = 86;
        visibleFrom = 4;
        visibleUntil = 74;
        title = ModLanguageManager.get("screen.hud_layout.city_preview_title");
        subtitle = ModLanguageManager.get("screen.hud_layout.city_preview_subtitle");
        kind = ModToast.Kind.INFO;
        ticks = 10;
        att2$previewCity = true;
        render(var0);
        title = var1;
        subtitle = var2;
        kind = var3;
        ticks = var4;
        screenKind = var5;
        life = var6;
        visibleFrom = var7;
        visibleUntil = var8;
        att2$previewCity = false;
    }

    private static Component att2$originalTextComponent(int var0) {
        return var0 == -13096159 ? title : subtitle;
    }

    public static void setCitySubtitle(Component var0) {
        if (subtitle != null && isActive()) {
            subtitle = var0;
        }
    }

    public enum Kind {
        ERROR,
        OK,
        INFO;
    }
}
