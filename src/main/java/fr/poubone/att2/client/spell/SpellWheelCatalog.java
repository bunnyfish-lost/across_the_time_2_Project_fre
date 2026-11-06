package fr.poubone.att2.client.spell;

import fr.poubone.att2.client.data.SpellLauncherTracker;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpellWheelCatalog {
    public static final List<Integer> IDS =
            List.of(
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
                    31, 32, 33, 34, 35, 40, 41, 42, 43, 44, 45, 46);

    private SpellWheelCatalog() {}

    public static Map<Integer, SpellWheelCatalog.Owned> scan(Inventory var0, ItemStack var1) {
        return scan(var0, var1, var0.getSelectedSlot());
    }

    public static Map<Integer, SpellWheelCatalog.Owned> scan(
            Inventory var0, ItemStack var1, int var2) {
        ArrayList var3 = new ArrayList();

        for (int var4 = 0; var4 < var0.getContainerSize(); var4++) {
            var3.add(var0.getItem(var4));
        }

        return scan(var3, var1, var2);
    }

    public static Map<Integer, SpellWheelCatalog.Owned> scan(
            List<ItemStack> var0, ItemStack var1, int var2) {
        HashMap var3 = new HashMap();

        for (int var4 = 0; var4 < var0.size(); var4++) {
            consider(var3, (ItemStack) var0.get(var4), var4, var2);
        }

        consider(var3, var1, 40, var2);
        return var3;
    }

    private static void consider(
            Map<Integer, SpellWheelCatalog.Owned> var0, ItemStack var1, int var2, int var3) {
        SpellLauncherTracker.LauncherState var4 = SpellLauncherTracker.parse(var1);
        if (var4 != null && IDS.contains(var4.spellId)) {
            SpellWheelCatalog.Owned var5 = (SpellWheelCatalog.Owned) var0.get(var4.spellId);
            if (var5 == null
                    || var4.level > var5.state().level
                    || var4.level == var5.state().level
                            && var2 == var3
                            && var5.inventorySlot() != var3) {
                var0.put(var4.spellId, new SpellWheelCatalog.Owned(var4.spellId, var2, var4));
            }
        }
    }

    public static Component name(int var0) {
        MutableComponent var1 = Component.translatable("att2.spell" + var0 + ".name");
        String var2 = var1.getString();
        if (var2.startsWith("att2.spell")) {
            var2 = "Spell " + var0;
        }

        int var3 = defaultColor(var0);
        String var4 = "0123456789abcdef";
        int[] var5 =
                new int[] {
                    0, 170, 43520, 43690, 11141120, 11141290, 16755200, 11184810, 5592405, 5592575,
                    5635925, 5636095, 16733525, 16733695, 16777045, 16777215
                };

        for (int var6 = 0; var6 + 1 < var2.length(); var6++) {
            if (var2.charAt(var6) == 167) {
                int var7 = var4.indexOf(Character.toLowerCase(var2.charAt(var6 + 1)));
                if (var7 >= 0) {
                    var3 = var5[var7];
                    break;
                }
            }
        }

        if (var1.getStyle().getColor() != null) {
            var3 = var1.getStyle().getColor().getValue();
        }

        return Component.literal(ChatFormatting.stripFormatting(var2)).withColor(var3);
    }

    public static int defaultColor(int var0) {
        if (var0 <= 11 || var0 == 33) {
            return 16733525;
        } else if (var0 == 20) {
            return 170;
        } else if (var0 == 30) {
            return 16733695;
        } else if (var0 == 31 || var0 == 32 || var0 == 34) {
            return 16755200;
        } else if (var0 == 40) {
            return 0;
        } else {
            return var0 >= 41 ? 5635925 : 5592575;
        }
    }

    public record Owned(int spellId, int inventorySlot, SpellLauncherTracker.LauncherState state) {}
}
