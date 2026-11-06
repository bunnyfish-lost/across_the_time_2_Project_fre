package fr.poubone.att2.client.rune;

import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public final class RuneWorkshopRecipes119 {
    private RuneWorkshopRecipes119() {}

    private static boolean isPotion(String var0) {
        return List.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion")
                .contains(var0);
    }

    private static boolean emptyBundle(ItemStack var0) {
        BundleContents var1 = var0.get(DataComponents.BUNDLE_CONTENTS);
        return var1 == null || var1.isEmpty();
    }

    public static ItemStack item(String var0) {
        return new ItemStack(
                BuiltInRegistries.ITEM
                        .getOptional(Identifier.withDefaultNamespace(var0))
                        .orElseThrow());
    }

    public static String quality(String var0) {
        return switch (var0) {
            case "com" -> "普通";
            case "unc" -> "优良";
            case "rar" -> "稀有";
            case "epi" -> "史诗";
            case "epi_set" -> "史诗套装";
            case "epi_esc" -> "混沌史诗";
            case "leg" -> "传奇";
            case "leg_armset" -> "传奇套装";
            case "ult" -> "终极";
            default -> var0;
        };
    }

    private static RuneWorkshopRecipes119.Material rune(String var0) {
        RuneCatalog.RuneDef var1 = RuneCatalog.byId(var0).orElseThrow();
        return new RuneWorkshopRecipes119.Material(
                RuneWorkshopRecipes119.Kind.RUNE,
                var0,
                1,
                RuneItems.rune(var1),
                Component.translatable(var1.translationKey()));
    }

    private static List<RuneWorkshopRecipes119.Material> runes(String... var0) {
        return Arrays.stream(var0).map(RuneWorkshopRecipes119::rune).toList();
    }

    private static RuneWorkshopRecipes119.Material quality(
            RuneWorkshopRecipes119.Kind var0, String var1, int var2) {
        String var3 =
                quality(var1)
                        + (var0 == RuneWorkshopRecipes119.Kind.POTION
                                ? "药水"
                                : (var0 == RuneWorkshopRecipes119.Kind.GEAR ? "装备" : "物品"));
        return new RuneWorkshopRecipes119.Material(
                var0,
                var1,
                var2,
                item(var0 == RuneWorkshopRecipes119.Kind.POTION ? "potion" : "iron_sword"),
                Component.literal(var3));
    }

    private static RuneWorkshopRecipes119.Material special(
            RuneWorkshopRecipes119.Kind var0, String var1, String var2, String var3) {
        ItemStack var4 = item(var2);
        return new RuneWorkshopRecipes119.Material(
                var0, var1, 1, var4, Component.translatable(var3));
    }

    private static RuneWorkshopRecipes119.Recipe manual(
            String var0,
            Component var1,
            ItemStack var2,
            List<RuneWorkshopRecipes119.Material> var3) {
        return new RuneWorkshopRecipes119.Recipe(
                var0,
                3,
                var1,
                var2,
                RuneWorkshopRecipes119.Mode.MANUAL,
                List.copyOf(var3),
                null,
                null,
                null);
    }

    public static List<RuneWorkshopRecipes119.Recipe> category(int var0) {
        ArrayList var1 = new ArrayList();
        if (var0 == 0) {
            for (RuneCatalog.RuneDef var3 : RuneCatalog.allRunes()) {
                var1.add(
                        new RuneWorkshopRecipes119.Recipe(
                                "r:" + var3.id(),
                                0,
                                Component.translatable(var3.translationKey()),
                                RuneItems.rune(var3),
                                RuneWorkshopRecipes119.Mode.DIRECT,
                                List.of(),
                                var3,
                                null,
                                null));
            }
        }

        if (var0 == 1) {
            for (RuneCatalog.WordDef var11 : RuneCatalog.words()) {
                ArrayList var4 = new ArrayList();

                for (RuneCatalog.Ingredient var6 : var11.ingredients()) {
                    for (int var7 = 0; var7 < var6.count(); var7++) {
                        var4.add(rune(var6.runeId()));
                    }
                }

                var1.add(
                        new RuneWorkshopRecipes119.Recipe(
                                "w:" + var11.wordId(),
                                1,
                                Component.translatable(var11.nameKey()),
                                RuneResultIcons119.word(var11),
                                var11.trigger() > 0
                                        ? RuneWorkshopRecipes119.Mode.DIRECT
                                        : RuneWorkshopRecipes119.Mode.MANUAL,
                                List.copyOf(var4),
                                null,
                                var11,
                                null));
            }
        }

        if (var0 == 2) {
            for (RuneCatalog.ArrowDef var12 : RuneCatalog.arrows()) {
                ArrayList var14 = new ArrayList();
                ItemStack var15 = RuneItems.craftBaseArrow(var12);
                var14.add(
                        new RuneWorkshopRecipes119.Material(
                                RuneWorkshopRecipes119.Kind.ARROW,
                                var12.id(),
                                var12.baseArrowCount(),
                                var15,
                                var15.getHoverName()));

                for (RuneCatalog.Ingredient var17 : var12.runes()) {
                    var14.add(
                            new RuneWorkshopRecipes119.Material(
                                    RuneWorkshopRecipes119.Kind.RUNE,
                                    var17.runeId(),
                                    var17.count(),
                                    RuneItems.rune(RuneCatalog.byId(var17.runeId()).orElseThrow()),
                                    Component.translatable(
                                            RuneCatalog.byId(var17.runeId())
                                                    .orElseThrow()
                                                    .translationKey())));
                }

                var1.add(
                        new RuneWorkshopRecipes119.Recipe(
                                "a:" + var12.id(),
                                2,
                                Component.translatable(var12.nameKey()),
                                RuneItems.arrow(var12),
                                RuneWorkshopRecipes119.Mode.MANUAL,
                                List.copyOf(var14),
                                null,
                                null,
                                null));
            }
        }

        if (var0 == 3) {
            other(var1);
        }

        if (var0 == 4) {
            for (RuneCatalog.BonusDef var13 : RuneCatalog.bonuses()) {
                var1.add(
                        new RuneWorkshopRecipes119.Recipe(
                                "b:" + var13.holder(),
                                4,
                                ModLanguageManager.get(var13.langKey()),
                                RuneItems.pouchIcon(),
                                RuneWorkshopRecipes119.Mode.INFO,
                                List.of(),
                                null,
                                null,
                                var13));
            }
        }

        return List.copyOf(var1);
    }

    private static void other(List<RuneWorkshopRecipes119.Recipe> var0) {
        label153:
        for (RuneCatalog.InfoDef var2 : RuneCatalog.others()) {
            ItemStack var3 = RuneItems.other(var2);
            MutableComponent var4 = Component.translatable(var2.nameKey());
            ArrayList var5 = new ArrayList();
            switch (var2.id()) {
                case "esc_2":
                case "esc_7":
                case "esc_25":
                    String[] var16 =
                            var2.id().equals("esc_2")
                                    ? new String[] {"com", "unc", "rar", "epi"}
                                    : (var2.id().equals("esc_7")
                                            ? new String[] {"com", "unc", "rar", "epi", "leg"}
                                            : new String[] {"unc", "rar", "epi", "leg", "ult"});

                    for (String var26 : var16) {
                        var5.add(quality(RuneWorkshopRecipes119.Kind.GEAR, var26, 1));
                    }

                    var4 = var4.copy().append(" ×" + var2.id().substring(4));
                    break;
                case "loot_runes":
                    var5.add(
                            new RuneWorkshopRecipes119.Material(
                                    RuneWorkshopRecipes119.Kind.ESC,
                                    "esc",
                                    1,
                                    RuneItems.esc(),
                                    RuneItems.esc().getHoverName()));
                    break;
                case "extraloot":
                    var5.add(
                            new RuneWorkshopRecipes119.Material(
                                    RuneWorkshopRecipes119.Kind.KINUIL,
                                    "kinuil",
                                    1,
                                    item("diamond_axe"),
                                    Component.literal("Kinuil")));
                    break;
                case "elixir":
                    for (String var19 : List.of("leg", "epi", "rar", "epi", "leg")) {
                        var5.add(quality(RuneWorkshopRecipes119.Kind.POTION, var19, 1));
                    }
                    break;
                case "rune_bundle":
                    var5.addAll(runes("ra", "ust"));
                    var5.add(
                            special(
                                    RuneWorkshopRecipes119.Kind.BUNDLE,
                                    "empty",
                                    "bundle",
                                    "att2.misc.bundle"));
                    var5.addAll(runes("nym", "ehl"));
                    break;
                case "spell_bundle_1":
                    var5.addAll(runes("tha", "org"));
                    var5.add(
                            special(
                                    RuneWorkshopRecipes119.Kind.BUNDLE,
                                    "empty",
                                    "bundle",
                                    "att2.misc.bundle"));
                    var5.addAll(runes("ra", "inu"));
                    break;
                case "spell_bundle_2":
                    var5.addAll(runes("von", "ave"));
                    var5.add(
                            special(
                                    RuneWorkshopRecipes119.Kind.SPELL_BUNDLE,
                                    "1",
                                    "blue_bundle",
                                    "att2.misc.spell_bundle_1.name"));
                    var5.addAll(runes("for", "wej"));
                    break;
                case "spell_bundle_3":
                    var5.addAll(runes("da", "ave"));
                    var5.add(
                            special(
                                    RuneWorkshopRecipes119.Kind.SPELL_BUNDLE,
                                    "2",
                                    "blue_bundle",
                                    "att2.misc.spell_bundle_2.name"));
                    var5.addAll(runes("ra", "bex"));
                    break;
                case "xp":
                    Iterator var14 =
                            List.of(
                                            "com",
                                            "unc",
                                            "rar",
                                            "epi",
                                            "epi_set",
                                            "epi_esc",
                                            "leg",
                                            "leg_armset",
                                            "ult")
                                    .iterator();

                    while (true) {
                        if (!var14.hasNext()) {
                            continue label153;
                        }

                        String var18 = (String) var14.next();
                        var0.add(
                                manual(
                                        "i:xp:" + var18,
                                        var4.copy().append(" · " + quality(var18)),
                                        var3,
                                        List.of(
                                                quality(
                                                        RuneWorkshopRecipes119.Kind.QUALITY,
                                                        var18,
                                                        1))));
                    }
                case "reforge":
                    String[] var8 = new String[] {"com", "unc", "rar", "epi", "leg", "ult"};

                    for (RuneWorkshopRecipes119.Kind var10 :
                            List.of(
                                    RuneWorkshopRecipes119.Kind.GEAR,
                                    RuneWorkshopRecipes119.Kind.POTION)) {
                        for (int var11 = 0; var11 < var8.length - 1; var11++) {
                            ArrayList var12 = new ArrayList();

                            for (int var13 = 0; var13 < 4; var13++) {
                                var12.add(quality(var10, var8[var11], 1));
                            }

                            var12.add(quality(var10, var8[var11 + 1], 1));
                            var0.add(
                                    manual(
                                            "i:reforge:" + var10 + ":" + var8[var11],
                                            var4.copy()
                                                    .append(
                                                            " · "
                                                                    + (var10
                                                                                    == RuneWorkshopRecipes119
                                                                                            .Kind
                                                                                            .POTION
                                                                            ? "药水 "
                                                                            : "装备 ")
                                                                    + quality(var8[var11])
                                                                    + "→"
                                                                    + quality(var8[var11 + 1])),
                                            var3,
                                            var12));
                        }
                    }

                    for (String var21 : List.of("epi_set", "leg_armset")) {
                        String var23 = var21.equals("epi_set") ? "rar" : "epi";
                        ArrayList var25 = new ArrayList();

                        for (int var27 = 0; var27 < 4; var27++) {
                            var25.add(quality(RuneWorkshopRecipes119.Kind.GEAR, var23, 1));
                        }

                        var25.add(quality(RuneWorkshopRecipes119.Kind.GEAR, var21, 1));
                        var0.add(
                                manual(
                                        "i:reforge:" + var21,
                                        var4.copy()
                                                .append(
                                                        " · "
                                                                + quality(var23)
                                                                + "→"
                                                                + quality(var21)),
                                        var3,
                                        var25));
                    }
                    continue;
            }

            var0.add(manual("i:" + var2.id(), var4, var3, var5));
        }
    }

    public static RuneWorkshopRecipes119.Recipe find(String var0) {
        for (int var1 = 0; var1 < 5; var1++) {
            for (RuneWorkshopRecipes119.Recipe var3 : category(var1)) {
                if (var3.key().equals(var0)) {
                    return var3;
                }
            }
        }

        return null;
    }

    public enum Kind {
        RUNE,
        ARROW,
        QUALITY,
        GEAR,
        POTION,
        BUNDLE,
        SPELL_BUNDLE,
        ESC,
        KINUIL;
    }

    public record Material(
            RuneWorkshopRecipes119.Kind kind,
            String id,
            int quantity,
            ItemStack icon,
            Component name) {
        public boolean matches(ItemStack var1) {
            if (var1 != null && !var1.isEmpty()) {
                CustomData var2 = var1.get(DataComponents.CUSTOM_DATA);
                if (var2 != null && !var2.isEmpty()) {
                    CompoundTag var3 = var2.copyTag();
                    String var4 = var3.getString("Rarity").orElse("");
                    String var5 = BuiltInRegistries.ITEM.getKey(var1.getItem()).toString();

                    return switch (this.kind) {
                        case RUNE ->
                                RuneCatalog.byId(this.id)
                                        .map(var1x -> RuneStock.countStack(var1, var1x) > 0)
                                        .orElse(false);
                        case ARROW ->
                                RuneCatalog.arrowById(this.id)
                                        .map(var1x -> RuneItems.matchesCraftBaseArrow(var1, var1x))
                                        .orElse(false);
                        case QUALITY -> var4.equals(this.id);
                        case GEAR ->
                                var4.equals(this.id)
                                        && !RuneWorkshopRecipes119.isPotion(var5)
                                        && !List.of(
                                                        "minecraft:snowball",
                                                        "minecraft:fishing_rod",
                                                        "minecraft:arrow",
                                                        "minecraft:tipped_arrow",
                                                        "minecraft:spectral_arrow")
                                                .contains(var5);
                        case POTION ->
                                var4.equals(this.id) && RuneWorkshopRecipes119.isPotion(var5);
                        case BUNDLE ->
                                var5.equals("minecraft:bundle")
                                        && "bundle"
                                                .equals(var3.getString("EquipmentType").orElse(""))
                                        && RuneWorkshopRecipes119.emptyBundle(var1);
                        case SPELL_BUNDLE ->
                                var5.equals("minecraft:blue_bundle")
                                        && var3.getIntOr("Spell_Bundle", -1)
                                                == Integer.parseInt(this.id);
                        case ESC ->
                                var5.equals("minecraft:quartz")
                                        && "unk".equals(var4)
                                        && "esc".equals(var3.getString("Coin").orElse(""));
                        case KINUIL ->
                                var5.equals("minecraft:diamond_axe")
                                        && "kinuil"
                                                .equals(var3.getString("EquipmentID").orElse(""));
                    };
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        public int owned(LocalPlayer var1) {
            if (var1 == null) {
                return 0;
            }

            int var2 = 0;

            for (int var3 = 0; var3 < var1.getInventory().getContainerSize(); var3++) {
                ItemStack var4 = var1.getInventory().getItem(var3);
                if (this.matches(var4)) {
                    var2 += var4.getCount();
                }
            }

            return var2;
        }

        public String key() {
            return this.kind + ":" + this.id;
        }
    }

    public enum Mode {
        DIRECT,
        MANUAL,
        INFO;
    }

    public record Recipe(
            String key,
            int category,
            Component name,
            ItemStack icon,
            RuneWorkshopRecipes119.Mode mode,
            List<RuneWorkshopRecipes119.Material> sequence,
            RuneCatalog.RuneDef rune,
            RuneCatalog.WordDef word,
            RuneCatalog.BonusDef bonus) {
        public boolean trackable() {
            return !this.sequence.isEmpty();
        }

        public List<RuneWorkshopRecipes119.Row> rows() {
            LinkedHashMap<String, RuneWorkshopRecipes119.Material> var1 = new LinkedHashMap();
            HashMap<String, Integer> var2 = new HashMap();
            HashMap<String, List<Integer>> var3 = new HashMap();

            for (int var4 = 0; var4 < this.sequence.size(); var4++) {
                RuneWorkshopRecipes119.Material var5 = this.sequence.get(var4);
                var1.putIfAbsent(var5.key(), var5);
                var2.merge(var5.key(), var5.quantity(), Integer::sum);
                var3.computeIfAbsent(var5.key(), var0 -> new ArrayList<>()).add(var4 + 1);
            }

            ArrayList var7 = new ArrayList();

            for (var var6 : var1.entrySet()) {
                var7.add(
                        new RuneWorkshopRecipes119.Row(
                                (RuneWorkshopRecipes119.Material) var6.getValue(),
                                (Integer) var2.get(var6.getKey()),
                                List.copyOf(
                                        (Collection<? extends Integer>) var3.get(var6.getKey()))));
            }

            return List.copyOf(var7);
        }
    }

    public record Row(RuneWorkshopRecipes119.Material material, int needed, List<Integer> orders) {}
}
