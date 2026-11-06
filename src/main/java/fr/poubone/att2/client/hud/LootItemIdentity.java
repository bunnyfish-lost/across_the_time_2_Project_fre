package fr.poubone.att2.client.hud;

import fr.poubone.att2.client.renderer.ItemRarity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class LootItemIdentity {
    private LootItemIdentity() {}

    public static String coinRarityId(String var0) {
        if (var0 != null && !var0.isBlank()) {
            return "esc".equals(var0) ? "esc" : "cur";
        } else {
            return null;
        }
    }

    public static String rarityId(ItemStack var0) {
        if (var0 != null && !var0.isEmpty()) {
            CustomData var1 = var0.get(DataComponents.CUSTOM_DATA);
            if (var1 != null && !var1.isEmpty()) {
                String var2 = coinRarityId(var1.copyTag().getString("Coin").orElse(""));
                if (var2 != null) {
                    return var2;
                }
            }

            ItemRarity var3 = ItemRarity.fromStack(var0);
            if (var3 != null) {
                return var3.id;
            } else {
                return var1 != null && !var1.isEmpty()
                        ? var1.copyTag().getString("Rarity").orElse(null)
                        : null;
            }
        } else {
            return null;
        }
    }
}
