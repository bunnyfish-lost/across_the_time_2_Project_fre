package fr.poubone.att2.client.renderer;

import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.hud.LootItemIdentity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class ItemPiles {
    private static final Map<ItemStack, String> RARITY = new WeakHashMap<>();

    private ItemPiles() {}

    public static boolean enabled() {
        return HUDConfig.get().lootPiles;
    }

    public static Set<ItemEntity> representatives(Collection<ItemEntity> var0) {
        return representatives(var0, ItemPiles::key, Entity::getId);
    }

    static <T> Set<T> representatives(
            Collection<T> var0, Function<T, Object> var1, ToIntFunction<T> var2) {
        HashMap<Object, T> var3 = new HashMap();

        for (T var5 : var0) {
            var3.merge(
                    var1.apply(var5),
                    var5,
                    (var1x, var2x) ->
                            (var2.applyAsInt(var1x) <= var2.applyAsInt(var2x) ? var1x : var2x));
        }

        Set<T> var6 = Collections.newSetFromMap(new IdentityHashMap());
        var6.addAll(var3.values());
        return var6;
    }

    private static Object key(ItemEntity var0) {
        return new ItemPiles.PileKey(
                Mth.floor(var0.getX()),
                Mth.floor(var0.getY()),
                Mth.floor(var0.getZ()),
                rarity(var0.getItem()));
    }

    private static String rarity(ItemStack var0) {
        String var1 = LootItemIdentity.rarityId(var0);
        CustomData var2 = var0.get(DataComponents.CUSTOM_DATA);
        String var3 = var2 == null ? "" : var2.copyTag().getString("Coin").orElse("");
        return (var1 == null ? "" : var1) + "|" + var3;
    }

    private record PileKey(int x, int y, int z, String rarity) {}
}
