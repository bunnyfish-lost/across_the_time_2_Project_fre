package fr.poubone.att2.client.rune;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class RunePouchStock1113 {
    private RunePouchStock1113() {}

    public static boolean isRune(RuneWorkshopRecipes119.Material var0) {
        return var0 != null && var0.kind() == RuneWorkshopRecipes119.Kind.RUNE;
    }

    public static int count(RuneWorkshopRecipes119.Material var0, LocalPlayer var1) {
        if (!isRune(var0)) {
            return -1;
        } else {
            return var1 == null
                    ? 0
                    : RuneCatalog.byId(var0.id())
                            .map(var1x -> RunePouchSync1115.count(var1x, var1))
                            .orElse(0);
        }
    }

    public static ItemStack icon(LocalPlayer var0) {
        if (var0 != null) {
            for (int var1 = 0; var1 < var0.getInventory().getContainerSize(); var1++) {
                ItemStack var2 = var0.getInventory().getItem(var1);
                if (RuneStock.isBundle(var2)) {
                    ItemStack var3 = var2.copy();
                    var3.setCount(1);
                    return var3;
                }
            }
        }

        ItemStack var4 = RuneItems.pouchIcon();
        var4.set(DataComponents.CUSTOM_NAME, Component.translatable("item.rune_bundle.name"));
        return var4;
    }
}
