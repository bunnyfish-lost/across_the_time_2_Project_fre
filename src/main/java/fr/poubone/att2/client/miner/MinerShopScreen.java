package fr.poubone.att2.client.miner;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.rune.RuneCatalog;
import fr.poubone.att2.client.rune.RuneItems;
import fr.poubone.att2.client.rune.RunePouchSync1115;
import fr.poubone.att2.client.screen.GuiBackdrop1132;
import fr.poubone.att2.client.shop.MenuScale207;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class MinerShopScreen extends Screen {
    private static final int BG = -15724262;
    private static final int PANEL = -15132123;
    private static final int LINE = -12041903;
    private static final int INK = -1843223;
    private static final int MUTED = -5527622;
    private static final int ACCENT = -4741927;
    private static final int GOLD = -2769528;
    private static final MinerCatalog.Tab[] TABS =
            new MinerCatalog.Tab[] {
                MinerCatalog.Tab.BUY,
                MinerCatalog.Tab.EXCHANGE,
                MinerCatalog.Tab.RESIN,
                MinerCatalog.Tab.POWDER,
                MinerCatalog.Tab.ORE
            };
    private static final SoundEvent OPEN_SOUND =
            SoundEvent.createVariableRangeEvent(
                    Identifier.fromNamespaceAndPath("att2", "ui.book.open"));
    private final List<MinerShopScreen.Hit> hits = new ArrayList<>();
    private final PrisonerTrade1121 trade = new PrisonerTrade1121();
    private final ItemStack[] runes =
            MinerCatalog.buys().stream()
                    .map(var0 -> RuneItems.rune(var0.rune()))
                    .toArray(ItemStack[]::new);
    private final ItemStack chrono = RuneItems.chronoton();
    private final ItemStack powder = RuneItems.powder();
    private final ItemStack esc = RuneItems.esc();
    private final ItemStack resin = officialResin();
    private final ItemStack ore = RuneItems.runicOre();
    private MinerCatalog.Tab tab;
    private int x;
    private int y;
    private int w;
    private int h;
    private int headerH;
    private int navH;
    private int bx;
    private int by;
    private int bw;
    private int bh;
    private int footerY;
    private int runeOffset;
    private int recipeOffset;
    private int selectedRune;
    private int selectedRecipe;
    private int batch = 1;
    private int focusIndex = -1;
    private long lastClick;
    private boolean sessionOpened;
    private static long closedUntil;
    private int mouseX;
    private int mouseY;
    private int localTicks;

    public static boolean suppressLateOpen1121() {
        return PrisonerTrade1121.now() < closedUntil;
    }

    public static void allowUserReopen1121() {
        closedUntil = 0L;
    }

    private MinerShopScreen(MinerCatalog.Tab var1) {
        super(ModLanguageManager.get("miner2.title"));
        this.tab = normalize(var1);
    }

    private static MinerCatalog.Tab normalize(MinerCatalog.Tab var0) {
        return var0 == null
                ? MinerCatalog.Tab.BUY
                : (var0 == MinerCatalog.Tab.ACTIONS ? MinerCatalog.Tab.RESIN : var0);
    }

    public static void open(MinerCatalog.Tab var0) {
        if (!FlashbackCompat.isInReplay()
                && HUDConfig.get().minerMenuEnabled
                && !suppressLateOpen1121()) {
            Minecraft var1 = Minecraft.getInstance();
            if (!(var1.screen instanceof MinerShopScreen)) {
                if (var1.screen == null || var1.screen instanceof ChatScreen) {
                    var1.setScreen(new MinerShopScreen(var0));
                    var1.getSoundManager().play(SimpleSoundInstance.forUI(OPEN_SOUND, 1.0F));
                }
            }
        }
    }

    public void switchTab(MinerCatalog.Tab var1) {
        MinerCatalog.Tab var2 = this.tab;
        this.tab = normalize(var1);
        this.focusIndex = -1;
        this.hits.clear();
        if (this.tab == MinerCatalog.Tab.EXCHANGE
                && var2 != this.tab
                && MinerShopModel.get().exchanges().isEmpty()
                && PrisonerTrade1121.resin().orElse(0) > 0
                && !this.trade.busy()) {
            MinerShopModel.get().requestExchangeRefresh();
        }
    }

    public MinerCatalog.Tab tab() {
        return this.tab;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        MenuScale207.prepare(this);
        this.layout();
        this.hits.clear();
        this.focusIndex = -1;
        if (!this.sessionOpened) {
            this.sessionOpened = true;
            MinerShopModel.get().onScreenOpened();
        }

        this.trade.observe();
    }

    private void layout() {
        this.w = Math.max(1, Math.min(900, this.width - 20));
        this.h = Math.max(1, Math.min(520, this.height - 20));
        this.x = (this.width - this.w) / 2;
        this.y = (this.height - this.h) / 2;
        this.headerH = this.h < 240 ? 27 : 38;
        this.navH = 24;
        this.bx = this.x + 10;
        this.bw = Math.max(1, this.w - 20);
        this.by = this.y + this.headerH + this.navH + 10;
        this.footerY = this.y + this.h - 18;
        this.bh = Math.max(1, this.footerY - this.by - 7);
    }

    @Override
    public void removed() {
        MenuScale207.reset(this);
        closedUntil = PrisonerTrade1121.now() + 1000L;
        MinerShopModel.get().onScreenClosed();
        super.removed();
    }

    @Override
    public void tick() {
        if (++this.localTicks % 5 == 0) {
            RunePouchSync1115.observePrisonerInventory1121();
        }

        this.trade.observe();
    }

    @Override
    public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
        var2 = MenuScale207.coordinate(this, var2);
        var3 = MenuScale207.coordinate(this, var3);
        MenuScale207.begin(this, var1);
        MenuScale207.end(var1);
    }

    private String tr(String var1) {
        return ModLanguageManager.getString("miner2." + var1);
    }

    private String fmt(String var1, String var2, Object var3) {
        return ModLanguageManager.format("miner2." + var1, var2, var3);
    }

    private String num(OptionalInt var1) {
        return var1.isPresent() ? String.format(Locale.ROOT, "%,d", var1.getAsInt()) : "...";
    }

    private String num(long var1) {
        return String.format(Locale.ROOT, "%,d", var1);
    }

    private String runeName(RuneCatalog.RuneDef var1) {
        String var2 = var1.id();
        return var2.substring(0, 1).toUpperCase(Locale.ROOT) + var2.substring(1);
    }

    private static ItemStack officialResin() {
        ItemStack var0 = RuneItems.darkResin();
        PropertyMap var1 =
                new PropertyMap(
                        ImmutableMultimap.of(
                                "textures",
                                new Property(
                                        "textures",
                                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTljNDBhZjdkOGVmZmEzYjQ5ZDIzYWJiYTJkZGE5YmRhMzQ1YzliZDU4YjhmMThjOGIyZTZjMDc1ZjRhNDQyNSJ9fX0=")));
        GameProfile var2 =
                new GameProfile(UUID.fromString("24803d8c-7a4e-4c19-9ade-d7dc68752a05"), "", var1);
        var0.set(DataComponents.PROFILE, ResolvableProfile.createResolved(var2));
        return var0;
    }

    private ItemStack resinIcon() {
        LocalPlayer var1 = PrisonerTrade1121.player();
        if (var1 != null) {
            for (int var2 = 0; var2 < var1.getInventory().getContainerSize(); var2++) {
                ItemStack var3 = var1.getInventory().getItem(var2);
                if (PrisonerTrade1121.resinItem(var3) && var3.get(DataComponents.PROFILE) != null) {
                    return var3;
                }
            }
        }

        return this.resin;
    }

    private void text(
            GuiGraphics var1, String var2, int var3, int var4, int var5, int var6, float var7) {
        if (var5 >= 1) {
            var2 = var2 == null ? "" : var2;
            float var8 = Math.min(var7, (float) var5 / Math.max(1, this.font.width(var2)));
            var1.pose().pushMatrix();
            var1.pose().translate(var3, var4);
            var1.pose().scale(var8, var8);
            var1.drawString(this.font, var2, 0, 0, var6, false);
            var1.pose().popMatrix();
        }
    }

    private void center(
            GuiGraphics var1, String var2, int var3, int var4, int var5, int var6, float var7) {
        float var8 = Math.min(var7, (float) var5 / Math.max(1, this.font.width(var2)));
        this.text(
                var1,
                var2,
                var3 + Math.round((var5 - this.font.width(var2) * var8) / 2.0F),
                var4,
                var5,
                var6,
                var8);
    }

    private void rule(GuiGraphics var1, int var2, int var3, int var4) {
        if (var4 > 0) {
            GuiBackdrop1132.fill(var1, var2, var3, var2 + var4, var3 + 1, -12041903, this);
        }
    }

    private void panel(GuiGraphics var1, int var2, int var3, int var4, int var5) {
        if (var4 >= 1 && var5 >= 1) {
            GuiBackdrop1132.fill(var1, var2, var3, var2 + var4, var3 + var5, -15132123, this);
            PrisonerArt1121.frame(var1, var2, var3, var4, var5, -12041903);
        }
    }

    private void hit(
            String var1,
            int var2,
            int var3,
            int var4,
            int var5,
            boolean var6,
            Runnable var7,
            List<Component> var8) {
        if (var4 > 0 && var5 > 0) {
            this.hits.add(new MinerShopScreen.Hit(var1, var2, var3, var4, var5, var6, var7, var8));
        }
    }

    private List<Component> tips(String... var1) {
        return Arrays.stream(var1)
                .filter(var0 -> !var0.isEmpty())
                .<Component>map(Component::literal)
                .toList();
    }

    private boolean hover(int var1, int var2, int var3, int var4) {
        return this.mouseX >= var1
                && this.mouseX < var1 + var3
                && this.mouseY >= var2
                && this.mouseY < var2 + var4;
    }

    private void button(
            GuiGraphics var1,
            String var2,
            String var3,
            int var4,
            int var5,
            int var6,
            int var7,
            boolean var8,
            Runnable var9,
            List<Component> var10) {
        if (var6 >= 1 && var7 >= 1) {
            GuiBackdrop1132.fill(
                    var1,
                    var4,
                    var5,
                    var4 + var6,
                    var5 + var7,
                    var8 ? (this.hover(var4, var5, var6, var7) ? -12570295 : -13621191) : -14540501,
                    this);
            PrisonerArt1121.frame(var1, var4, var5, var6, var7, var8 ? -7637348 : -12041903);
            this.center(
                    var1,
                    var3,
                    var4 + 4,
                    var5 + (var7 - 9) / 2,
                    var6 - 8,
                    var8 ? -1843223 : -5527622,
                    1.0F);
            this.hit(var2, var4, var5, var6, var7, var8, var9, var10);
        }
    }

    private void heading(GuiGraphics var1, String var2, String var3) {
        this.heading(var1, var2, var3, this.bw);
    }

    private void heading(GuiGraphics var1, String var2, String var3, int var4) {
        this.text(var1, var2, this.bx, this.by, var4, -1843223, 1.3F);
        if (this.bh >= 130 && !var3.isEmpty()) {
            this.text(var1, var3, this.bx, this.by + 17, var4, -5527622, 0.9F);
        }
    }

    private int contentY() {
        return this.by + (this.bh >= 130 ? 35 : 17);
    }

    private int contentH() {
        return Math.max(1, this.by + this.bh - this.contentY());
    }

    private void header(GuiGraphics var1) {
        GuiBackdrop1132.fill(
                var1, this.x, this.y, this.x + this.w, this.y + this.headerH, -15000791, this);
        int var2 = this.headerH < 30 ? 15 : 23;
        PrisonerArt1121.icon(
                var1,
                PrisonerArt1121.TRADE,
                this.x + 10,
                this.y + (this.headerH - var2) / 2,
                var2,
                255);
        this.text(
                var1,
                this.tr("title"),
                this.x + var2 + 17,
                this.y + 7,
                Math.max(65, this.w / 3 - var2 - 22),
                -1843223,
                this.headerH < 30 ? 1.1F : 1.5F);
        if (this.headerH >= 30) {
            this.text(var1, "ELDRIC", this.x + var2 + 17, this.y + 24, 90, -5527622, 0.8F);
        }

        int var3 = this.x + Math.max(110, this.w * 43 / 100);
        int var4 = Math.max(1, this.x + this.w - 32 - var3);
        int var5 = var4 / 3;
        ItemStack[] var6 = new ItemStack[] {this.chrono, this.powder, this.esc};
        String[] var7 =
                new String[] {
                    this.num(PrisonerTrade1121.chrono()),
                    this.num(PrisonerTrade1121.powder()),
                    this.num(PrisonerTrade1121.esc())
                };
        String[] var8 = new String[] {this.tr("chrono"), this.tr("powder"), this.tr("esc")};

        for (int var9 = 0; var9 < 3; var9++) {
            int var10 = var3 + var9 * var5;
            int var11 = this.y + (this.headerH - 12) / 2;
            PrisonerArt1121.item(var1, var6[var9], var10, var11, 12);
            this.text(
                    var1,
                    var7[var9],
                    var10 + 16,
                    var11 + 2,
                    Math.max(1, var5 - 20),
                    -2769528,
                    1.0F);
            this.hit(
                    "wallet" + var9,
                    var10,
                    var11,
                    var5,
                    14,
                    false,
                    () -> {},
                    this.tips(var8[var9] + ": " + var7[var9]));
        }

        this.button(
                var1,
                "close",
                "×",
                this.x + this.w - 23,
                this.y + 7,
                15,
                15,
                true,
                this::onClose,
                this.tips(this.tr("close")));
        byte var18 = 5;
        boolean var19 = true;
        int var20 = this.navH;
        int var12 = (this.w - 16) / var18;

        for (int var13 = 0; var13 < TABS.length; var13++) {
            MinerCatalog.Tab var14 = TABS[var13];
            int var15 = this.x + 8 + var13 % var18 * var12;
            int var16 = this.y + this.headerH + var13 / var18 * var20;
            boolean var17 = var14 == this.tab;
            if (var17) {
                GuiBackdrop1132.fill(
                        var1, var15, var16, var15 + var12, var16 + var20, -14409681, this);
                GuiBackdrop1132.fill(
                        var1,
                        var15,
                        var16 + var20 - 2,
                        var15 + var12,
                        var16 + var20,
                        -4741927,
                        this);
            }

            this.center(
                    var1,
                    this.tr("tab." + var14.name().toLowerCase(Locale.ROOT)),
                    var15 + 3,
                    var16 + (var20 - 9) / 2,
                    var12 - 6,
                    var17 ? -1843223 : -5527622,
                    0.98F);
            this.hit(
                    "tab:" + var14,
                    var15,
                    var16,
                    var12,
                    var20,
                    true,
                    () -> this.switchTab(var14),
                    List.of());
        }
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        var2 = MenuScale207.coordinate(this, var2);
        var3 = MenuScale207.coordinate(this, var3);
        MenuScale207.begin(this, var1);
        this.layout();
        this.mouseX = var2;
        this.mouseY = var3;
        this.trade.observe();
        this.hits.clear();
        GuiBackdrop1132.fill(var1, 0, 0, this.width, this.height, -1727000808, this);
        GuiBackdrop1132.fill(
                var1, this.x, this.y, this.x + this.w, this.y + this.h, -15724262, this);
        PrisonerArt1121.frame(var1, this.x, this.y, this.w, this.h, -10331796);
        this.header(var1);
        var1.enableScissor(
                Math.max(this.x + 1, this.bx - 4),
                Math.max(this.y + this.headerH + this.navH + 2, this.by - 6),
                Math.min(this.x + this.w - 1, this.bx + this.bw + 4),
                this.by + this.bh + 2);

        try {
            switch (this.tab) {
                case BUY:
                    this.buyPage(var1);
                    break;
                case POWDER:
                    this.powderPage(var1);
                    break;
                case EXCHANGE:
                    this.exchangePage(var1);
                    break;
                case RESIN:
                case ACTIONS:
                    this.resinPage(var1);
                    break;
                case ORE:
                    this.orePage(var1);
            }
        } finally {
            var1.disableScissor();
        }

        this.rule(var1, this.x + 1, this.footerY - 3, this.w - 2);
        String var5 = this.trade.status();
        this.text(
                var1,
                var5.isEmpty() ? this.tr("footer") : ModLanguageManager.getString(var5),
                this.x + 10,
                this.footerY + 3,
                this.w - 20,
                var5.isEmpty() ? -5527622 : -4202503,
                0.85F);
        if (this.focusIndex >= 0 && this.focusIndex < this.hits.size()) {
            MinerShopScreen.Hit var6 = this.hits.get(this.focusIndex);
            PrisonerArt1121.frame(var1, var6.x - 1, var6.y - 1, var6.w + 2, var6.h + 2, -4741927);
        }

        for (int var12 = this.hits.size() - 1; var12 >= 0; var12--) {
            MinerShopScreen.Hit var7 = this.hits.get(var12);
            if (this.hover(var7.x, var7.y, var7.w, var7.h) && !var7.tip.isEmpty()) {
                MenuScale207.tooltipSimple(
                        var1, this.font, var7.tip, Optional.empty(), var2, var3, this);
                break;
            }
        }

        MenuScale207.end(var1);
    }

    private int detailWidth() {
        return this.bw < 420 ? Math.max(92, this.bw / 3) : Math.min(215, this.bw / 3);
    }

    private int buyColumns() {
        return Math.max(3, Math.min(7, (this.bw - this.detailWidth() - 10) / 40));
    }

    private int buyRows() {
        return Math.max(1, (this.contentH() - 17) / (this.contentH() < 150 ? 32 : 44));
    }

    private int buyVisible() {
        return Math.min(27, this.buyColumns() * this.buyRows());
    }

    private int buyTileHeight() {
        int var1 = this.buyVisible();
        int var2 = var1 < 27 ? this.buyRows() : (27 + this.buyColumns() - 1) / this.buyColumns();
        return Math.max(24, (this.contentH() - (var1 < 27 ? 17 : 2)) / var2);
    }

    private void buyPage(GuiGraphics var1) {
        int var2 = this.contentY();
        int var3 = this.contentH();
        int var4 = this.detailWidth();
        int var5 = this.bw - var4 - 10;
        int var6 = this.buyColumns();
        int var7 = Math.max(1, var5 / var6);
        int var8 = this.buyTileHeight();
        int var9 = this.buyVisible();
        this.heading(var1, this.tr("buy.title"), this.tr("buy.hint"), var5);
        this.runeOffset = Math.max(0, Math.min(Math.max(0, 27 - var9), this.runeOffset));
        OptionalInt var10 = PrisonerTrade1121.resin();
        List var11 = MinerCatalog.buys();

        for (int var12 = 0; var12 < var9 && this.runeOffset + var12 < var11.size(); var12++) {
            int var13 = this.runeOffset + var12;
            int var14 = this.bx + var12 % var6 * var7;
            int var15 = var2 + var12 / var6 * var8;
            int var16 = Math.max(12, Math.min(44, Math.min(var8 - 17, var7 - 10)));
            MinerCatalog.BuyOffer var17 = (MinerCatalog.BuyOffer) var11.get(var13);
            boolean var18 = var10.isEmpty() || var10.getAsInt() < var17.resinRequired();
            if (var13 == this.selectedRune) {
                GuiBackdrop1132.fill(
                        var1, var14, var15, var14 + var7 - 3, var15 + var8 - 3, -13884103, this);
                PrisonerArt1121.frame(var1, var14, var15, var7 - 3, var8 - 3, -7438684);
            }

            int var19 = var14 + (var7 - var16) / 2;
            int var20 = var15 + Math.max(4, (var8 - var16 - 13) / 2);
            PrisonerArt1121.texture(
                    var1,
                    PrisonerArt1121.SEAL,
                    var19 - 3,
                    var20 - 3,
                    var16 + 6,
                    var16 + 6,
                    128,
                    128,
                    var18 ? 1618442120 : -855638017);
            PrisonerArt1121.item(var1, this.runes[var13], var19, var20, var16);
            if (var18) {
                GuiBackdrop1132.fill(
                        var1, var19, var20, var19 + var16, var20 + var16, -1743777510, this);
            }

            this.center(
                    var1,
                    this.runeName(var17.rune()),
                    var14 + 2,
                    var15 + var8 - 13,
                    var7 - 7,
                    var18 ? -5527622 : -1843223,
                    0.95F);
            List var21 =
                    this.tips(
                            this.runeName(var17.rune()),
                            var10.isEmpty()
                                    ? this.tr("loading")
                                    : (var18
                                            ? this.fmt("need_resin", "n", var17.resinRequired())
                                            : this.num(MinerShopModel.get().price(var17.trigger()))
                                                    + " Chronotons"));
            int var22 = var13;
            this.hit(
                    "rune:" + var13,
                    var14,
                    var15,
                    var7 - 3,
                    var8 - 3,
                    true,
                    () -> this.selectedRune = var22,
                    var21);
        }

        if (var9 < 27) {
            int var23 = var2 + var3 - 15;
            this.button(
                    var1,
                    "runes.prev",
                    "‹",
                    this.bx,
                    var23,
                    20,
                    14,
                    this.runeOffset > 0,
                    () -> this.runeOffset = Math.max(0, this.runeOffset - var6),
                    List.of());
            this.button(
                    var1,
                    "runes.next",
                    "›",
                    this.bx + var5 - 21,
                    var23,
                    20,
                    14,
                    this.runeOffset + var9 < 27,
                    () -> this.runeOffset = Math.min(27 - var9, this.runeOffset + var6),
                    List.of());
            this.center(
                    var1,
                    this.runeOffset + 1 + "–" + Math.min(27, this.runeOffset + var9) + " / 27",
                    this.bx + 23,
                    var23 + 3,
                    var5 - 47,
                    -5527622,
                    0.8F);
        }

        int var24 = this.bx + var5 + 10;
        int var25 = this.by;
        int var26 = this.bh;
        this.panel(var1, var24, var25, var4, var26);
        MinerCatalog.BuyOffer var27 = (MinerCatalog.BuyOffer) var11.get(this.selectedRune);
        boolean var28 = var10.isEmpty() || var10.getAsInt() < var27.resinRequired();
        OptionalInt var29 = MinerShopModel.get().price(var27.trigger());
        byte var30 = 7;
        int var31 = var25 + 7;
        this.center(
                var1,
                this.runeName(var27.rune()),
                var24 + var30,
                var31,
                var4 - 2 * var30,
                -1843223,
                1.35F);
        var31 += 16;
        if (var26 >= 124) {
            int var34 = Math.max(20, Math.min(88, Math.min(var4 - 2 * var30 - 12, var26 - 104)));
            PrisonerArt1121.texture(
                    var1,
                    PrisonerArt1121.SEAL,
                    var24 + (var4 - var34 - 8) / 2,
                    var31,
                    var34 + 8,
                    var34 + 8,
                    128,
                    128,
                    -855638017);
            PrisonerArt1121.item(
                    var1,
                    this.runes[this.selectedRune],
                    var24 + (var4 - var34) / 2,
                    var31 + 4,
                    var34);
            var31 += var34 + 14;
        }

        this.text(
                var1,
                var28
                        ? (var10.isEmpty()
                                ? this.tr("loading")
                                : this.fmt("need_resin", "n", var27.resinRequired()))
                        : this.num(var29) + " Chronotons",
                var24 + var30,
                var31,
                var4 - 2 * var30,
                var28 ? -5527622 : -2769528,
                0.9F);
        var31 += 14;
        if (var26 >= 95) {
            this.text(
                    var1,
                    this.fmt("loose", "n", PrisonerTrade1121.loose(var27.rune())),
                    var24 + var30,
                    var31,
                    var4 - 2 * var30,
                    -5527622,
                    0.85F);
            this.text(
                    var1,
                    this.fmt("pouch", "n", PrisonerTrade1121.pouch(var27.rune())),
                    var24 + var30,
                    var31 + 12,
                    var4 - 2 * var30,
                    -5527622,
                    0.85F);
        }

        boolean var35 =
                var29.isPresent()
                        && (PrisonerTrade1121.chrono().isEmpty()
                                || PrisonerTrade1121.chrono().getAsInt() >= var29.getAsInt());
        String var36 =
                var28
                        ? this.tr("locked")
                        : (var29.isEmpty()
                                ? this.tr("loading")
                                : (!var35 ? this.tr("no_money") : this.tr("buy.one")));
        this.button(
                var1,
                "buy",
                var36,
                var24 + var30,
                var25 + var26 - 23,
                var4 - 2 * var30,
                18,
                !var28 && var35 && !this.trade.busy(),
                () -> this.trade.buy(var27),
                this.tips(this.tr("buy.one"), this.num(var29) + " Chronotons"));
    }

    private void powderPage(GuiGraphics var1) {
        this.heading(var1, this.tr("powder.title"), this.tr("powder.hint"));
        int var2 = this.contentY();
        int var3 = this.contentH();
        int var4 = Math.max(105, Math.min(185, this.bw / 3));
        int var5 = this.bw - var4 - 12;
        int var6 = this.bx + var5 + 12;
        MinerCatalog.PowderOffer var7 = MinerCatalog.powders().get(this.batch);
        int var8 = Math.max(30, Math.min(82, (var5 - 28) / 2));
        int var9 = Math.min(150, var3 - 10);
        int var10 = var2 + (var3 - var9) / 2;
        int var11 = this.bx + (var5 - 2 * var8 - 24) / 2;
        this.vessel(
                var1,
                var11,
                var10,
                var8,
                var9,
                this.powder,
                this.num(var7.powder()),
                this.tr("powder"));
        this.vessel(
                var1,
                var11 + var8 + 24,
                var10,
                var8,
                var9,
                this.esc,
                this.num(var7.esc()),
                this.tr("esc"));
        int var12 = var11 + var8 + 21;
        float var13 = var10 + var9 / 2.0F;
        PrisonerArt1121.line(var1, var11 + var8 + 2, var13, var12, var13, 1.0F, -4741927);
        PrisonerArt1121.line(var1, var12 - 5, var13 - 4.0F, var12, var13, 1.0F, -4741927);
        PrisonerArt1121.line(var1, var12 - 5, var13 + 4.0F, var12, var13, 1.0F, -4741927);
        if (this.trade.pulse() > 0.0F) {
            PrisonerArt1121.texture(
                    var1,
                    PrisonerArt1121.HALO,
                    var11 + var8 + 24,
                    var10,
                    var8,
                    var9,
                    256,
                    320,
                    (int) (100.0F * this.trade.pulse()) << 24 | 16777215);
        }

        int var14 = var3 < 120 ? 18 : 25;
        byte var15 = 5;
        int var16 = (var4 - var15) / 2;

        for (int var17 = 0; var17 < 4; var17++) {
            MinerCatalog.PowderOffer var18 = MinerCatalog.powders().get(var17);
            int var19 = var6 + var17 % 2 * (var16 + var15);
            int var20 = var2 + var17 / 2 * (var14 + var15);
            int var21 = var17;
            this.button(
                    var1,
                    "batch:" + var17,
                    this.num(var18.powder()),
                    var19,
                    var20,
                    var16,
                    var14,
                    true,
                    () -> this.batch = var21,
                    this.tips(var18.powder() + " → " + var18.esc() + " " + this.tr("esc")));
            if (this.batch == var17) {
                PrisonerArt1121.frame(var1, var19 + 1, var20 + 1, var16 - 2, var14 - 2, -4741927);
            }
        }

        int var22 = var2 + 2 * (var14 + var15) + 7;
        if (var22 + 12 < var2 + var3 - 25) {
            this.text(
                    var1,
                    this.tr("stock") + ": " + this.num(PrisonerTrade1121.powder()),
                    var6,
                    var22,
                    var4,
                    -5527622,
                    0.9F);
        }

        boolean var23 =
                PrisonerTrade1121.powder().isPresent()
                        && PrisonerTrade1121.powder().getAsInt() >= var7.powder();
        this.button(
                var1,
                "powder.trade",
                this.tr("powder.action"),
                var6,
                var2 + var3 - 21,
                var4,
                19,
                var23 && !this.trade.busy(),
                () -> this.trade.sellPowder(var7),
                this.tips(
                        var7.powder() + " " + this.tr("powder"),
                        "→ " + var7.esc() + " " + this.tr("esc")));
    }

    private void vessel(
            GuiGraphics var1,
            int var2,
            int var3,
            int var4,
            int var5,
            ItemStack var6,
            String var7,
            String var8) {
        this.panel(var1, var2, var3, var4, var5);
        GuiBackdrop1132.fill(var1, var2 + 5, var3 + 3, var2 + var4 - 5, var3 + 5, -7833453, this);
        GuiBackdrop1132.fill(
                var1, var2 + 5, var3 + var5 - 5, var2 + var4 - 5, var3 + var5 - 3, -7833453, this);
        int var9 = Math.min(38, Math.max(12, var5 / 3));
        if (var5 >= 75) {
            this.center(var1, var8, var2 + 3, var3 + 11, var4 - 6, -5527622, 0.85F);
        }

        PrisonerArt1121.item(
                var1, var6, var2 + (var4 - var9) / 2, var3 + (var5 - var9) / 2 - 5, var9);
        this.center(var1, var7, var2 + 4, var3 + var5 - 21, var4 - 8, -2769528, 1.25F);
    }

    private int recipeVisible() {
        return Math.max(1, (this.contentH() - 18) / 32);
    }

    private void exchangePage(GuiGraphics var1) {
        this.heading(var1, this.tr("exchange.title"), this.tr("exchange.hint"));
        int var2 = this.contentY();
        int var3 = this.contentH();
        int var4 = Math.max(100, Math.min(210, this.bw / 3));
        List var5 = MinerShopModel.get().exchanges();
        if (var5.isEmpty()) {
            this.text(
                    var1,
                    this.trade.busy()
                            ? this.tr("pending")
                            : (PrisonerTrade1121.resin().orElse(-1) == 0
                                    ? this.tr("exchange.none")
                                    : this.tr("exchange.loading")),
                    this.bx,
                    var2 + 12,
                    this.bw,
                    -5527622,
                    1.0F);
        } else {
            this.selectedRecipe = Math.max(0, Math.min(this.selectedRecipe, var5.size() - 1));
            int var6 = this.recipeVisible();
            this.recipeOffset =
                    Math.max(0, Math.min(Math.max(0, var5.size() - var6), this.recipeOffset));

            for (int var7 = this.recipeOffset;
                    var7 < Math.min(var5.size(), this.recipeOffset + var6);
                    var7++) {
                int var8 = var2 + (var7 - this.recipeOffset) * 32;
                MinerCatalog.ExchangeOffer var9 = (MinerCatalog.ExchangeOffer) var5.get(var7);
                Optional<RuneCatalog.RuneDef> var10 = MinerCatalog.runeByName(var9.result());
                this.panel(var1, this.bx, var8, var4, 29);
                if (var7 == this.selectedRecipe) {
                    GuiBackdrop1132.fill(
                            var1, this.bx, var8, this.bx + 2, var8 + 29, -4741927, this);
                }

                int var11 = Math.max(28, Math.min(60, (var4 - 12) / 3));
                int var12 = this.bx + var4 - 6 - var11;
                int var13 = var12 - 12;
                int var14 = var13 - (this.bx + 6) - 4;
                this.text(
                        var1,
                        this.fmt("exchange.recipe", "n", var7 + 1),
                        this.bx + 6,
                        var8 + 4,
                        var14,
                        -1843223,
                        0.9F);
                int var15 = Math.max(1, var9.ingredients().size());
                int var16 = Math.max(1, Math.min(10, (var14 - 2 * (var15 - 1)) / var15));
                int var17 = this.bx + 6;

                for (String var19 : var9.ingredients()) {
                    Optional<RuneCatalog.RuneDef> var20 = MinerCatalog.runeByName(var19);
                    if (var20.isPresent()) {
                        PrisonerArt1121.item(
                                var1,
                                this.runes[((RuneCatalog.RuneDef) var20.get()).level()],
                                var17,
                                var8 + 15,
                                var16);
                    }

                    var17 += var16 + 2;
                }

                PrisonerArt1121.line(var1, var13, var8 + 7, var13 + 8, var8 + 7, 1.0F, -4741927);
                PrisonerArt1121.line(
                        var1, var13 + 5, var8 + 4, var13 + 8, var8 + 7, 1.0F, -4741927);
                PrisonerArt1121.line(
                        var1, var13 + 5, var8 + 10, var13 + 8, var8 + 7, 1.0F, -4741927);
                String var46 = var10.map(this::runeName).orElse(var9.result());
                this.center(var1, var46, var12, var8 + 4, var11, -1843223, 0.9F);
                if (var10.isPresent()) {
                    byte var48 = 12;
                    PrisonerArt1121.item(
                            var1,
                            this.runes[((RuneCatalog.RuneDef) var10.get()).level()],
                            var12 + (var11 - var48) / 2,
                            var8 + 14,
                            var48);
                }

                int var49 = var7;
                this.hit(
                        "recipe:" + var7,
                        this.bx,
                        var8,
                        var4,
                        29,
                        true,
                        () -> this.selectedRecipe = var49,
                        this.tips(this.fmt("exchange.recipe", "n", var7 + 1) + " → " + var46));
            }

            if (var5.size() > var6) {
                int var34 = var2 + var3 - 15;
                this.button(
                        var1,
                        "recipes.prev",
                        "‹",
                        this.bx,
                        var34,
                        18,
                        14,
                        this.recipeOffset > 0,
                        () -> this.recipeOffset--,
                        List.of());
                this.button(
                        var1,
                        "recipes.next",
                        "›",
                        this.bx + var4 - 19,
                        var34,
                        18,
                        14,
                        this.recipeOffset + var6 < var5.size(),
                        () -> this.recipeOffset++,
                        List.of());
                this.center(
                        var1,
                        this.recipeOffset
                                + 1
                                + "–"
                                + Math.min(var5.size(), this.recipeOffset + var6)
                                + " / "
                                + var5.size(),
                        this.bx + 20,
                        var34 + 3,
                        var4 - 40,
                        -5527622,
                        0.8F);
            }

            MinerCatalog.ExchangeOffer var35 =
                    (MinerCatalog.ExchangeOffer) var5.get(this.selectedRecipe);
            Optional<RuneCatalog.RuneDef> var36 = MinerCatalog.runeByName(var35.result());
            LinkedHashMap<RuneCatalog.RuneDef, Integer> var37 =
                    PrisonerTrade1121.requirements(var35);
            int var38 = this.bx + var4 + 12;
            int var39 = this.bw - var4 - 12;
            if (var3 >= 90 && (var39 >= 240 || var3 >= 150)) {
                boolean var40 = var39 >= 240 && var3 >= 90;
                int var41 = var40 ? Math.min(140, var39 / 3) : var39;
                int var42 = var40 ? var39 - var41 - 10 : var39;
                int var43 = var40 ? var38 + var42 + 10 : var38;
                int var44 = var40 ? var2 : var2 + Math.min(110, var3 / 2);
                int var45 = var40 ? var3 : Math.min(105, var3 / 2);
                int var47 = Math.max(24, Math.min(var42 - 12, var45 - 4));
                int var50 = var38 + var42 / 2;
                int var51 = var2 + var45 / 2;
                PrisonerArt1121.texture(
                        var1,
                        PrisonerArt1121.SEAL,
                        var50 - var47 / 2,
                        var51 - var47 / 2,
                        var47,
                        var47,
                        128,
                        128,
                        -855638017);
                int var21 = Math.max(12, Math.min(25, var47 / 5));
                int var22 = 0;

                for (String var24 : var35.ingredients()) {
                    Optional<RuneCatalog.RuneDef> var25 = MinerCatalog.runeByName(var24);
                    double var26 =
                            (-Math.PI / 2)
                                    + var22++
                                            * 2
                                            * Math.PI
                                            / Math.max(1, var35.ingredients().size());
                    int var28 = var50 + (int) (Math.cos(var26) * var47 * 0.36) - var21 / 2;
                    int var29 = var51 + (int) (Math.sin(var26) * var47 * 0.36) - var21 / 2;
                    this.panel(var1, var28 - 2, var29 - 2, var21 + 4, var21 + 4);
                    if (var25.isPresent()) {
                        PrisonerArt1121.item(
                                var1,
                                this.runes[((RuneCatalog.RuneDef) var25.get()).level()],
                                var28,
                                var29,
                                var21);
                        RuneCatalog.RuneDef var30 = (RuneCatalog.RuneDef) var25.get();
                        this.hit(
                                "ingredient:" + var22,
                                var28 - 2,
                                var29 - 2,
                                var21 + 4,
                                var21 + 4,
                                false,
                                () -> {},
                                this.tips(
                                        this.runeName(var30),
                                        this.fmt("loose", "n", PrisonerTrade1121.loose(var30)),
                                        this.fmt("pouch", "n", PrisonerTrade1121.pouch(var30))));
                    }
                }

                if (var36.isPresent()) {
                    PrisonerArt1121.item(
                            var1,
                            this.runes[((RuneCatalog.RuneDef) var36.get()).level()],
                            var50 - var21 * 2 / 3,
                            var51 - var21 * 2 / 3,
                            var21 * 4 / 3);
                }

                int var52 = var2 + var3 - var44;
                String var53 = var36.<String>map(var1x -> this.runeName(var1x) + " ×1").orElse("?");
                this.text(
                        var1,
                        this.tr("exchange.result") + " " + var53,
                        var43,
                        var44,
                        var41,
                        -4202503,
                        1.0F);
                int var54 = var44 + 15;
                int var55 = Math.max(0, var52 - 39);
                int var27 = Math.min(17, Math.max(10, var55 / Math.max(1, var37.size())));

                for (var var58 : var37.entrySet()) {
                    RuneCatalog.RuneDef var59 = (RuneCatalog.RuneDef) var58.getKey();
                    long var31 = PrisonerTrade1121.total(var59);
                    String var33 = this.num(var31) + " / " + var58.getValue();
                    PrisonerArt1121.item(var1, this.runes[var59.level()], var43, var54, 10);
                    this.text(
                            var1,
                            this.runeName(var59),
                            var43 + 13,
                            var54 + 1,
                            Math.max(18, var41 / 2 - 10),
                            -1843223,
                            0.9F);
                    this.text(
                            var1,
                            var33,
                            var43 + var41 / 2,
                            var54 + 1,
                            Math.max(1, var41 / 2),
                            var31 >= ((Integer) var58.getValue()).intValue() ? -5651801 : -2382683,
                            0.9F);
                    this.hit(
                            "stock:" + var59.id(),
                            var43,
                            var54,
                            var41,
                            var27,
                            false,
                            () -> {},
                            this.tips(
                                    this.runeName(var59),
                                    this.fmt("loose", "n", PrisonerTrade1121.loose(var59)),
                                    this.fmt("pouch", "n", PrisonerTrade1121.pouch(var59)),
                                    this.tr("exchange.stock")));
                    var54 += var27;
                }

                boolean var57 = PrisonerTrade1121.canExchange(var35);
                this.button(
                        var1,
                        "exchange.trade",
                        var57 ? this.tr("exchange.action") : this.tr("no_material"),
                        var43,
                        var2 + var3 - 20,
                        var41,
                        18,
                        var57 && !this.trade.busy(),
                        () -> this.trade.exchange(var35),
                        this.tips(this.tr("exchange.stock"), var53));
            } else {
                this.compactExchange(var1, var35, var37, var36, var38, var2, var39, var3);
            }
        }
    }

    private void compactExchange(
            GuiGraphics var1,
            MinerCatalog.ExchangeOffer var2,
            LinkedHashMap<RuneCatalog.RuneDef, Integer> var3,
            Optional<RuneCatalog.RuneDef> var4,
            int var5,
            int var6,
            int var7,
            int var8) {
        int var9 = var3.size() > 2 ? 2 : 1;
        int var10 = var7 / var9;
        int var11 =
                Math.max(
                        10,
                        Math.min(14, (var8 - 20) / Math.max(1, (var3.size() + var9 - 1) / var9)));
        int var12 = 0;

        for (var var14 : var3.entrySet()) {
            int var15 = var5 + var12 % var9 * var10;
            int var16 = var6 + var12 / var9 * var11;
            var12++;
            RuneCatalog.RuneDef var17 = (RuneCatalog.RuneDef) var14.getKey();
            long var18 = PrisonerTrade1121.total(var17);
            PrisonerArt1121.item(var1, this.runes[var17.level()], var15, var16, 9);
            this.text(
                    var1,
                    this.runeName(var17) + " " + this.num(var18) + "/" + var14.getValue(),
                    var15 + 11,
                    var16,
                    var10 - 13,
                    var18 >= ((Integer) var14.getValue()).intValue() ? -5651801 : -2382683,
                    0.85F);
            this.hit(
                    "stock:" + var17.id(),
                    var15,
                    var16,
                    var10,
                    var11,
                    false,
                    () -> {},
                    this.tips(
                            this.runeName(var17),
                            this.fmt("loose", "n", PrisonerTrade1121.loose(var17)),
                            this.fmt("pouch", "n", PrisonerTrade1121.pouch(var17))));
        }

        if (var8 >= 80 && var4.isPresent()) {
            int var20 = (var3.size() + var9 - 1) / var9;
            int var22 = var8 - var20 * var11 - 24;
            int var24 = Math.min(32, Math.max(14, var22 - 8));
            int var25 = var6 + var20 * var11 + (var22 - var24) / 2;
            PrisonerArt1121.texture(
                    var1,
                    PrisonerArt1121.SEAL,
                    var5 + var7 / 2 - var24 / 2 - 3,
                    var25 - 3,
                    var24 + 6,
                    var24 + 6,
                    128,
                    128,
                    -855638017);
            PrisonerArt1121.item(
                    var1,
                    this.runes[((RuneCatalog.RuneDef) var4.get()).level()],
                    var5 + var7 / 2 - var24 / 2,
                    var25,
                    var24);
        }

        boolean var21 = PrisonerTrade1121.canExchange(var2);
        String var23 = var4.map(this::runeName).orElse("?");
        this.button(
                var1,
                "exchange.trade",
                var21 ? this.tr("exchange.result") + " " + var23 : this.tr("no_material"),
                var5,
                var6 + var8 - 18,
                var7,
                17,
                var21 && !this.trade.busy(),
                () -> this.trade.exchange(var2),
                this.tips(this.tr("exchange.stock"), var23 + " ×1"));
    }

    private int unlocked(int var1) {
        return Math.max(0, Math.min(27, var1));
    }

    private int contractCount(int var1) {
        int var2 = 0;

        for (int var6 : new int[] {1, 3, 6, 9, 12, 15, 18, 21, 24, 27}) {
            if (var1 >= var6) {
                var2++;
            }
        }

        return var2;
    }

    private void resinPage(GuiGraphics var1) {
        this.heading(var1, this.tr("resin.title"), this.tr("resin.hint"));
        ItemStack var2 = this.resinIcon();
        int var3 = this.contentY();
        int var4 = this.contentH();
        int var5 = this.trade.progress();
        boolean var6 = var5 >= 0;
        boolean var7 = this.bw >= 460 && var4 >= 135;
        int var8 = Math.max(96, Math.min(155, this.bw / 3));
        int var9 = var7 ? 105 : 0;
        int var10 = var7 ? 12 : 8;
        int var11 = this.bx + var9 + (var7 ? var10 : 0);
        int var12 = this.bw - var9 - var8 - var10 - (var7 ? var10 : 0);
        int var13 = this.bx + this.bw - var8;
        if (var7) {
            int var14 = var3 + Math.max(0, (var4 - 138) / 2);
            GuiBackdrop1132.fill(var1, this.bx, var14, this.bx + 1, var14 + 137, -9346173, this);
            this.text(
                    var1,
                    this.tr("resin.given"),
                    this.bx + 8,
                    var14 + 2,
                    var9 - 12,
                    -5527622,
                    0.9F);
            this.text(
                    var1,
                    var6 ? this.num(var5) + " / 27" : "... / 27",
                    this.bx + 8,
                    var14 + 23,
                    var9 - 12,
                    -1843223,
                    1.7F);
            this.rule(var1, this.bx + 8, var14 + 44, var9 - 14);
            this.text(
                    var1,
                    this.tr("resin.unlocked"),
                    this.bx + 8,
                    var14 + 60,
                    var9 - 12,
                    -5527622,
                    0.85F);
            this.text(
                    var1,
                    var6 ? this.unlocked(var5) + " / 27" : "...",
                    this.bx + 8,
                    var14 + 75,
                    var9 - 12,
                    -1843223,
                    1.1F);
            this.text(
                    var1,
                    this.tr("resin.contracts"),
                    this.bx + 8,
                    var14 + 101,
                    var9 - 12,
                    -5527622,
                    0.85F);
            this.text(
                    var1,
                    var6 ? this.num(this.contractCount(var5)) : "...",
                    this.bx + 8,
                    var14 + 117,
                    var9 - 12,
                    -1843223,
                    1.1F);
        }

        PrisonerArt1121.tree(
                var1,
                var11,
                var3,
                Math.max(1, var12),
                var4,
                this.trade.growth(),
                this.trade.bloom(),
                var6);
        this.panel(var1, var13, var3, var8, var4);
        byte var17 = 7;
        int var15 = var3 + 7;
        if (var4 >= 155) {
            byte var16 = 34;
            PrisonerArt1121.item(var1, var2, var13 + (var8 - var16) / 2, var15 + 4, var16);
            var15 += 48;
            this.text(
                    var1,
                    this.tr("resin.offer"),
                    var13 + var17,
                    var15,
                    var8 - 2 * var17,
                    -1843223,
                    1.1F);
            var15 += 20;
        }

        if (!var7) {
            this.text(
                    var1,
                    var6 ? this.num(var5) + " / 27" : "... / 27",
                    var13 + var17,
                    var15,
                    var8 - 2 * var17,
                    -1843223,
                    var4 < 70 ? 1.1F : 1.6F);
            var15 += var4 < 70 ? 13 : 20;
        }

        if (var15 + 11 < var3 + var4 - 22) {
            PrisonerArt1121.item(var1, var2, var13 + var17, var15, 11);
            this.text(
                    var1,
                    this.tr("carried") + ": " + this.num(PrisonerTrade1121.carried(false)),
                    var13 + var17 + 15,
                    var15 + 1,
                    var8 - 2 * var17 - 15,
                    -5527622,
                    0.9F);
            var15 += 20;
        }

        if (var4 >= 145) {
            String var19 =
                    var6
                            ? (var5 >= 27
                                    ? this.tr("resin.full")
                                    : this.fmt("need_resin", "n", var5 + 1))
                            : this.tr("loading");
            this.text(
                    var1,
                    var19,
                    var13 + var17,
                    Math.min(var15 + 2, var3 + var4 - 43),
                    var8 - 2 * var17,
                    -5527622,
                    0.8F);
        }

        List var20 =
                this.tips(
                        this.tr("resin.given") + ": " + (var6 ? var5 + " / 27" : "..."),
                        this.tr("resin.tip"),
                        this.tr("resin.shared"),
                        var5 >= 27 ? this.tr("resin.full") : "");
        this.hit("tree", var11, var3, var12, var4, false, () -> {}, var20);
        this.button(
                var1,
                "resin.trade",
                this.tr("resin.action"),
                var13 + var17,
                var3 + var4 - 22,
                var8 - 2 * var17,
                18,
                PrisonerTrade1121.carried(false) > 0
                        && PrisonerTrade1121.resin().isPresent()
                        && !this.trade.busy(),
                this.trade::donate,
                var20);
    }

    private void orePage(GuiGraphics var1) {
        this.heading(var1, this.tr("ore.title"), this.tr("ore.hint"));
        int var2 = this.contentY();
        int var3 = this.contentH();
        int var4 = PrisonerTrade1121.carried(true);
        int var5 = Math.max(98, Math.min(160, this.bw / 3));
        int var6 = this.bw >= 460 && var3 >= 130 ? 105 : 0;
        byte var7 = 10;
        int var8 = this.bx + this.bw - var5;
        int var9 = this.bx + var6 + (var6 > 0 ? var7 : 0);
        int var10 = var8 - var9 - var7;
        if (var6 > 0) {
            this.panel(var1, this.bx, var2 + var3 / 4, var6, var3 / 2);
            this.text(
                    var1,
                    this.tr("carried"),
                    this.bx + 6,
                    var2 + var3 / 4 + 7,
                    var6 - 12,
                    -1843223,
                    1.0F);
            List var11 = PrisonerTrade1121.oreStacks();

            for (int var12 = 0; var12 < Math.min(3, var11.size()); var12++) {
                int var13 = this.bx + 6 + var12 * 30;
                int var14 = var2 + var3 / 4 + 27;
                PrisonerArt1121.frame(var1, var13, var14, 26, 27, -12041903);
                PrisonerArt1121.item(var1, (ItemStack) var11.get(var12), var13 + 3, var14 + 3, 19);
                this.text(
                        var1,
                        this.num(((ItemStack) var11.get(var12)).getCount()),
                        var13 + 3,
                        var14 + 18,
                        21,
                        -2769528,
                        0.75F);
            }
        }

        int var21 = Math.max(25, Math.min(68, var10 - 15));
        int var22 = Math.max(20, Math.min(70, var3 / 3));
        int var23 = var9 + var10 / 2;
        int var24 = var2 + Math.max(2, (var3 - var22 * 2 - 25) / 2);
        int var15 = Math.min(40, var3 / 4);
        PrisonerArt1121.line(
                var1, var23 - var21 * 0.65F, var24, var23 + var21 * 0.65F, var24, 1.0F, -6256011);
        PrisonerArt1121.line(
                var1,
                var23 - var21 * 0.65F,
                var24,
                var23 - var21 * 0.2F,
                var24 + var15,
                1.0F,
                -9083783);
        PrisonerArt1121.line(
                var1,
                var23 + var21 * 0.65F,
                var24,
                var23 + var21 * 0.2F,
                var24 + var15,
                1.0F,
                -9083783);
        if (var4 > 0) {
            PrisonerArt1121.item(
                    var1, this.ore, var23 - 10, var24 + 3, Math.min(20, Math.max(10, var15 - 3)));
        }

        int var16 = var24 + var15 + 3;
        int var17 = var23 - var21 / 2;
        int var18 = Math.max(8, Math.min(var21 - 10, var22 - 10));
        this.panel(var1, var17, var16, var21, var22);
        PrisonerArt1121.icon(
                var1,
                PrisonerArt1121.EXTRACTOR,
                var17 + (var21 - var18) / 2,
                var16 + (var22 - var18) / 2,
                var18,
                255);
        if (this.trade.pulse() > 0.0F) {
            PrisonerArt1121.texture(
                    var1,
                    PrisonerArt1121.HALO,
                    var23 - var21 / 2,
                    var16,
                    var21,
                    var22,
                    256,
                    320,
                    (int) (110.0F * this.trade.pulse()) << 24 | 16777215);
        }

        int var19 = Math.min(var2 + var3 - 18, var16 + var22 + 12);
        this.rule(var1, var23 - var21 / 2, var19 - 4, var21);
        PrisonerArt1121.item(var1, this.powder, var23 - 7, var19, 14);
        int var20 = var2 + 4;
        this.text(var1, this.tr("ore.input"), var8, var20, var5, -5527622, 0.9F);
        this.text(var1, this.num(var4), var8, var20 + 15, var5, -1843223, var3 < 80 ? 1.2F : 1.6F);
        if (var3 >= 90) {
            this.text(var1, this.tr("ore.output"), var8, var20 + 45, var5, -5527622, 0.9F);
            this.text(var1, "+" + this.num(var4 * 5L), var8, var20 + 60, var5, -2769528, 1.4F);
        }

        this.button(
                var1,
                "ore.trade",
                this.tr("ore.action"),
                var8,
                var2 + var3 - 21,
                var5,
                18,
                var4 > 0 && !this.trade.busy(),
                this.trade::sellOre,
                this.tips(
                        this.tr("ore.hint"),
                        this.num(var4) + " → " + this.num(var4 * 5L) + " " + this.tr("powder")));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
        var1 = MenuScale207.event(this, var1);
        if (var1.button() != 0) {
            return false;
        }

        long var3 = PrisonerTrade1121.now();

        for (int var5 = this.hits.size() - 1; var5 >= 0; var5--) {
            MinerShopScreen.Hit var6 = this.hits.get(var5);
            if (var1.x() >= var6.x
                    && var1.x() < var6.x + var6.w
                    && var1.y() >= var6.y
                    && var1.y() < var6.y + var6.h) {
                if (var6.enabled && var3 - this.lastClick >= 90L) {
                    this.lastClick = var3;
                    var6.action.run();
                }

                return true;
            }
        }

        return super.mouseClicked(var1, var2);
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        var1 = MenuScale207.coordinate(this, var1);
        var3 = MenuScale207.coordinate(this, var3);
        if (var7 != 0.0
                && !(var1 < this.bx)
                && !(var1 >= this.bx + this.bw)
                && !(var3 < this.by)
                && !(var3 >= this.by + this.bh)) {
            int var9 = var7 > 0.0 ? -1 : 1;
            if (this.tab == MinerCatalog.Tab.BUY) {
                this.runeOffset =
                        Math.max(
                                0,
                                Math.min(
                                        Math.max(0, 27 - this.buyVisible()),
                                        this.runeOffset + var9 * this.buyColumns()));
                return true;
            } else if (this.tab == MinerCatalog.Tab.EXCHANGE) {
                this.recipeOffset =
                        Math.max(
                                0,
                                Math.min(
                                        Math.max(
                                                0,
                                                MinerShopModel.get().exchanges().size()
                                                        - this.recipeVisible()),
                                        this.recipeOffset + var9));
                return true;
            } else {
                return false;
            }
        } else {
            return super.mouseScrolled(var1, var3, var5, var7);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent var1) {
        int var2 = var1.key();
        if (var2 == 256) {
            this.onClose();
            return true;
        }

        if (var2 == 263 || var2 == 262) {
            int var5 = Arrays.asList(TABS).indexOf(this.tab);
            this.switchTab(TABS[Math.floorMod(var5 + (var2 == 262 ? 1 : -1), TABS.length)]);
            return true;
        }

        if (var2 == 258) {
            int var4 = this.focusIndex;

            do {
                this.focusIndex = Math.floorMod(this.focusIndex + 1, Math.max(1, this.hits.size()));
            } while (!this.hits.isEmpty()
                    && !this.hits.get(this.focusIndex).enabled
                    && this.focusIndex != var4);

            return true;
        } else if ((var2 == 257 || var2 == 32)
                && this.focusIndex >= 0
                && this.focusIndex < this.hits.size()) {
            MinerShopScreen.Hit var3 = this.hits.get(this.focusIndex);
            if (var3.enabled) {
                var3.action.run();
            }

            return true;
        } else {
            return super.keyPressed(var1);
        }
    }

    @Override
    public void mouseMoved(double var1, double var3) {
        var1 = MenuScale207.coordinate(this, var1);
        var3 = MenuScale207.coordinate(this, var3);
        super.mouseMoved(var1, var3);
    }

    @Override
    public void resize(int var1, int var2) {
        MenuScale207.reset(this);
        super.resize(var1, var2);
    }

    private record Hit(
            String id,
            int x,
            int y,
            int w,
            int h,
            boolean enabled,
            Runnable action,
            List<Component> tip) {}
}
