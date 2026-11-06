package fr.poubone.att2.client.miner;

import fr.poubone.att2.client.data.ScoreCache;
import fr.poubone.att2.client.rune.RuneCatalog;
import fr.poubone.att2.client.rune.RunePouchSync1115;
import fr.poubone.att2.client.rune.RuneStock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public final class PrisonerTrade1121 {
    private int pendingTrigger = -1;
    private long requestedAt;
    private long exchangeBefore;
    private String before = "";
    private RuneCatalog.RuneDef purchaseRune;
    private List<RuneCatalog.RuneDef> exchangeRunes = List.of();
    private String status = "";
    private long statusAt;
    private long effectAt;
    private int lastResin = -1;
    private boolean initialized;
    private long growthAt;
    private long bloomAt;
    private float growthFrom;
    private float growthTo;
    private boolean bloomPending;
    private int donationBefore = -1;

    public static long now() {
        return System.nanoTime() / 1000000L;
    }

    public static OptionalInt resin() {
        OptionalInt var0 = MinerShopModel.get().resinCount();
        return var0.isPresent() ? var0 : ScoreCache.readHolder("DARK_RESIN", "Eldric");
    }

    public static LocalPlayer player() {
        Minecraft var0 = Minecraft.getInstance();
        return var0 == null ? null : var0.player;
    }

    public static int loose(RuneCatalog.RuneDef var0) {
        return RuneStock.countInventory(player(), var0);
    }

    public static int pouch(RuneCatalog.RuneDef var0) {
        return RunePouchSync1115.count(var0, player());
    }

    public static long total(RuneCatalog.RuneDef var0) {
        return (long) loose(var0) + pouch(var0);
    }

    public static OptionalInt powder() {
        return RuneStock.powder();
    }

    public static OptionalInt chrono() {
        return ScoreCache.get("CHRONOTON");
    }

    public static int esc() {
        return RuneStock.esc(player());
    }

    private static boolean named(ItemStack var0, String var1) {
        Component var2 = var0.get(DataComponents.CUSTOM_NAME);
        return var2 != null
                && var2.getContents() instanceof TranslatableContents var3
                && var3.getKey().equals(var1);
    }

    public static boolean resinItem(ItemStack var0) {
        return var0 != null
                && !var0.isEmpty()
                && var0.getItem()
                        == BuiltInRegistries.ITEM
                                .getOptional(Identifier.withDefaultNamespace("player_head"))
                                .orElse(null)
                && named(var0, "item.quest.dark_resin.name");
    }

    public static boolean oreItem(ItemStack var0) {
        return var0 != null
                && !var0.isEmpty()
                && var0.getItem()
                        == BuiltInRegistries.ITEM
                                .getOptional(Identifier.withDefaultNamespace("raw_iron"))
                                .orElse(null)
                && named(var0, "item.coin.runic_ore.name");
    }

    public static int carried(boolean var0) {
        LocalPlayer var1 = player();
        if (var1 == null) {
            return 0;
        }

        long var2 = 0L;

        for (int var4 = 0; var4 < var1.getInventory().getContainerSize(); var4++) {
            ItemStack var5 = var1.getInventory().getItem(var4);
            if (var0 ? oreItem(var5) : resinItem(var5)) {
                var2 += var5.getCount();
            }
        }

        return (int) Math.min(2147483647L, var2);
    }

    public static List<ItemStack> oreStacks() {
        LocalPlayer var0 = player();
        if (var0 == null) {
            return List.of();
        }

        ArrayList var1 = new ArrayList();

        for (int var2 = 0; var2 < var0.getInventory().getContainerSize(); var2++) {
            ItemStack var3 = var0.getInventory().getItem(var2);
            if (oreItem(var3)) {
                var1.add(var3);
            }
        }

        return List.copyOf(var1);
    }

    public static LinkedHashMap<RuneCatalog.RuneDef, Integer> requirements(
            MinerCatalog.ExchangeOffer var0) {
        LinkedHashMap<RuneCatalog.RuneDef, Integer> var1 = new LinkedHashMap();

        for (String var3 : var0.ingredients()) {
            Optional var4 = MinerCatalog.runeByName(var3);
            if (var4.isEmpty()) {
                return new LinkedHashMap<>();
            }

            var1.merge((RuneCatalog.RuneDef) var4.get(), 1, Integer::sum);
        }

        return var1;
    }

    public static boolean canExchange(MinerCatalog.ExchangeOffer var0) {
        LinkedHashMap<RuneCatalog.RuneDef, Integer> var1 = requirements(var0);
        return !var1.isEmpty()
                && MinerCatalog.runeByName(var0.result()).isPresent()
                && var1.entrySet().stream()
                        .allMatch(
                                var0x ->
                                        total((RuneCatalog.RuneDef) var0x.getKey())
                                                >= ((Integer) var0x.getValue()).intValue());
    }

    private String fingerprint() {
        if (this.pendingTrigger == 3490) {
            return "r:" + resin().orElse(-1);
        }

        if (this.pendingTrigger == 3536) {
            return "o:" + carried(true) + ":" + powder().orElse(-1);
        }

        if (MinerCatalog.isPowderTrigger(this.pendingTrigger)) {
            return "p:" + powder().orElse(-1) + ":" + esc();
        }

        if (this.purchaseRune != null) {
            return "b:" + chrono().orElse(-1) + ":" + loose(this.purchaseRune);
        }

        StringBuilder var1 = new StringBuilder();

        for (RuneCatalog.RuneDef var3 : this.exchangeRunes) {
            var1.append(var3.id())
                    .append(':')
                    .append(loose(var3))
                    .append(':')
                    .append(pouch(var3))
                    .append(';');
        }

        return var1.toString();
    }

    public void observe() {
        long var1 = now();
        OptionalInt var3 = resin();
        if (var3.isPresent()) {
            int var4 = Math.max(0, var3.getAsInt());
            if (!this.initialized) {
                this.initialized = true;
                this.lastResin = var4;
                this.growthFrom = this.growthTo = Math.min(27, var4);
                this.growthAt = var1;
            } else if (var4 != this.lastResin) {
                float var5 = this.growth();
                this.growthFrom = var5;
                this.growthTo = Math.min(27, var4);
                this.growthAt = var1;
                if (this.pendingTrigger == 3490 && this.lastResin < 27 && var4 >= 27) {
                    this.bloomPending = true;
                }

                if (var4 < 27) {
                    this.bloomPending = false;
                }

                this.lastResin = var4;
            }
        }

        if (this.bloomPending && var1 - this.growthAt >= 700L) {
            this.bloomPending = false;
            this.bloomAt = var1;
        }

        if (this.pendingTrigger >= 0) {
            boolean var6 =
                    this.pendingTrigger == 3490
                            ? var3.isPresent()
                                    && var3.getAsInt() > this.donationBefore
                                    && MinerShopModel.get().prisonerExchangeRevision1121()
                                            > MinerShopModel.get().prisonerResinExchangeBase1121()
                            : !this.fingerprint().equals(this.before);
            if (var1 - this.requestedAt >= 150L && var6) {
                this.pendingTrigger = -1;
                this.status = "miner2.updated";
                this.statusAt = var1;
                this.effectAt = var1;
            } else if (var1 - this.requestedAt >= 5000L) {
                this.pendingTrigger = -1;
                this.status = "miner2.wait_timeout";
                this.statusAt = var1;
            }
        }
    }

    public boolean busy() {
        return this.pendingTrigger >= 0;
    }

    public int progress() {
        return this.lastResin;
    }

    public float growth() {
        if (!this.initialized) {
            return 0.0F;
        }

        float var1 = Math.max(0.0F, Math.min(1.0F, (float) (now() - this.growthAt) / 700.0F));
        return this.growthFrom
                + (this.growthTo - this.growthFrom) * (1.0F - (float) Math.pow(1.0F - var1, 3.0));
    }

    public float bloom() {
        return this.bloomAt == 0L
                ? 0.0F
                : Math.max(0.0F, 1.0F - (float) (now() - this.bloomAt) / 2200.0F);
    }

    public float pulse() {
        return this.effectAt == 0L
                ? 0.0F
                : Math.max(0.0F, 1.0F - (float) (now() - this.effectAt) / 1100.0F);
    }

    public String status() {
        if (this.busy()) {
            return "miner2.pending";
        } else {
            return now() - this.statusAt < 5000L ? this.status : "";
        }
    }

    private void begin(int var1) {
        this.pendingTrigger = var1;
        this.requestedAt = now();
        this.exchangeBefore = MinerShopModel.get().prisonerExchangeRevision1121();
        this.before = this.fingerprint();
        this.status = "miner2.pending";
        this.statusAt = this.requestedAt;
    }

    public boolean buy(MinerCatalog.BuyOffer var1) {
        if (this.busy()) {
            return false;
        } else {
            OptionalInt var2 = resin();
            OptionalInt var3 = MinerShopModel.get().price(var1.trigger());
            OptionalInt var4 = chrono();
            if (!var2.isEmpty()
                    && var2.getAsInt() >= var1.resinRequired()
                    && !var3.isEmpty()
                    && (!var4.isPresent() || var4.getAsInt() >= var3.getAsInt())) {
                this.purchaseRune = var1.rune();
                this.exchangeRunes = List.of();
                this.begin(var1.trigger());
                MinerShopModel.get().send(var1.trigger());
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean sellPowder(MinerCatalog.PowderOffer var1) {
        if (!this.busy() && !powder().isEmpty() && powder().getAsInt() >= var1.powder()) {
            this.purchaseRune = null;
            this.exchangeRunes = List.of();
            this.begin(var1.trigger());
            MinerShopModel.get().send(var1.trigger());
            MinerShopModel.get().send(3488);
            return true;
        } else {
            return false;
        }
    }

    public boolean exchange(MinerCatalog.ExchangeOffer var1) {
        if (!this.busy() && canExchange(var1) && MinerCatalog.isExchangeTrigger(var1.trigger())) {
            this.purchaseRune = null;
            this.exchangeRunes = List.copyOf(requirements(var1).keySet());
            this.begin(var1.trigger());
            MinerShopModel.get().send(var1.trigger());
            return true;
        } else {
            return false;
        }
    }

    public boolean donate() {
        if (!this.busy() && carried(false) >= 1 && !resin().isEmpty()) {
            this.purchaseRune = null;
            this.exchangeRunes = List.of();
            this.donationBefore = resin().orElse(-1);
            this.begin(3490);
            MinerShopModel.get().preparePrisonerRead1121();
            MinerShopModel.get().send(3490);
            MinerShopModel.get().send(3489);
            MinerShopModel.get().send(3535);
            return true;
        } else {
            return false;
        }
    }

    public boolean sellOre() {
        if (!this.busy() && carried(true) >= 1) {
            this.purchaseRune = null;
            this.exchangeRunes = List.of();
            this.begin(3536);
            MinerShopModel.get().send(3536);
            MinerShopModel.get().send(3488);
            return true;
        } else {
            return false;
        }
    }
}
