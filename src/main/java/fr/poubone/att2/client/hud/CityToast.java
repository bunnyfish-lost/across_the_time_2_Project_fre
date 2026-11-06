package fr.poubone.att2.client.hud;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.Set;

public final class CityToast {
    private static final Set<String> LEVEL_TITLES =
            Set.of(
                    "att2.leveling.title",
                    "att2.leveling.master.title",
                    "att2.leveling.eternan.title");
    private static final Set<String> LEVEL_SUBTITLES =
            Set.of("att2.leveling.master.subtitle", "att2.leveling.eternan.subtitle");
    private static String activeKey = "";
    private static String pendingKey = "";
    private static int activeKind;
    private static int pendingHint;
    private static Component pending;
    private static long clock;
    private static long pendingAt = -100L;
    private static boolean pendingConsumed;
    private static boolean activeOwned;
    private static int fadeIn = 10;
    private static int hold = 70;
    private static int fadeOut = 20;

    private CityToast() {}

    public static void reset() {
        resetContext();
        clock = 0L;
        fadeIn = 10;
        hold = 70;
        fadeOut = 20;
    }

    private static void resetContext() {
        activeKey = "";
        activeKind = 0;
        activeOwned = false;
        pending = null;
        pendingKey = "";
        pendingHint = 0;
        pendingAt = -100L;
        pendingConsumed = false;
    }

    public static void tick() {
        clock++;
        if (clock - pendingAt > 2L && (pendingHint != 0 || !HUDConfig.get().takeoverOtherTitles)) {
            pending = null;
            pendingConsumed = false;
        }
    }

    public static boolean isActive() {
        return ModToast.isActive();
    }

    public static void render(GuiGraphics var0) {}

    public static void clearTitles(boolean var0) {
        ModToast.clearScreenBanner();
        resetContext();
        if (var0) {
            fadeIn = 10;
            hold = 70;
            fadeOut = 20;
        }
    }

    public static void titleTimes(int var0, int var1, int var2) {
        if (var0 >= 0) {
            fadeIn = var0;
        }

        if (var1 >= 0) {
            hold = var1;
        }

        if (var2 >= 0) {
            fadeOut = var2;
        }

        ModToast.updateOtherTiming(fadeIn, hold, fadeOut);
    }

    private static boolean cityTitle(String var0) {
        return var0.startsWith("att2.citywalk.") && var0.endsWith(".title");
    }

    private static boolean citySubtitle(String var0) {
        return var0.startsWith("att2.citywalk.") && var0.endsWith(".subtitle");
    }

    private static String specialKey(Component var0) {
        if (var0 == null) {
            return "";
        }

        if (var0.getContents() instanceof TranslatableContents var1) {
            String var10 = var1.getKey();
            if (cityTitle(var10)
                    || citySubtitle(var10)
                    || LEVEL_TITLES.contains(var10)
                    || LEVEL_SUBTITLES.contains(var10)) {
                return var10;
            }

            for (Object var6 : var1.getArgs()) {
                if (var6 instanceof Component var7) {
                    String var8 = specialKey(var7);
                    if (!var8.isEmpty()) {
                        return var8;
                    }
                }
            }
        }

        for (Component var11 : var0.getSiblings()) {
            String var12 = specialKey(var11);
            if (!var12.isEmpty()) {
                return var12;
            }
        }

        return "";
    }

    private static String firstKey(Component var0) {
        if (var0 == null) {
            return "";
        } else if (var0.getContents() instanceof TranslatableContents var4) {
            return var4.getKey();
        } else {
            for (Component var5 : var0.getSiblings()) {
                String var3 = firstKey(var5);
                if (!var3.isEmpty()) {
                    return var3;
                }
            }

            return "";
        }
    }

    private static String key(Component var0) {
        String var1 = specialKey(var0);
        return var1.isEmpty() ? firstKey(var0) : var1;
    }

    private static int titleKind(String var0) {
        return cityTitle(var0) ? 1 : (LEVEL_TITLES.contains(var0) ? 2 : 3);
    }

    private static int subtitleHint(String var0) {
        return citySubtitle(var0) ? 1 : (LEVEL_SUBTITLES.contains(var0) ? 2 : 0);
    }

    private static boolean enabled(int var0) {
        HUDConfig var1 = HUDConfig.get();
        return var0 == 1
                ? var1.takeoverCityTitles
                : (var0 == 2 ? var1.takeoverLevelTitles : var0 == 3 && var1.takeoverOtherTitles);
    }

    private static Gui gui() {
        Minecraft var0 = Minecraft.getInstance();
        return var0 == null ? null : var0.gui;
    }

    private static String matchingSub(String var0) {
        return var0.endsWith(".title") ? var0.substring(0, var0.length() - 6) + ".subtitle" : "";
    }

    private static boolean matchesPending(String var0, int var1) {
        return pendingHint == 0 || pendingHint == var1 && pendingKey.equals(matchingSub(var0));
    }

    public static boolean consumeTitle(Component var0) {
        if (var0 != null && !ChatFormatting.stripFormatting(var0.getString()).isBlank()) {
            String var1 = key(var0);
            int var2 = titleKind(var1);
            boolean var3 = enabled(var2);
            Component var4 = null;
            boolean var5 =
                    pending != null
                            && (var2 == 3 || clock - pendingAt <= 2L)
                            && matchesPending(var1, var2);
            if (var5) {
                var4 = pending;
            }

            if (!var3) {
                if (var5 && pendingConsumed) {
                    Gui var7 = gui();
                    if (var7 != null) {
                        var7.setSubtitle(var4);
                    }
                }

                ModToast.clearScreenBanner();
                activeKey = var1;
                activeKind = var2;
                activeOwned = false;
                pending = null;
                pendingConsumed = false;
                return false;
            } else {
                if (var4 == null
                        && (var2 == 1 || var2 == 2 && !var1.equals("att2.leveling.title"))) {
                    var4 = Component.translatable(matchingSub(var1));
                }

                if (var2 == 1) {
                    ModToast.showCityBanner(var0, var4);
                } else if (var2 == 2) {
                    ModToast.showLevelBanner(var0, var4);
                } else {
                    ModToast.showOtherBanner(var0, var4, fadeIn, hold, fadeOut);
                }

                activeKey = var1;
                activeKind = var2;
                activeOwned = true;
                if (var2 != 3) {
                    pending = null;
                    pendingConsumed = false;
                }

                Gui var6 = gui();
                if (var6 != null) {
                    var6.clearTitles();
                }

                return true;
            }
        } else {
            clearTitles(false);
            return false;
        }
    }

    public static boolean consumeSubtitle(Component var0) {
        String var1 = key(var0);
        int var2 = subtitleHint(var1);
        pending = var0;
        pendingKey = var1;
        pendingHint = var2;
        pendingAt = clock;
        pendingConsumed = false;
        int var3 = var2 != 0 ? var2 : (activeKind != 0 ? activeKind : 3);
        boolean var4 = enabled(var3);
        if (!var4) {
            return false;
        }

        pendingConsumed = true;
        boolean var5 = var2 == 0 || var2 == activeKind && var1.equals(matchingSub(activeKey));
        if (var5 && activeOwned && enabled(activeKind) && ModToast.ownedScreenBanner()) {
            ModToast.setScreenSubtitle(var0);
        }

        return true;
    }
}
