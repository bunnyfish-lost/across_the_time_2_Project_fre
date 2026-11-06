package fr.poubone.att2.client.spell;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.HashMap;
import java.util.Map;

public final class SpellWheelLevelColors1111 {
    private static final Map<Integer, int[]> LEVEL_COLORS = new HashMap<>();
    public static final int UNKNOWN = 10392713;

    private SpellWheelLevelColors1111() {}

    public static int maxLevel(int var0) {
        int[] var1 = LEVEL_COLORS.get(var0);
        return var1 == null ? 0 : var1.length;
    }

    public static int mapColor(int var0, int var1) {
        int[] var2 = LEVEL_COLORS.get(var0);
        return var2 != null && var1 >= 1 && var1 <= var2.length ? var2[var1 - 1] : 10392713;
    }

    public static int color(SpellWheelCatalog.Owned var0) {
        if (var0 == null) {
            return 10392713;
        }

        int var1 = var0.state().level;
        Component var2 = var0.state().stack.get(DataComponents.CUSTOM_NAME);
        Integer var3 = levelColor(var2, var1);
        return var3 == null ? mapColor(var0.spellId(), var1) : var3;
    }

    private static Integer levelColor(Component var0, int var1) {
        if (var0 != null && var1 >= 1) {
            String var2 = ChatFormatting.stripFormatting(var0.getString()).strip();
            if (var2.equalsIgnoreCase("lvl" + var1)) {
                TextColor var3 = var0.getStyle().getColor();
                if (var3 != null) {
                    return var3.getValue();
                }

                String var4 = var0.getString();
                int var5 = -1;
                String var6 = "0123456789abcdef";
                int[] var7 =
                        new int[] {
                            0, 170, 43520, 43690, 11141120, 11141290, 16755200, 11184810, 5592405,
                            5592575, 5635925, 5636095, 16733525, 16733695, 16777045, 16777215
                        };

                for (int var8 = 0; var8 + 1 < var4.length(); var8++) {
                    if (var4.charAt(var8) == 167) {
                        int var9 = var6.indexOf(Character.toLowerCase(var4.charAt(var8 + 1)));
                        if (var9 >= 0) {
                            var5 = var7[var9];
                        }
                    }
                }

                if (var5 >= 0) {
                    return var5;
                }
            }

            if (var0.getContents() instanceof TranslatableContents var10) {
                for (Object var19 : var10.getArgs()) {
                    if (var19 instanceof Component var12) {
                        Integer var20 = levelColor(var12, var1);
                        if (var20 != null) {
                            return var20;
                        }
                    }
                }
            }

            for (Component var13 : var0.getSiblings()) {
                Integer var16 = levelColor(var13, var1);
                if (var16 != null) {
                    return var16;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public static int borderColor(SpellWheelCatalog.Owned var0, boolean var1) {
        return var0 == null ? (var1 ? -8357520 : -11450818) : 0xFF000000 | color(var0);
    }

    static {
        LEVEL_COLORS.put(
                1,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                2,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                3,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                4,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                5,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                6,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                7,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                8,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                9,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                10,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                11,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(21, new int[] {8434560, 10882559, 12533760});
        LEVEL_COLORS.put(22, new int[] {8434560, 10882559, 12533760});
        LEVEL_COLORS.put(23, new int[] {8434560, 10882559, 12533760});
        LEVEL_COLORS.put(
                24,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                25,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                26,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                27,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                28,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                29,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                30,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                31,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                32,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                33,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                34,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                35,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                40,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                41,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                42,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                43,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                44,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                45,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
        LEVEL_COLORS.put(
                46,
                new int[] {
                    8421504, 8434560, 4233536, 4227200, 33023, 10911999, 10882559, 7536780,
                    16741153, 12533760
                });
    }
}
