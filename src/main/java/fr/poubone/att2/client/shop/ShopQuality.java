package fr.poubone.att2.client.shop;

import fr.poubone.att2.client.renderer.ItemRarity;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class ShopQuality {
    private static final List<String> ORDER =
            List.of(
                    "com",
                    "unc",
                    "rar",
                    "epi_esc",
                    "epi",
                    "epi_set",
                    "leg",
                    "leg_armset",
                    "ult",
                    "myt",
                    "spe",
                    "que",
                    "misc",
                    "cur",
                    "esc",
                    "unk");
    private static final String[] TOOL_TIERS =
            new String[] {"com", "unc", "rar", "epi_esc", "epi", "leg", "ult"};

    private ShopQuality() {}

    public static String id(ShopOffer var0) {
        if (var0 == null) {
            return "unk";
        } else {
            String var1 = var0.rarityId();
            if (ItemRarity.fromId(var1) != null) {
                return var1;
            } else {
                return ShopSmithData.isTool(var0) ? TOOL_TIERS[var0.trigger() - 406] : "unk";
            }
        }
    }

    public static List<String> available(List<ShopOffer> var0, String var1) {
        HashSet var2 = new HashSet();

        for (ShopOffer var4 : var0) {
            var2.add(id(var4));
        }

        if (var1 != null && !var1.isEmpty()) {
            var2.add(var1);
        }

        ArrayList var6 = new ArrayList();
        var6.add("");

        for (String var5 : ORDER) {
            if (var2.contains(var5)) {
                var6.add(var5);
            }
        }

        return List.copyOf(var6);
    }

    public static boolean matches(ShopOffer var0, String var1) {
        return var1 == null || var1.isEmpty() || id(var0).equals(var1);
    }

    public static String label(String var0) {
        return ModLanguageManager.getString("shop.quality." + (var0.isEmpty() ? "all" : var0));
    }

    public static int color(String var0, int var1) {
        ItemRarity var2 = ItemRarity.fromId(var0);
        return var2 == null ? var1 : var2.argb(255);
    }

    public static Component toolTitle(ShopOffer var0, Component var1) {
        if (ShopSmithData.isTool(var0) && var1 != null) {
            Integer[] var2 = new Integer[] {null};
            StringDecomposer.iterateFormatted(
                    var0.name() == null ? Component.empty() : var0.name(),
                    Style.EMPTY,
                    (var1x, var2x, var3x) -> {
                        if (!Character.isWhitespace(var3x)
                                && var3x != 60
                                && var3x != 62
                                && var2x.getColor() != null) {
                            var2[0] = var2x.getColor().getValue();
                            return false;
                        } else {
                            return true;
                        }
                    });
            ItemRarity var3 = ItemRarity.fromId(id(var0));
            if ((var2[0] == null || var2[0] == 16777215 || var2[0] == 11184810) && var3 != null) {
                var2[0] = var3.rgb;
            }

            if (var2[0] == null) {
                return var1;
            }

            int var4 = var2[0];
            MutableComponent var5 = Component.empty().withStyle(var1.getStyle().withColor(var4));
            StringBuilder var6 = new StringBuilder();
            Style[] var7 = new Style[] {null};
            StringDecomposer.iterateFormatted(
                    var1,
                    Style.EMPTY,
                    (var4x, var5x, var6x) -> {
                        Style var7x =
                                var5x.withColor(var4).withClickEvent(null).withHoverEvent(null);
                        if (var7[0] != null && !var7[0].equals(var7x)) {
                            var5.append(Component.literal(var6.toString()).withStyle(var7[0]));
                            var6.setLength(0);
                        }

                        var6.appendCodePoint(var6x);
                        var7[0] = var7x;
                        return true;
                    });
            if (!var6.isEmpty()) {
                var5.append(Component.literal(var6.toString()).withStyle(var7[0]));
            }

            return var5;
        } else {
            return var1;
        }
    }

    public static ItemStack tooltipStack(ShopOffer var0) {
        ItemStack var1 = var0.stack();
        if (ShopSmithData.isTool(var0) && !var1.isEmpty()) {
            ItemStack var2 = var1.copy();
            var2.set(DataComponents.CUSTOM_NAME, toolTitle(var0, var1.getHoverName()));
            return var2;
        } else {
            return var1;
        }
    }

    public static List<Component> tooltipLines(ShopOffer var0, List<Component> var1) {
        if (ShopSmithData.isTool(var0) && !var1.isEmpty()) {
            ArrayList var2 = new ArrayList(var1);
            var2.set(0, toolTitle(var0, (Component) var2.getFirst()));
            return var2;
        } else {
            return var1;
        }
    }
}
