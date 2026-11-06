package fr.poubone.att2.client.shop;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Map;

public final class ShopNpcNames {
    private static final String PREFIX = "att2.npc.name.";
    private static final Map<String, String> MAP_IDS =
            Map.of("rokar_borton", "borton", "lisa_payin", "lisa", "marc_aboth", "mark_aboth");
    private static final Map<String, String> KNOWN_NAMES =
            Map.of(
                    "rokar_borton",
                    "Rokar Borton",
                    "lisa_payin",
                    "Lisa Payin",
                    "marc_aboth",
                    "Mark Aboth");

    private ShopNpcNames() {}

    public static Component resolve(String var0) {
        if (var0 != null && !var0.isBlank()) {
            String var1 = "att2.npc.name." + var0;
            if (Language.getInstance().has(var1)) {
                return Component.translatable(var1);
            }

            String var2 = MAP_IDS.get(var0);
            return var2 != null && Language.getInstance().has("att2.npc.name." + var2)
                    ? Component.translatable("att2.npc.name." + var2)
                    : Component.literal(fallback(var0));
        } else {
            return Component.literal("");
        }
    }

    static String fallback(String var0) {
        if (KNOWN_NAMES.containsKey(var0)) {
            return KNOWN_NAMES.get(var0);
        }

        StringBuilder var1 = new StringBuilder();

        for (String var5 : var0.replace('_', ' ').strip().split("\\s+")) {
            if (!var5.isEmpty()) {
                if (var1.length() > 0) {
                    var1.append(' ');
                }

                var1.append(var5.substring(0, 1).toUpperCase(Locale.ROOT));
                var1.append(var5.substring(1));
            }
        }

        return var1.toString();
    }
}
