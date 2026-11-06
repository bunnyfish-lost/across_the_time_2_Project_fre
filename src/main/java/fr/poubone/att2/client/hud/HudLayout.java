package fr.poubone.att2.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HudLayout {
    public static final String XP = "xp";
    public static final String CHRONOTON = "chronoton";
    public static final String MANA = "mana";
    public static final String STATS = "stats";
    public static final String ARROWS = "arrows";
    public static final String ARMOR = "armor";
    public static final String SPELLS = "spells";
    public static final String TEMPERATURE = "temperature";
    public static final String[] IDS =
            new String[] {
                "xp",
                "chronoton",
                "mana",
                "stats",
                "arrows",
                "armor",
                "spells",
                "temperature",
                "city",
                "recipe_tracking"
            };
    public static final float SCALE_MIN = 0.4F;
    public static final float SCALE_MAX = 3.0F;
    public static final float SCALE_STEP = 0.1F;
    public static final float GAP_MIN = 0.3F;
    public static final float GAP_MAX = 2.0F;
    public static final float GAP_STEP = 0.1F;
    private static final Map<String, HudSlot> DEFAULTS = new LinkedHashMap<>();

    private HudLayout() {}

    public static HudSlot slot(String var0) {
        HUDConfig var1 = HUDConfig.get();
        if (var1.slots == null) {
            var1.slots = new LinkedHashMap<>();
        }

        return var1.slots.computeIfAbsent(var0, var0x -> defaultSlot(var0x).copy());
    }

    public static HudSlot defaultSlot(String var0) {
        HudSlot var1 = DEFAULTS.get(var0);
        return var1 == null ? new HudSlot(0.4F, 0.4F, 0.15F, 0.08F) : var1;
    }

    public static void resetAll() {
        HUDConfig var0 = HUDConfig.get();
        if (var0.slots == null) {
            var0.slots = new LinkedHashMap<>();
        }

        var0.slots.clear();

        for (String var4 : IDS) {
            var0.slots.put(var4, defaultSlot(var4).copy());
        }
    }

    public static HudLayout.Box box(String var0) {
        Minecraft var1 = Minecraft.getInstance();
        int var2 = Math.max(1, var1.getWindow().getGuiScaledWidth());
        int var3 = Math.max(1, var1.getWindow().getGuiScaledHeight());
        return box(var0, var2, var3);
    }

    public static HudLayout.Box box(String var0, int var1, int var2) {
        return boxFromSlot(slot(var0), var1, var2);
    }

    public static HudLayout.Box boxFromSlot(HudSlot var0, int var1, int var2) {
        int var3 = Mth.clamp(Math.round(var0.w * var1), 24, var1);
        int var4 = Mth.clamp(Math.round(var0.h * var2), 14, var2);
        int var5 = Mth.clamp(Math.round(var0.x * var1), 0, Math.max(0, var1 - var3));
        int var6 = Mth.clamp(Math.round(var0.y * var2), 0, Math.max(0, var2 - var4));
        if (var5 + var3 > var1) {
            var5 = Math.max(0, var1 - var3);
        }

        if (var6 + var4 > var2) {
            var6 = Math.max(0, var2 - var4);
        }

        return new HudLayout.Box(var5, var6, var3, var4);
    }

    public static int snapToEdge(int var0, int var1, int var2, int var3) {
        int var4 = Mth.clamp(var0, var1, var2);
        if (var4 - var1 <= var3) {
            return var1;
        } else if (var2 - var4 <= var3) {
            return var2;
        } else {
            return var3 <= 0 ? var4 : Mth.clamp(var4 / var3 * var3, var1, var2);
        }
    }

    public static float fractionX(int var0, int var1, int var2) {
        return edgeFraction(var0, var1, var2);
    }

    public static float fractionY(int var0, int var1, int var2) {
        return edgeFraction(var0, var1, var2);
    }

    private static float edgeFraction(int var0, int var1, int var2) {
        if (var2 <= 0) {
            return 0.0F;
        }

        if (var0 <= 0) {
            return 0.0F;
        }

        int var3 = Math.max(0, var2 - Math.max(0, var1));
        return var3 > 0 && var0 >= var3 ? 1.0F : Mth.clamp((float) var0 / var2, 0.0F, 1.0F);
    }

    public static float contentScale(String var0, int var1, int var2) {
        float var3 = box(var0).fit(var1, var2);
        float var4 = slot(var0).scale;
        if (var4 <= 0.0F) {
            var4 = 1.0F;
        }

        var4 = Mth.clamp(var4, 0.4F, 3.0F);
        return var3 * var4;
    }

    public static void adjustScale(String var0, float var1) {
        HudSlot var2 = slot(var0);
        float var3 = var2.scale <= 0.0F ? 1.0F : var2.scale;
        float var4 = (var2.scale <= 0.0F ? 1.0F : var2.scale) + var1;
        var2.scale = Mth.clamp(var4, 0.4F, 3.0F);
        var2.scale = Math.round(var2.scale * 10.0F) / 10.0F;
        if ("recipe_tracking".equals(var0)) {
            float var5 = var2.scale / var3;
            var2.w = Mth.clamp(var2.w * var5, 0.03F, 1.0F);
            var2.h = Mth.clamp(var2.h * var5, 0.025F, 1.0F);
        }
    }

    public static void adjustGap(String var0, float var1) {
        HudSlot var2 = slot(var0);
        float var3 = (var2.gap <= 0.0F ? 1.0F : var2.gap) + var1;
        var2.gap = Mth.clamp(var3, 0.3F, 2.0F);
        var2.gap = Math.round(var2.gap * 10.0F) / 10.0F;
    }

    public static void writePixels(String var0, int var1, int var2, int var3, int var4) {
        Minecraft var5 = Minecraft.getInstance();
        int var6 = Math.max(1, var5.getWindow().getGuiScaledWidth());
        int var7 = Math.max(1, var5.getWindow().getGuiScaledHeight());
        HudSlot var8 = slot(var0);
        var8.x = fractionX(var1, var3, var6);
        var8.y = fractionY(var2, var4, var7);
        var8.w = Mth.clamp((float) var3 / var6, 0.03F, 1.0F);
        var8.h = Mth.clamp((float) var4 / var7, 0.025F, 1.0F);
    }

    public static String labelKey(String var0) {
        return "screen.hud_config.slot." + var0;
    }

    static {
        DEFAULTS.put("xp", new HudSlot(0.88056F, 0.00438F, 0.11944F, 0.03501F, 1.9F));
        DEFAULTS.put("chronoton", new HudSlot(0.93677F, 0.05689F, 0.06323F, 0.03939F, 2.6F));
        DEFAULTS.put("mana", new HudSlot(0.88525F, 0.7593F, 0.07494F, 0.15317F, 1.0F));
        DEFAULTS.put("stats", new HudSlot(0.01F, 0.86F, 0.18F, 0.11F, 1.1F));
        DEFAULTS.put("arrows", new HudSlot(0.28337F, 0.9453F, 0.09016F, 0.0547F, 0.8F));
        DEFAULTS.put("armor", new HudSlot(0.62295F, 0.92779F, 0.14052F, 0.06127F, 1.3F));
        DEFAULTS.put("temperature", new HudSlot(0.86F, 0.115F, 0.14F, 0.12F, 1.0F));
        DEFAULTS.put("spells", new HudSlot(0.0F, 0.24508F, 0.03747F, 0.0919F, 0.4F));
        DEFAULTS.put("recipe_tracking", new HudSlot(0.7F, 0.32F, 0.29F, 0.44F, 1.0F));
        DEFAULTS.put("city", new HudSlot(0.25F, 0.044F, 0.5F, 0.17F, 1.0F));
    }

    public record Box(int x, int y, int w, int h) {
        public float fit(int var1, int var2) {
            return var1 > 0 && var2 > 0
                    ? Mth.clamp(Math.min((float) this.w / var1, (float) this.h / var2), 0.4F, 3.0F)
                    : 1.0F;
        }
    }
}
