package fr.poubone.att2.client.shop;

import fr.poubone.att2.client.data.CustomDataView;
import fr.poubone.att2.client.input.SneakCompareInput117;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.Equippable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public final class ShopComparison {
    private static final int GAP = 16;
    private static final int STACK_GAP = 10;
    private static final int PADDING = 12;
    private static final int MIN_COLUMN = 110;
    private static final int MAX_COLUMN = 240;
    private static Object contextScreen;
    private static Player contextPlayer;
    private static ItemStack contextItem;
    private static ShopComparison.Kind contextKind;
    private static List<String> stamps = List.of();
    private static int chosen;
    private static double wheel;

    private ShopComparison() {}

    static ShopComparison.Kind kind(ShopOffer var0) {
        return kind(var0.stack(), var0.equipmentType(), var0.translationKey());
    }

    public static ShopComparison.Kind kind(ItemStack var0) {
        if (var0 != null && !var0.isEmpty()) {
            CustomData var1 = var0.get(DataComponents.CUSTOM_DATA);
            String var2 = var1 == null ? "" : var1.copyTag().getString("EquipmentType").orElse("");
            return kind(var0, var2, "");
        } else {
            return null;
        }
    }

    static ShopComparison.Kind kind(ItemStack var0, String var1, String var2) {
        if (var0 != null && !var0.isEmpty()) {
            Equippable var3 = var0.get(DataComponents.EQUIPPABLE);
            if (var3 != null) {
                ShopComparison.Kind var4 = armorKind(var3.slot());
                if (var4 != null) {
                    return var4;
                }
            }
        }

        String var5 = itemPath(var0);
        ShopComparison.Kind var6 = armorKind(armorSlot(var1, var2, var5));
        return var6 != null ? var6 : weaponKind(var1, var5);
    }

    static String armorSlot(String var0, String var1, String var2) {
        String var3 = normalize(safe(var0) + " " + safe(var1) + " " + safe(var2));
        if (containsAny(var3, "helmet", "casque")) {
            return "HEAD";
        } else if (containsAny(var3, "chestplate", "plastron")) {
            return "CHEST";
        } else if (containsAny(var3, "leggings", "jambiere")) {
            return "LEGS";
        } else {
            return containsAny(var3, "boots", "bottes") ? "FEET" : null;
        }
    }

    static ShopComparison.Kind weaponKind(String var0, String var1) {
        String var2 = safe(var1);
        if (var2.equals("shield")) {
            return ShopComparison.Kind.SHIELD;
        }

        if (!var2.equals("bow") && !var2.equals("crossbow")) {
            String var3 = safe(var0);
            if (!var3.isEmpty()) {
                return switch (var3) {
                    case "meleeWeapon" -> ShopComparison.Kind.MELEE;
                    case "rangeWeapon" -> ShopComparison.Kind.RANGED;
                    default -> null;
                };
            } else if (!var2.equals("trident") && !var2.equals("mace")) {
                for (String var7 : new String[] {"_sword", "_axe", "_spear"}) {
                    if (var2.endsWith(var7)) {
                        return ShopComparison.Kind.MELEE;
                    }
                }

                return null;
            } else {
                return ShopComparison.Kind.MELEE;
            }
        } else {
            return ShopComparison.Kind.RANGED;
        }
    }

    static boolean shouldShowHint(ShopComparison.Kind var0, boolean var1) {
        return var0 != null && !var1;
    }

    static boolean holdsCompare(boolean var0, int var1, boolean var2, boolean var3) {
        if (var0) {
            return true;
        } else {
            return var1 != 340 && var1 != 344 ? false : var2 || var3;
        }
    }

    public static boolean inShop() {
        Minecraft var0 = Minecraft.getInstance();
        return var0 != null && var0.screen instanceof ShopScreen;
    }

    public static boolean isHeld(long var0, int var2) {
        boolean var3 = inShop() && SneakCompareInput117.held();
        if (!var3) {
            release();
        }

        return var3;
    }

    public static MutableComponent compareHint() {
        return Component.literal(
                ModLanguageManager.format(
                        "shop.compare.hold", "key", SneakCompareInput117.label().getString()));
    }

    private static MutableComponent shared(String var0) {
        return ModLanguageManager.getString(var0).equals(var0)
                ? Component.translatable(var0)
                : ModLanguageManager.get(var0);
    }

    private static MutableComponent shared(String var0, String var1, Object var2) {
        return ModLanguageManager.getString(var0).equals(var0)
                ? Component.translatable(var0, var2)
                : Component.literal(
                        ModLanguageManager.format(
                                var0,
                                var1,
                                var2 instanceof Component var3 ? var3.getString() : var2));
    }

    static EquipmentSlot slot(ShopOffer var0) {
        ShopComparison.Kind var1 = kind(var0);
        return var1 == null ? null : var1.slot;
    }

    static EquipmentSlot slot(ItemStack var0) {
        ShopComparison.Kind var1 = kind(var0);
        return var1 == null ? null : var1.slot;
    }

    static EquipmentSlot slot(ItemStack var0, String var1, String var2) {
        ShopComparison.Kind var3 = kind(var0, var1, var2);
        return var3 == null ? null : var3.slot;
    }

    static boolean shouldShowHint(String var0, boolean var1) {
        return var0 != null && !var1;
    }

    public static void release() {
        contextScreen = null;
        contextPlayer = null;
        contextItem = null;
        contextKind = null;
        stamps = List.of();
        chosen = 0;
        wheel = 0.0;
    }

    private static String targetKey(String var0, ItemStack var1) {
        CustomData var2 = var1.get(DataComponents.CUSTOM_DATA);
        String var3 =
                var2 == null ? "" : CustomDataView.read(var2).getString("EquipmentID").orElse("");
        return var0 + "|" + itemPath(var1) + "|" + var3;
    }

    private static List<ShopComparison.Target> context(
            Player var0, ShopComparison.Kind var1, ItemStack var2) {
        List<ShopComparison.Target> var3 = targets(var0, var1, var2);
        ArrayList var4 = new ArrayList();

        for (ShopComparison.Target var6 : var3) {
            var4.add(var6.key());
        }

        Screen var10 = Minecraft.getInstance().screen;
        boolean var11 =
                var10 != contextScreen
                        || var0 != contextPlayer
                        || var2 != contextItem
                        || var1 != contextKind;
        if (var11) {
            chosen = 0;
            wheel = 0.0;
        } else if (!var4.equals(stamps)) {
            String var7 =
                    stamps.isEmpty() ? null : stamps.get(Math.floorMod(chosen, stamps.size()));
            boolean var8 = new HashSet(var4).equals(new HashSet<>(stamps));
            int var9 = var7 == null ? -1 : var4.indexOf(var7);
            chosen = var8 && var9 >= 0 ? var9 : 0;
            wheel = 0.0;
        }

        contextScreen = var10;
        contextPlayer = var0;
        contextItem = var2;
        contextKind = var1;
        stamps = List.copyOf(var4);
        chosen = var3.isEmpty() ? 0 : Math.floorMod(chosen, var3.size());
        return var3;
    }

    public static boolean scroll(
            Player var0, ShopComparison.Kind var1, ItemStack var2, double var3) {
        if (var3 != 0.0 && var0 != null && var1 != null && isHeld(0L, 0)) {
            List var5 = context(var0, var1, var2);
            if (var5.isEmpty()) {
                return false;
            }

            wheel += var3;
            int var6 = (int) wheel;
            if (var6 != 0) {
                chosen = Math.floorMod(chosen - var6, var5.size());
                wheel -= var6;
            }

            return true;
        } else {
            return false;
        }
    }

    public static int selectedIndex() {
        return chosen;
    }

    static List<ShopComparison.Target> targets(
            Player var0, ShopComparison.Kind var1, ItemStack var2) {
        ArrayList var3 = new ArrayList();
        if (var1.slot != null) {
            ItemStack var12 = var0.getItemBySlot(var1.slot);
            if (isHovered(var12, var2)) {
                return var3;
            }

            if (var1 == ShopComparison.Kind.SHIELD && kind(var12) != ShopComparison.Kind.SHIELD) {
                var12 = ItemStack.EMPTY;
            }

            var3.add(
                    new ShopComparison.Target(
                            shared("att2.ui.equipped"),
                            var12,
                            var1 == ShopComparison.Kind.SHIELD
                                    ? "att2.ui.unequipped_shield"
                                    : "att2.ui.unequipped",
                            targetKey("gear:" + var1.name(), var12)));
            return var3;
        } else {
            Inventory var4 = var0.getInventory();
            int var5 = var4.getSelectedSlot();

            for (int var9 : hotbarOrder(var5)) {
                ItemStack var10 = var4.getItem(var9);
                if (!isHovered(var10, var2) && kind(var10) == var1) {
                    MutableComponent var11 =
                            var9 == var5
                                    ? shared("att2.ui.in_hand")
                                    : shared("att2.ui.hotbar_slot", "slot", var9 + 1);
                    var3.add(
                            new ShopComparison.Target(
                                    var11,
                                    var10,
                                    "att2.ui.unequipped_weapon",
                                    targetKey("hotbar:" + var9, var10)));
                }
            }

            if (var3.isEmpty()) {
                var3.add(
                        new ShopComparison.Target(
                                shared("att2.ui.in_hand"),
                                ItemStack.EMPTY,
                                "att2.ui.unequipped_weapon",
                                "empty:weapon"));
            }

            return var3;
        }
    }

    public static boolean hasComparison(Player var0, ItemStack var1) {
        if (!inShop()) {
            return false;
        }

        ShopComparison.Kind var2 = kind(var1);
        return var2 != null && !targets(var0, var2, var1).isEmpty();
    }

    private static boolean isHovered(ItemStack var0, ItemStack var1) {
        return !var1.isEmpty() && var0 == var1;
    }

    static int[] hotbarOrder(int var0) {
        int[] var1 = new int[9];
        var1[0] = var0;
        int var2 = 0;
        int var3 = 1;

        while (var2 < 9) {
            if (var2 != var0) {
                var1[var3++] = var2;
            }

            var2++;
        }

        return var1;
    }

    static List<Component> targetLines(Component var0, List<Component> var1, String var2) {
        ArrayList var3 = new ArrayList();
        var3.add(var0.copy().withStyle(ChatFormatting.GOLD));
        if (var1 != null && !var1.isEmpty()) {
            var3.addAll(var1);
        } else {
            var3.add(shared(var2));
        }

        return var3;
    }

    static List<Component> wornLines(List<Component> var0) {
        return targetLines(shared("att2.ui.equipped"), var0, "att2.ui.unequipped");
    }

    static int fitCount(int[] var0, int var1) {
        int var2 = 14;
        int var3 = 0;

        for (int var7 : var0) {
            var2 += 10 + var7;
            if (var2 > var1 - 16 && var3 > 0) {
                break;
            }

            var3++;
        }

        return var3;
    }

    public static void render(
            GuiGraphics var0,
            List<Component> var1,
            Player var2,
            ShopComparison.Kind var3,
            ItemStack var4,
            int var5,
            int var6,
            int var7,
            int var8) {
        if (!inShop()) {
            release();
            var0.setTooltipForNextFrame(
                    Minecraft.getInstance().font, var1, Optional.empty(), var5, var6);
        } else {
            List var9 = context(var2, var3, var4);
            if (var9.isEmpty()) {
                var0.setTooltipForNextFrame(
                        Minecraft.getInstance().font, var1, Optional.empty(), var5, var6);
            } else {
                ShopComparison.Target var10 = (ShopComparison.Target) var9.get(chosen);
                Component var11 = var10.header();
                if (var9.size() > 1) {
                    var11 =
                            var11.copy()
                                    .append(
                                            " · "
                                                    + ModLanguageManager.format(
                                                            "shop.compare.cycle",
                                                            "index",
                                                            chosen + 1,
                                                            "total",
                                                            var9.size()));
                }

                render(
                        var0,
                        var1,
                        List.of(new ShopComparison.Target(var11, var10.stack(), var10.emptyKey())),
                        var5,
                        var6,
                        var7,
                        var8);
            }
        }
    }

    static void render(
            GuiGraphics var0,
            List<Component> var1,
            List<ShopComparison.Target> var2,
            int var3,
            int var4,
            int var5,
            int var6) {
        int var7 = Math.max(110, Math.min(240, (var5 - 16 - 24 - 16) / 2));
        ArrayList<List<Component>> var8 = new ArrayList();
        int[] var9 = new int[var2.size()];

        for (int var10 = 0; var10 < var2.size(); var10++) {
            ShopComparison.Target var11 = (ShopComparison.Target) var2.get(var10);
            var8.add(
                    targetLines(
                            var11.header(),
                            var11.stack().isEmpty() ? List.of() : ShopTooltips.of(var11.stack()),
                            var11.emptyKey()));
            var9[var10] = split((List<Component>) var8.get(var10), var7).size() * 10;
        }

        List var25 = split(var1, var7);
        int var26 = fitCount(var9, var6);
        ArrayList<List<FormattedCharSequence>> var12 = new ArrayList();

        for (int var13 = 0; var13 < var26; var13++) {
            List var14 = (List) var8.get(var13);
            if (var13 == var26 - 1 && var26 < var2.size()) {
                var14 = new ArrayList(var14);
                var14.add(
                        shared("att2.ui.compare_more", "count", var2.size() - var26)
                                .withStyle(ChatFormatting.GRAY));
            }

            var12.add(split(var14, var7));
        }

        int var27 = boxWidth(var25);
        int var28 = 0;
        int var15 = -10;

        for (List var17 : var12) {
            var28 = Math.max(var28, boxWidth(var17));
            var15 += var17.size() * 10 + 10;
        }

        int var29 = var27 + 16 + var28 + 24;
        int var30 = Math.max(var25.size() * 10, var15) + 24;
        float var18 = Math.min(1.0F, Math.min((var6 - 16.0F) / var30, (var5 - 16.0F) / var29));
        int var19 = Math.round(var29 * var18);
        int var20 = Math.max(8, Math.min(var3 + 12, var5 - var19 - 8));
        int var21 = Math.max(8, Math.min(var4 - 12, var6 - Math.round(var30 * var18) - 8));
        var0.nextStratum();
        var0.pose().pushMatrix();
        var0.pose().translate(var20 + 12.0F * var18, var21 + 12.0F * var18);
        var0.pose().scale(var18, var18);
        draw(var0, var25, 0, 0, var27);
        int var22 = 0;

        for (List var24 : var12) {
            draw(var0, var24, var27 + 16, var22, boxWidth(var24));
            var22 += var24.size() * 10 + 10;
        }

        var0.pose().popMatrix();
    }

    private static List<FormattedCharSequence> split(List<Component> var0, int var1) {
        Font var2 = Minecraft.getInstance().font;
        ArrayList var3 = new ArrayList();

        for (Component var5 : var0) {
            var3.addAll(var2.split(var5, var1));
        }

        return var3;
    }

    private static int boxWidth(List<FormattedCharSequence> var0) {
        Font var1 = Minecraft.getInstance().font;
        int var2 = 0;

        for (FormattedCharSequence var4 : var0) {
            var2 = Math.max(var2, var1.width(var4));
        }

        return Math.max(1, var2);
    }

    private static void draw(
            GuiGraphics var0, List<FormattedCharSequence> var1, int var2, int var3, int var4) {
        TooltipRenderUtil.renderTooltipBackground(var0, var2, var3, var4, var1.size() * 10, null);

        for (int var5 = 0; var5 < var1.size(); var5++) {
            var0.drawString(
                    Minecraft.getInstance().font,
                    (FormattedCharSequence) var1.get(var5),
                    var2,
                    var3 + var5 * 10,
                    -1,
                    true);
        }
    }

    private static ShopComparison.Kind armorKind(EquipmentSlot var0) {
        if (var0 == null) {
            return null;
        }

        return switch (var0) {
            case HEAD -> ShopComparison.Kind.HEAD;
            case CHEST -> ShopComparison.Kind.CHEST;
            case LEGS -> ShopComparison.Kind.LEGS;
            case FEET -> ShopComparison.Kind.FEET;
            default -> null;
        };
    }

    private static ShopComparison.Kind armorKind(String var0) {
        if (var0 == null) {
            return null;
        }

        return switch (var0) {
            case "HEAD" -> ShopComparison.Kind.HEAD;
            case "CHEST" -> ShopComparison.Kind.CHEST;
            case "LEGS" -> ShopComparison.Kind.LEGS;
            case "FEET" -> ShopComparison.Kind.FEET;
            default -> null;
        };
    }

    private static String itemPath(ItemStack var0) {
        if (var0 != null && !var0.isEmpty()) {
            try {
                return BuiltInRegistries.ITEM.getKey(var0.getItem()).getPath();
            } catch (RuntimeException var2) {
                return "";
            }
        } else {
            return "";
        }
    }

    private static String safe(String var0) {
        return var0 == null ? "" : var0;
    }

    private static String normalize(String var0) {
        StringBuilder var1 = new StringBuilder(var0.length());

        for (int var2 = 0; var2 < var0.length(); var2++) {
            char var3 = Character.toLowerCase(var0.charAt(var2));
            switch (var3) {
                case 'à':
                case 'â':
                case 'ä':
                    var1.append('a');
                    break;
                case 'á':
                case 'ã':
                case 'å':
                case 'æ':
                case 'ì':
                case 'í':
                case 'ð':
                case 'ñ':
                case 'ò':
                case 'ó':
                case 'õ':
                case '÷':
                case 'ø':
                case 'ú':
                default:
                    var1.append(var3);
                    break;
                case 'ç':
                    var1.append('c');
                    break;
                case 'è':
                case 'é':
                case 'ê':
                case 'ë':
                    var1.append('e');
                    break;
                case 'î':
                case 'ï':
                    var1.append('i');
                    break;
                case 'ô':
                case 'ö':
                    var1.append('o');
                    break;
                case 'ù':
                case 'û':
                case 'ü':
                    var1.append('u');
            }
        }

        return var1.toString();
    }

    private static boolean containsAny(String var0, String... var1) {
        for (String var5 : var1) {
            if (var0.contains(var5)) {
                return true;
            }
        }

        return false;
    }

    public enum Kind {
        HEAD(EquipmentSlot.HEAD),
        CHEST(EquipmentSlot.CHEST),
        LEGS(EquipmentSlot.LEGS),
        FEET(EquipmentSlot.FEET),
        SHIELD(EquipmentSlot.OFFHAND),
        MELEE(null),
        RANGED(null);

        final EquipmentSlot slot;

        Kind(EquipmentSlot nullxx) {
            this.slot = nullxx;
        }
    }

    record Target(Component header, ItemStack stack, String emptyKey, String key) {
        Target(Component var1, ItemStack var2, String var3) {
            this(var1, var2, var3, "");
        }
    }
}
