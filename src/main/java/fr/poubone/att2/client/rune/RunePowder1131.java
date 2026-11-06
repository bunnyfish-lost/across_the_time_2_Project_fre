package fr.poubone.att2.client.rune;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class RunePowder1131 {
    public static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("att2", "fre/rune_powder");

    private RunePowder1131() {}

    public static ItemStack apply(ItemStack var0) {
        if (var0 != null && !var0.isEmpty()) {
            var0.set(DataComponents.ITEM_MODEL, MODEL);
        }

        return var0;
    }

    public static boolean custom(ItemStack var0) {
        return var0 != null && !var0.isEmpty() && MODEL.equals(var0.get(DataComponents.ITEM_MODEL));
    }
}
