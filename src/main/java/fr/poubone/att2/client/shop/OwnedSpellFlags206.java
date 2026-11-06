package fr.poubone.att2.client.shop;

import fr.poubone.att2.client.hud.HUDConfig;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class OwnedSpellFlags206 {
    private static final Map<ShopCatalog, Map<Integer, Boolean>> MARKED = new WeakHashMap<>();

    private OwnedSpellFlags206() {}

    public static void clear(ShopCatalog var0) {
        MARKED.remove(var0);
    }

    public static void addOffer(ShopCatalog var0, ShopOffer var1, Component var2) {
        var0.addOffer(var1);
        MARKED.computeIfAbsent(var0, var0x -> new HashMap<>()).put(var1.trigger(), marker(var2));
    }

    private static boolean marker(Component var0) {
        if (var0 == null) {
            return false;
        }

        if (var0.getContents() instanceof PlainTextContents var1
                && var1.text().strip().equals("°")) {
            TextColor var4 = var0.getStyle().getColor();
            if (var4 != null && var4.getValue() == 43520) {
                return true;
            }
        }

        for (Component var5 : var0.getSiblings()) {
            if (marker(var5)) {
                return true;
            }
        }

        return false;
    }

    public static Set<Integer> carried(Player var0) {
        HashSet var1 = new HashSet();
        if (var0 != null) {
            for (int var2 = 0; var2 < var0.getInventory().getContainerSize(); var2++) {
                ItemStack var3 = var0.getInventory().getItem(var2);
                CustomData var4 = var3.get(DataComponents.CUSTOM_DATA);
                if (var4 != null) {
                    int var5 = var4.copyTag().getIntOr("Spell", 0);
                    if (var5 > 0) {
                        var1.add(var5);
                    }
                }
            }
        }

        return Set.copyOf(var1);
    }

    public static boolean visible(ShopCatalog var0, ShopOffer var1, Set<Integer> var2) {
        if (var0.type() == ShopType.DAHAL && HUDConfig.get().shopHideOwnedSpells) {
            if (MARKED.getOrDefault(var0, Map.of()).getOrDefault(var1.trigger(), false)) {
                return false;
            }

            if (var1.trigger() >= 424 && var1.trigger() <= 432) {
                return true;
            }

            int var3 = ShopIcons.spellId(var1);
            return var3 <= 0 || !var2.contains(var3);
        } else {
            return true;
        }
    }
}
