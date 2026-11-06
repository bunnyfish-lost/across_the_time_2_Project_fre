package fr.poubone.att2.client.shop;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public final class ShopOfferPresentation {
    private ShopOfferPresentation() {}

    public static String plain(Component var0) {
        return var0 == null ? "" : ChatFormatting.stripFormatting(var0.getString());
    }

    public static String cleanName(String var0, String var1) {
        String var2 = var0 == null ? "" : var0.strip();
        String var3 = var1 == null ? "" : var1.strip();
        if (!var3.isEmpty() && var2.endsWith(var3)) {
            var2 = var2.substring(0, var2.length() - var3.length()).stripTrailing();
        }

        if (var2.startsWith("<")) {
            var2 = var2.substring(1).stripLeading();
        }

        if (var2.endsWith(">")) {
            var2 = var2.substring(0, var2.length() - 1).stripTrailing();
        }

        return var2;
    }

    public static Component displayName(ShopOffer var0) {
        Component var1 = var0.name();
        if (var1 != null
                && var1.getContents() instanceof TranslatableContents var2
                && var2.getKey().startsWith("att2.shop.")) {
            var1 = var1.plainCopy().setStyle(var1.getStyle());
        }

        Component var4 = ShopIcons.stripInlineObjects(var1);
        return Component.literal(cleanName(plain(var4), plain(var0.price())));
    }

    public static Component mapLore(Component var0) {
        LinkedHashMap<String, Component> var1 = new LinkedHashMap();
        Set var2 = Collections.newSetFromMap(new IdentityHashMap());
        collectHover(var0, var1, var2);
        if (var1.isEmpty()) {
            return null;
        }

        MutableComponent var3 = Component.empty();
        boolean var4 = true;

        for (Component var6 : var1.values()) {
            if (!var4) {
                var3.append(Component.literal("\n"));
            }

            var3.append(var6);
            var4 = false;
        }

        return var3;
    }

    private static void collectHover(
            Component var0, LinkedHashMap<String, Component> var1, Set<Component> var2) {
        if (var0 != null && var2.add(var0)) {
            if (var0.getStyle().getHoverEvent() instanceof ShowText var3) {
                Component var11 = var3.value();
                if (ShopTooltips.usefulLore(var11)) {
                    MutableComponent var5 =
                            var11.copy()
                                    .withStyle(
                                            var0x ->
                                                    var0x.withClickEvent(null)
                                                            .withHoverEvent(null));
                    String var6 = plain(var5).strip();
                    if (!var6.isEmpty()) {
                        var1.putIfAbsent(var6, var5);
                    }
                }
            }

            if (var0.getContents() instanceof TranslatableContents var9) {
                for (Object var7 : var9.getArgs()) {
                    if (var7 instanceof Component var8) {
                        collectHover(var8, var1, var2);
                    }
                }
            }

            for (Component var14 : var0.getSiblings()) {
                collectHover(var14, var1, var2);
            }
        }
    }

    public static List<Component> offerTooltip(ShopOffer var0, Component var1) {
        return composeTooltip(
                displayName(var0), var0.lore(), var0.price(), ShopTooltips.of(var0.stack()));
    }

    static List<Component> composeTooltip(
            Component var0, Component var1, Component var2, List<Component> var3) {
        ArrayList var4 = new ArrayList();
        var4.add(var0);

        for (int var5 = 1; var5 < var3.size(); var5++) {
            addDistinct(var4, (Component) var3.get(var5));
        }

        if (ShopTooltips.usefulLore(var1)) {
            for (Component var6 : ShopTooltips.flattenLore(var1)) {
                addDistinct(var4, var6);
            }
        }

        if (var2 != null && !plain(var2).isBlank()) {
            addDistinct(var4, Component.literal(plain(var2)));
        }

        return var4;
    }

    private static void addDistinct(List<Component> var0, Component var1) {
        String var2 = plain(var1).strip();
        if (!var2.isEmpty()) {
            if (var0.stream().noneMatch(var1x -> plain(var1x).strip().equals(var2))) {
                var0.add(var1);
            }
        }
    }
}
