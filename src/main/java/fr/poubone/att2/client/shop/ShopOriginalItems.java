package fr.poubone.att2.client.shop;

import java.util.Map;
import java.util.WeakHashMap;

public final class ShopOriginalItems {
    private static final Map<ShopOffer, Boolean> ORIGINAL = new WeakHashMap<>();

    private ShopOriginalItems() {}

    public static ShopOffer mark(ShopOffer var0) {
        if (var0 != null) {
            ORIGINAL.put(var0, Boolean.TRUE);
        }

        return var0;
    }

    public static boolean contains(ShopOffer var0) {
        return var0 != null && ORIGINAL.containsKey(var0);
    }
}
