package fr.poubone.att2.client.rune;

import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent.ShowItem;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RuneItemDetails {
    private static final Map<String, ItemStack> ITEMS = new HashMap<>();
    private static int version;

    private RuneItemDetails() {}

    public static int version() {
        return version;
    }

    public static void reset() {
        ITEMS.clear();
        version++;
    }

    public static void capture(Component var0) {
        Set var1 = Collections.newSetFromMap(new IdentityHashMap());
        visit(var0, var1);
    }

    private static void visit(Component var0, Set<Component> var1) {
        if (var0 != null && var1.add(var0)) {
            if (var0.getStyle().getHoverEvent() instanceof ShowItem var2) {
                ItemStack var10 = var2.item();
                String var4 = identity(var10);
                if (var4 != null) {
                    ItemStack var5 = var10.copy();
                    var5.setCount(1);
                    ItemStack var6 = ITEMS.get(var4);
                    if (var6 == null || !ItemStack.isSameItemSameComponents(var6, var5)) {
                        ITEMS.put(var4, var5);
                        version++;
                    }
                }
            }

            if (var0.getContents() instanceof TranslatableContents var8) {
                for (Object var16 : var8.getArgs()) {
                    if (var16 instanceof Component var7) {
                        visit(var7, var1);
                    }
                }
            }

            for (Component var13 : var0.getSiblings()) {
                visit(var13, var1);
            }
        }
    }

    private static String nameKey(ItemStack var0) {
        if (var0 != null && !var0.isEmpty()) {
            Component var1 = var0.get(DataComponents.CUSTOM_NAME);
            return var1 != null && var1.getContents() instanceof TranslatableContents var2
                    ? var2.getKey()
                    : null;
        } else {
            return null;
        }
    }

    static String identity(ItemStack var0) {
        if (var0 != null && !var0.isEmpty()) {
            CustomData var1 = var0.get(DataComponents.CUSTOM_DATA);
            if (var1 != null && !var1.isEmpty()) {
                CompoundTag var2 = var1.copyTag();
                String var3 = var2.getString("EquipmentType").orElse("");
                if ("runic_word".equals(var3)) {
                    String var4 = var2.getString("RuneWord").orElse("");
                    if (!var4.isEmpty()) {
                        return "word:" + var4;
                    }
                }

                if ("rune".equals(var3)) {
                    byte var6 = var2.getByteOr("Runelvl", (byte) -1);
                    if (var6 >= 0) {
                        return "rune:" + var6;
                    }
                }
            }

            String var5 = nameKey(var0);
            if (var5 != null) {
                if (var5.matches("item\\.runeword\\.\\d+\\.name")) {
                    return "word:" + var5.split("\\.")[2];
                }

                if (var5.matches("item\\.rune\\.name\\.\\d+")) {
                    return "rune:" + var5.substring("item.rune.name.".length());
                }

                if (var5.equals("item.runeword.stock_upgrade")) {
                    return "word:20";
                }

                if (var5.equals("att2.spell31.name")) {
                    return "word:21";
                }

                if (var5.startsWith("item.rune.name.")
                        || var5.startsWith("item.runeword.")
                        || var5.startsWith("att2.item.special_arrow.")
                        || var5.startsWith("item.rune_bundle.")
                        || var5.startsWith("att2.misc.spell_bundle_")) {
                    return "name:" + var5;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public static boolean hasCaptured(ItemStack var0) {
        String var1 = identity(var0);
        return var1 != null && ITEMS.containsKey(var1);
    }

    public static ItemStack resolve(ItemStack var0) {
        if (var0 != null && !var0.isEmpty()) {
            String var1 = identity(var0);
            ItemStack var2 = var1 == null ? null : ITEMS.get(var1);
            if (var2 == null) {
                var2 = RuneItems.inventoryMatch(var0).orElse(null);
            }

            if (var2 != null) {
                ItemStack var6 = var2.copy();
                var6.setCount(var0.getCount());
                return var6;
            }

            ItemStack var3 = var0.copy();
            ItemLore var4 = var3.get(DataComponents.LORE);
            if (var4 != null && !var4.lines().isEmpty()) {
                return var3;
            }

            List var5 = translatedLore(nameKey(var0));
            if (!var5.isEmpty()) {
                var3.set(DataComponents.LORE, new ItemLore(var5));
            }

            return var3;
        } else {
            return var0;
        }
    }

    static List<Component> translatedLore(String var0) {
        if (var0 == null) {
            return List.of();
        }

        ArrayList var1 = new ArrayList();

        for (String var3 : RuneLoreKeys.KEYS.getOrDefault(var0, List.of())) {
            if (Language.getInstance().has(var3)) {
                var1.add(Component.translatable(var3));
            }
        }

        return List.copyOf(var1);
    }

    public static List<Component> lore(ItemStack var0) {
        ItemStack var1 = resolve(var0);
        if (var1 != null && !var1.isEmpty()) {
            ItemLore var2 = var1.get(DataComponents.LORE);
            return var2 == null ? List.of() : List.copyOf(var2.lines());
        } else {
            return List.of();
        }
    }
}
