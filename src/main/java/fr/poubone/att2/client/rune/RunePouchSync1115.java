package fr.poubone.att2.client.rune;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.data.Att2Triggers;
import fr.poubone.att2.client.data.MapReady;
import fr.poubone.att2.client.data.ScoreCache;
import fr.poubone.att2.client.miner.MinerShopScreen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.scores.Scoreboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RunePouchSync1115 {
    public static final int QUERY_TRIGGER = 3608;
    public static final int RESPONSE_TIMEOUT_TICKS = 60;
    public static final long BACKPACK_COOLDOWN_MILLIS = 10000L;
    private static final Pattern INTEGER = Pattern.compile("-?\\d+");
    private static final Pattern DEPOSIT =
            Pattern.compile("\\b([A-Za-z]{2,3})\\s*\\[\\+\\d+\\s*/\\s*(-?\\d+)\\]");
    private static final Map<String, RunePouchSync1115.Entry> VALUES = new HashMap<>();
    private static final Set<String> seenRunes = new HashSet<>();
    private static final Set<String> seenBonuses = new HashSet<>();
    private static Map<Integer, ItemLore> pouchViews = new HashMap<>();
    private static Object world;
    private static Object playerScope;
    private static long clock;
    private static long deadline;
    private static long queuedAt;
    private static long lastSentAtMillis;
    private static boolean inventoryWasOpen;
    private static boolean hasSent;
    private static boolean queued;
    private static boolean awaiting;

    private RunePouchSync1115() {}

    public static void reset() {
        if (queued) {
            Att2Triggers.cancel(3608);
        }

        world = null;
        playerScope = null;
        clock = 0L;
        lastSentAtMillis = 0L;
        deadline = 0L;
        queuedAt = 0L;
        hasSent = false;
        inventoryWasOpen = false;
        awaiting = false;
        queued = false;
        VALUES.clear();
        seenRunes.clear();
        seenBonuses.clear();
        pouchViews.clear();
    }

    public static void onQueueReset() {
        awaiting = false;
        queued = false;
        deadline = 0L;
        seenRunes.clear();
        seenBonuses.clear();
    }

    private static boolean scope(Minecraft var0) {
        if (var0 != null
                && var0.player != null
                && var0.level != null
                && !FlashbackCompat.isInReplay()) {
            if (world != var0.level || playerScope != var0.player) {
                reset();
                world = var0.level;
                playerScope = var0.player;
            }

            return true;
        } else {
            if (world != null || queued || awaiting) {
                reset();
            }

            return false;
        }
    }

    private static RunePouchSync1115.Entry entry(String var0) {
        return VALUES.computeIfAbsent(var0, var0x -> new RunePouchSync1115.Entry());
    }

    private static void confirm(RuneCatalog.RuneDef var0, int var1) {
        RunePouchSync1115.Entry var2 = entry(var0.id());
        var2.value = Math.max(0, var1);
        var2.live = ScoreCache.readLive(var0.scoreObjective());
        var2.observed = true;
        ScoreCache.put(var0.scoreObjective(), var2.value);
    }

    private static long nowMillis() {
        return System.nanoTime() / 1000000L;
    }

    private static boolean ownInventory(Screen var0) {
        return var0 instanceof InventoryScreen || var0 instanceof CreativeModeInventoryScreen;
    }

    private static boolean hasRuneTracker() {
        RuneWorkshopRecipes119.Recipe var0 = RecipeTracking119.tracked();
        return var0 != null
                && var0.rows().stream()
                        .anyMatch(var0x -> RunePouchStock1113.isRune(var0x.material()));
    }

    private static boolean mapContext(Minecraft var0) {
        if (!MapReady.isReady() && VALUES.isEmpty() && !hasRuneTracker()) {
            Scoreboard var1 = var0.level.getScoreboard();
            if (var1.getObjective("RUNE") == null && var1.getObjective("RUNE_GAL") == null) {
                for (int var2 = 0; var2 < var0.player.getInventory().getContainerSize(); var2++) {
                    ItemStack var3 = var0.player.getInventory().getItem(var2);
                    if (var3 != null && !var3.isEmpty()) {
                        CustomData var4 = var3.get(DataComponents.CUSTOM_DATA);
                        String var5 =
                                var4 == null
                                        ? ""
                                        : var4.copyTag().getString("EquipmentType").orElse("");
                        if (var5.equals("rune") || var5.equals("rune_bundle")) {
                            return true;
                        }
                    }
                }

                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static void requestFromScreen(int var0) {
        if (var0 != 3608) {
            Att2Triggers.send(var0);
        } else if (scope(Minecraft.getInstance())) {
            if (!queued && !awaiting) {
                queueNow();
            }
        }
    }

    private static void queueNow() {
        queued = true;
        queuedAt = clock;
        Att2Triggers.send(3608);
    }

    public static void onTriggerSent(int var0) {
        if (var0 == 3608 && queued) {
            if (scope(Minecraft.getInstance())) {
                queued = false;
                awaiting = true;
                hasSent = true;
                lastSentAtMillis = nowMillis();
                deadline = clock + 60L;
                seenRunes.clear();
                seenBonuses.clear();
            }
        }
    }

    public static void tick() {
        Minecraft var0 = Minecraft.getInstance();
        if (scope(var0)) {
            boolean var1 = ownInventory(var0.screen);
            boolean var2 = var1 && !inventoryWasOpen;
            inventoryWasOpen = var1;
            if (!Att2Triggers.isGameplayPaused(var0)) {
                clock++;
                if (awaiting && clock >= deadline) {
                    awaiting = false;
                    seenRunes.clear();
                    seenBonuses.clear();
                }

                if (queued && clock - queuedAt >= 200L) {
                    Att2Triggers.cancel(3608);
                    queued = false;
                }

                if (var2
                        && RecipeTracking119.tracked() != null
                        && mapContext(var0)
                        && !queued
                        && !awaiting
                        && (!hasSent || nowMillis() - lastSentAtMillis >= 10000L)) {
                    queueNow();
                }

                if (clock % 5L == 0L
                        && (var1 || var0.screen instanceof RuneCodexScreen || hasRuneTracker())) {
                    observeInventory(var0.player);
                }
            }
        }
    }

    private static void observeInventory(LocalPlayer var0) {
        HashMap var1 = new HashMap();
        HashSet var2 = new HashSet<>(pouchViews.values());

        for (int var3 = 0; var3 < var0.getInventory().getContainerSize(); var3++) {
            ItemStack var4 = var0.getInventory().getItem(var3);
            if (var4 != null && !var4.isEmpty() && RuneStock.isBundle(var4)) {
                ItemLore var5 = var4.get(DataComponents.LORE);
                var1.put(var3, var5);
                ItemLore var6 = pouchViews.get(var3);
                if (var6 != null
                        && var5 != null
                        && !Objects.equals(var6, var5)
                        && !var2.contains(var5)) {
                    for (RuneCatalog.RuneDef var8 : RuneCatalog.allRunes()) {
                        OptionalInt var9;
                        try {
                            var9 = RuneStock.parseBundleLore(var4, var8);
                        } catch (RuntimeException var11) {
                            var9 = OptionalInt.empty();
                        }

                        if (var9.isPresent()) {
                            confirm(var8, var9.getAsInt());
                        } else if (var5.lines().stream()
                                .noneMatch(
                                        var1x ->
                                                containsKey(
                                                        var1x,
                                                        "att2.rune_bundle.total." + var8.id()))) {
                            confirm(var8, 0);
                        }
                    }
                }
            }
        }

        pouchViews = var1;
    }

    public static int count(RuneCatalog.RuneDef var0, LocalPlayer var1) {
        Minecraft var2 = Minecraft.getInstance();
        if (var1 != null && scope(var2) && var2.player == var1) {
            RunePouchSync1115.Entry var3 = entry(var0.id());
            OptionalInt var4 = ScoreCache.readLive(var0.scoreObjective());
            if (var3.observed && var3.live.isPresent()) {
                if (var4.isPresent() && var4.getAsInt() != var3.live.getAsInt()) {
                    var3.value = Math.max(0, var4.getAsInt());
                } else if (var4.isEmpty()
                        && var2.level.getScoreboard().getObjective(var0.scoreObjective()) != null) {
                    var3.value = 0;
                }
            }

            var3.live = var4;
            var3.observed = true;
            if (var3.value != null) {
                return var3.value;
            }

            if (var4.isPresent()) {
                return Math.max(0, var4.getAsInt());
            }

            OptionalInt var5;
            try {
                var5 = RuneStock.loreFallback(var1, var0);
            } catch (RuntimeException var7) {
                var5 = OptionalInt.empty();
            }

            return var5.isPresent()
                    ? Math.max(0, var5.getAsInt())
                    : Math.max(0, ScoreCache.get(var0.scoreObjective()).orElse(0));
        } else {
            return 0;
        }
    }

    public static boolean onMessage(Component var0) {
        try {
            return handleMessage(var0);
        } catch (RuntimeException var2) {
            return false;
        }
    }

    private static boolean handleMessage(Component var0) {
        if (var0 != null && scope(Minecraft.getInstance())) {
            TranslatableContents var1 = findTotal(var0);
            if (var1 != null) {
                String var6 = var1.getKey().substring("att2.rune_bundle.total.".length());
                Optional var8 = RuneCatalog.byId(var6);
                if (var8.isEmpty()) {
                    return false;
                }

                OptionalInt var10 = argument(var1);
                if (var10.isPresent()) {
                    confirm((RuneCatalog.RuneDef) var8.get(), var10.getAsInt());
                }

                ScoreCache.putHolder(
                        "RUNE",
                        ((RuneCatalog.RuneDef) var8.get()).unlockHolder(),
                        var0.getString().contains("✔") ? 1 : 0);
                if (awaiting) {
                    seenRunes.add(var6);
                    deadline = clock + 20L;
                    return true;
                } else {
                    return false;
                }
            } else if (containsKey(var0, "att2.rune_bundle.add")) {
                Matcher var5 = DEPOSIT.matcher(ChatFormatting.stripFormatting(var0.getString()));
                if (var5.find()) {
                    RuneCatalog.byId(var5.group(1).toLowerCase(Locale.ROOT))
                            .ifPresent(
                                    var1x -> {
                                        OptionalInt var2x = parse(var5.group(2));
                                        if (var2x.isPresent()) {
                                            confirm(var1x, var2x.getAsInt());
                                        }
                                    });
                    return false;
                } else if (awaiting && isSummaryHeader(var0)) {
                    deadline = clock + 20L;
                    return true;
                } else {
                    return false;
                }
            } else {
                if (awaiting && seenRunes.contains("mot")) {
                    String var2 =
                            var0.getContents() instanceof PlainTextContents var3 ? var3.text() : "";
                    if (RuneBonusText1115.HEADERS.contains(var2)) {
                        deadline = clock + 20L;
                        return true;
                    }

                    String var7 = RuneBonusText1115.HOLDERS.get(var2);
                    if (var7 != null) {
                        OptionalInt var9 = extraNumber(var0);
                        if (var9.isPresent()) {
                            ScoreCache.putHolder("RUNE", var7, var9.getAsInt());
                        }

                        seenBonuses.add(var7);
                        deadline = clock + 20L;
                        if (seenBonuses.size() == 8) {
                            awaiting = false;
                        }

                        return true;
                    }
                }

                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean isSummaryHeader(Component var0) {
        if (var0.getContents() instanceof TranslatableContents var1
                && var1.getKey().equals("att2.rune_bundle.add")
                && var1.getArgs().length == 0
                && var0.getSiblings().isEmpty()) {
            return true;
        } else {
            return var0.getContents() instanceof PlainTextContents var3
                            && var3.text().isBlank()
                            && var0.getSiblings().size() == 1
                    ? isSummaryHeader(var0.getSiblings().get(0))
                    : false;
        }
    }

    private static TranslatableContents findTotal(Component var0) {
        if (var0.getContents() instanceof TranslatableContents var1) {
            if (var1.getKey().startsWith("att2.rune_bundle.total.")) {
                return var1;
            }

            for (Object var5 : var1.getArgs()) {
                if (var5 instanceof Component var6) {
                    TranslatableContents var7 = findTotal(var6);
                    if (var7 != null) {
                        return var7;
                    }
                }
            }
        }

        for (Component var10 : var0.getSiblings()) {
            TranslatableContents var11 = findTotal(var10);
            if (var11 != null) {
                return var11;
            }
        }

        return null;
    }

    private static boolean containsKey(Component var0, String var1) {
        if (var0.getContents() instanceof TranslatableContents var2) {
            if (var2.getKey().equals(var1)) {
                return true;
            }

            for (Object var6 : var2.getArgs()) {
                if (var6 instanceof Component var7 && containsKey(var7, var1)) {
                    return true;
                }
            }
        }

        for (Component var10 : var0.getSiblings()) {
            if (containsKey(var10, var1)) {
                return true;
            }
        }

        return false;
    }

    private static OptionalInt argument(TranslatableContents var0) {
        if (var0.getArgs().length == 0) {
            return OptionalInt.empty();
        }

        Object var1 = var0.getArgs()[0];
        return var1 instanceof Component var2 && var2.getContents() instanceof ScoreContents
                ? OptionalInt.empty()
                : parse(var1 instanceof Component var3 ? var3.getString() : String.valueOf(var1));
    }

    private static OptionalInt parse(String var0) {
        Matcher var1 = INTEGER.matcher(var0);
        if (!var1.find()) {
            return OptionalInt.empty();
        }

        try {
            long var2 = Long.parseLong(var1.group());
            return OptionalInt.of((int) Math.max(-2147483648L, Math.min(2147483647L, var2)));
        } catch (NumberFormatException var4) {
            return OptionalInt.empty();
        }
    }

    private static OptionalInt extraNumber(Component var0) {
        for (Component var2 : var0.getSiblings()) {
            if (var2.getContents() instanceof PlainTextContents var3) {
                OptionalInt var6 = parse(var3.text());
                if (var6.isPresent()) {
                    return var6;
                }
            }

            OptionalInt var5 = extraNumber(var2);
            if (var5.isPresent()) {
                return var5;
            }
        }

        return OptionalInt.empty();
    }

    public static void observePrisonerInventory1121() {
        Minecraft var0 = Minecraft.getInstance();
        if (var0 != null && var0.screen instanceof MinerShopScreen && scope(var0)) {
            observeInventory(var0.player);
        }
    }

    private static final class Entry {
        Integer value;
        OptionalInt live = OptionalInt.empty();
        boolean observed;
    }
}
