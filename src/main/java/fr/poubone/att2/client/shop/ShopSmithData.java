package fr.poubone.att2.client.shop;

import fr.poubone.att2.client.data.Att2Triggers;
import fr.poubone.att2.client.renderer.ItemRarity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent.ShowItem;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ShopSmithData {
    public static final String TOOL_CATEGORY = "@smith_tools";
    private static final List<String> RARITIES =
            List.of("com", "unc", "rar", "epi_esc", "epi", "leg", "ult");
    private static final Map<String, String> NAMES =
            Map.of(
                    "com",
                    "common|commun|普通",
                    "unc",
                    "uncommon|peu commun|常见|罕见",
                    "rar",
                    "rare|稀有",
                    "epi_esc",
                    "sovereign|souverain|奇珍",
                    "epi",
                    "epic|epique|épique|史诗",
                    "leg",
                    "legendary|legendaire|légendaire|传说",
                    "ult",
                    "ultimate|supreme|suprême|ultime|终极|至尊");
    private static final Pattern LEVEL =
            Pattern.compile(
                    "(?i)(?:merchant\\s+level|shop\\s+level|niveau(?:\\s+du\\s+marchand)?|level|等级|级别|级数|lv\\.?|lvl\\.?)\\s*[:：=\\-]?\\s*(\\d{1,3})(?!\\d)");
    private static final Pattern REVERSED_LEVEL = Pattern.compile("(\\d{1,3})\\s*级(?:商人|铁匠|锻造)?");
    private static final Pattern DISCOUNT =
            Pattern.compile(
                    "(?i)(?:discount|remise|réduction|reduction|折扣|优惠|减免)[^\\d%\\n"
                        + "]{0,20}([+\\-]?\\d+(?:[.,]\\d+)?)\\s*%");
    private static final Pattern CN_DISCOUNT =
            Pattern.compile("(?:折扣|优惠)[^\\d\\n]{0,10}(\\d+(?:\\.\\d+)?)\\s*折");
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:[.,]\\d+)?");
    private static final LinkedHashMap<Integer, ShopOffer> TOOLS = new LinkedHashMap<>();
    private static final LinkedHashMap<String, String> QUOTES = new LinkedHashMap<>();
    private static String seller = "";
    private static String level;
    private static String discount;
    private static int version;
    private static int ticks;
    private static int window;
    private static int stage;
    private static int quietUntil;
    private static boolean requested;

    private ShopSmithData() {}

    public static int version() {
        return version;
    }

    public static void reset() {
        seller = "";
        level = null;
        discount = null;
        TOOLS.clear();
        QUOTES.clear();
        requested = false;
        stage = 0;
        window = 0;
        ticks = 0;
        quietUntil = 0;
        version++;
    }

    public static void sync(ShopModel var0) {
        String var1 = var0.seller() == null ? "" : var0.seller().npcId();
        if (!seller.equals(var1)) {
            reset();
            seller = var1;
        }
    }

    public static void tick(ShopModel var0) {
        sync(var0);
        ticks++;
        if (window > 0) {
            window--;
        }

        if (var0.hasRepair() && !var0.isCollecting() && ticks >= quietUntil) {
            if (!requested) {
                requested = true;
                stage = 1;
                window = 80;
                quietUntil = ticks + 45;
                Att2Triggers.send(413);
            } else if (stage == 1 && ticks >= quietUntil) {
                stage = 2;
                window = 80;
                quietUntil = ticks + 90;
                Att2Triggers.send(415);
            }
        }
    }

    public static void retry(ShopModel var0) {
        sync(var0);
        requested = false;
        stage = 0;
        window = 0;
        quietUntil = ticks + 2;
        TOOLS.clear();
        QUOTES.clear();
        version++;
    }

    public static List<ShopOffer> tools() {
        return List.copyOf(TOOLS.values());
    }

    public static void retryMissing(ShopModel var0) {
        if (window <= 0 && requested && ticks >= quietUntil) {
            retry(var0);
        }
    }

    public static boolean isTool(ShopOffer var0) {
        return var0 != null && var0.trigger() >= 406 && var0.trigger() <= 412;
    }

    public static String level() {
        return level;
    }

    public static String discount() {
        return discount;
    }

    public static String quote(String var0) {
        return QUOTES.get(normalizeRarity(var0));
    }

    private static String normalizeRarity(String var0) {
        return switch (var0 == null ? "" : var0) {
            case "epi_set" -> "epi";
            case "leg_armset" -> "leg";
            default -> var0;
        };
    }

    public static List<String> quoteTable() {
        ArrayList var0 = new ArrayList();

        for (String var2 : RARITIES) {
            if (QUOTES.containsKey(var2)) {
                var0.add(rarityName(var2) + ": " + QUOTES.get(var2));
            }
        }

        return var0;
    }

    public static String rarityName(String var0) {
        String var1 = "att2.shop.mending.quick_mending." + normalizeRarity(var0);
        String var2 = plain(Component.translatable(var1));
        return var2.equals(var1)
                ? (var0 == null ? "?" : var0)
                : var2.replaceAll("[\\s:：\\-]+$", "");
    }

    private static String plain(Component var0) {
        return var0 == null ? "" : ChatFormatting.stripFormatting(var0.getString());
    }

    static String parseLevel(String var0) {
        Matcher var1 = LEVEL.matcher(var0);
        if (var1.find()) {
            return var1.group(1);
        }

        var1 = REVERSED_LEVEL.matcher(var0);
        return var1.find() ? var1.group(1) : null;
    }

    static String parseDiscount(String var0) {
        Matcher var1 = DISCOUNT.matcher(var0);
        if (var1.find()) {
            return var1.group(1) + "%";
        }

        var1 = CN_DISCOUNT.matcher(var0);
        return var1.find() ? var1.group(1) + "折" : null;
    }

    private static boolean metadata(String var0) {
        String var1 = var0.toLowerCase(Locale.ROOT);
        return var1.contains("discount")
                || var1.contains("remise")
                || var1.contains("réduction")
                || var1.contains("shop level")
                || var1.contains("merchant level")
                || var1.contains("niveau")
                || var0.contains("折扣")
                || var0.contains("优惠")
                || var0.contains("商人等级")
                || var0.contains("铁匠等级")
                || var0.contains("锻造等级");
    }

    static Map<String, String> parseQuotes(String var0) {
        LinkedHashMap var1 = new LinkedHashMap();
        List<String> var2 = List.of("unc", "epi_esc", "leg", "ult", "epi", "rar", "com");
        StringBuilder var3 = new StringBuilder();

        for (String var5 : var2) {
            if (var3.length() > 0) {
                var3.append('|');
            }

            var3.append("(?<")
                    .append(var5.replace('_', 'X'))
                    .append(">(?<![\\p{L}])(?:")
                    .append(NAMES.get(var5))
                    .append(")(?![\\p{L}])\\s*[:：\\-]\\s*)");
        }

        Matcher var10 = Pattern.compile(var3.toString(), 66).matcher(var0);
        ArrayList var11 = new ArrayList();
        ArrayList var6 = new ArrayList();
        ArrayList var7 = new ArrayList();

        while (var10.find()) {
            for (String var9 : var2) {
                if (var10.group(var9.replace('_', 'X')) != null) {
                    var11.add(var9);
                    break;
                }
            }

            var6.add(var10.start());
            var7.add(var10.end());
        }

        for (int var12 = 0; var12 < var11.size(); var12++) {
            String var13 =
                    var0.substring(
                                    (Integer) var7.get(var12),
                                    var12 + 1 < var11.size()
                                            ? (Integer) var6.get(var12 + 1)
                                            : var0.length())
                            .strip()
                            .replaceAll("[\\s|;；]+$", "");
            if (NUMBER.matcher(var13).find() && !var13.contains("?")) {
                var1.put((String) var11.get(var12), var13);
            }
        }

        return var1;
    }

    private static void visit(Component var0, Consumer<Component> var1) {
        if (var0 != null) {
            var1.accept(var0);
            if (var0.getContents() instanceof TranslatableContents var2) {
                for (Object var7 : var2.getArgs()) {
                    if (var7 instanceof Component var3) {
                        visit(var3, var1);
                    }
                }
            }

            for (Component var9 : var0.getSiblings()) {
                visit(var9, var1);
            }
        }
    }

    public static boolean observe(ShopModel var0, Component var1) {
        sync(var0);
        if (var0.seller() == null && !var0.hasRepair()) {
            return false;
        }

        ArrayList<Integer> var2 = new ArrayList();
        visit(
                var1,
                var1x -> {
                    if (var1x.getStyle().getClickEvent() instanceof RunCommand var2x) {
                        int var4x = Att2Triggers.parseTriggerCommand(var2x.command());
                        if (var4x >= 0) {
                            var2.add(var4x);
                        }
                    }
                });
        boolean var3 = var2.stream().anyMatch(var0x -> var0x >= 406 && var0x <= 412);
        String var4 = plain(var1);
        boolean[] var5 = new boolean[] {false};
        visit(
                var1,
                var1x -> {
                    if (var1x.getStyle().getHoverEvent() instanceof ShowItem) {
                        var5[0] = true;
                    }
                });
        boolean var6 =
                var2.isEmpty()
                        && !var5[0]
                        && (metadata(var4) || var4.matches("(?is).*?(?:等级|level|niveau).*"));
        if (var6) {
            String var7 = parseLevel(var4);
            String var8 = parseDiscount(var4);
            if (var7 != null && !var7.equals(level)) {
                level = var7;
                version++;
            }

            if (var8 != null && !var8.equals(discount)) {
                boolean var9 = discount != null && !QUOTES.isEmpty();
                discount = var8;
                version++;
                if (var9) {
                    retry(var0);
                }
            }
        }

        if (var0.hasRepair() && window > 0) {
            if (var3) {
                for (ShopOffer var15 : var0.smithParseTools(var1)) {
                    if (isTool(var15)) {
                        ShopOffer var17 =
                                new ShopOffer(
                                        var15.stack(),
                                        var15.name(),
                                        var15.price(),
                                        var15.trigger(),
                                        "@smith_tools",
                                        var15.translationKey(),
                                        var15.sprite(),
                                        var15.lore());
                        if (ShopOriginalItems.contains(var15)) {
                            ShopOriginalItems.mark(var17);
                        }

                        ShopOffer var10 = TOOLS.put(var17.trigger(), var17);
                        if (!var17.equals(var10)) {
                            version++;
                        }
                    }
                }

                return true;
            } else {
                if (var2.isEmpty()) {
                    Map<String, String> var11 = parseQuotes(var4);
                    if (!var11.isEmpty()) {
                        for (var var16 : var11.entrySet()) {
                            if (!((String) var16.getValue())
                                    .equals(
                                            QUOTES.put(
                                                    (String) var16.getKey(),
                                                    (String) var16.getValue()))) {
                                version++;
                            }
                        }

                        return true;
                    }

                    String var13 = var4.toLowerCase(Locale.ROOT);
                    if (var4.contains("修理价格")
                            || var4.contains("维修价格")
                            || var13.contains("repair prices")
                            || var13.contains("prix de réparation")
                            || var13.contains("prix des outils")
                            || var4.contains("工具价格")) {
                        return true;
                    }
                }

                return false;
            }
        } else {
            return false;
        }
    }

    public static String heldQuote(ItemStack var0) {
        if (var0 != null && !var0.isEmpty()) {
            ItemRarity var1 = ItemRarity.fromStack(var0);
            return var1 == null ? null : quote(var1.id);
        } else {
            return null;
        }
    }
}
