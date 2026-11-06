package fr.poubone.att2.client.shop;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

public final class ShopDyeRendering {
    private ShopDyeRendering() {}

    public static boolean allowSprite(Identifier var0, ShopOffer var1) {
        return ShopIcons.isPackTexture(var0)
                && (var1 == null
                        || var1.stack() == null
                        || var1.stack().get(DataComponents.DYED_COLOR) == null);
    }
}
