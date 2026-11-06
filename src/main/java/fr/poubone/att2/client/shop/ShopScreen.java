package fr.poubone.att2.client.shop;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.data.CurrencyModel;
import fr.poubone.att2.client.data.ScoreCache;
import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.input.KeybindManager;
import fr.poubone.att2.client.quest.QuestJournalArt;
import fr.poubone.att2.client.quest.QuestJournalSearchBox;
import fr.poubone.att2.client.renderer.ItemRarity;
import fr.poubone.att2.client.screen.GuiBackdrop1132;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class ShopScreen extends Screen {
    private static final SoundEvent OPEN_SOUND =
            SoundEvent.createVariableRangeEvent(
                    Identifier.fromNamespaceAndPath("att2", "ui.book.open"));
    private static final int ROW_HEIGHT = 31;
    private int x;
    private int y;
    private int w;
    private int h;
    private int railW;
    private int detailsW;
    private int listX;
    private int listW;
    private int detailsX;
    private int listTop;
    private int listBottom;
    private int lastVersion = -1;
    private int smithVersion = -1;
    private int listOffset;
    private int categoryOffset;
    private int detailScroll;
    private int detailHeight;
    private int selectedTrigger = -1;
    private Set<Integer> carriedSpells206 = Set.of();
    private String filter = "";
    private String search = "";
    private String lastSeller = "";
    private ShopType type = ShopType.GENERAL;
    private boolean lastRepair;
    private boolean qualityOpen;
    private String qualityFilter = "";
    private int qualityOffset;
    private List<String> qualities = List.of("");
    private ShopAction confirmation;
    private ItemStack repairConfirmedStack;
    private String dragging;
    private int lastMouseX;
    private int lastMouseY;
    private long lastPurchaseMs;
    private long requestNoticeUntil;
    private EditBox searchBox;
    private List<ShopOffer> offers = List.of();
    private List<String> categories = List.of();
    private List<ShopAction> actions = List.of();

    private ShopScreen() {
        super(ModLanguageManager.get("shop.title"));
    }

    public static void open() {
        if (!FlashbackCompat.isInReplay()
                && HUDConfig.get().isShopMenuEnabled(ShopModel.get().menuTypeId())) {
            Minecraft var0 = Minecraft.getInstance();
            if (var0.screen instanceof ShopScreen var1) {
                var1.rebuild();
            } else if (var0.screen == null || var0.screen instanceof ChatScreen) {
                var0.setScreen(new ShopScreen());
                var0.getSoundManager().play(SimpleSoundInstance.forUI(OPEN_SOUND, 1.0F));
            }
        }
    }

    public static boolean isOpen() {
        return Minecraft.getInstance().screen instanceof ShopScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        MenuScale207.prepare(this);
        this.clearWidgets();
        this.w = Math.min(680, Math.max(1, this.width - 20));
        this.h = Math.min(382, Math.max(1, this.height - 20));
        this.x = (this.width - this.w) / 2;
        this.y = (this.height - this.h) / 2;
        this.railW = this.w >= 540 ? 104 : 78;
        this.detailsW = Math.max(120, Math.min(236, (this.w - this.railW) * 45 / 100));
        this.listX = this.x + this.railW + 8;
        this.listW = this.w - this.railW - this.detailsW - 20;
        this.detailsX = this.listX + this.listW + 6;
        this.listTop = this.y + (this.qualityWrapped() ? 104 : 83);
        this.listBottom = this.buttonY() - 9;
        this.searchBox =
                new QuestJournalSearchBox(
                        this.font,
                        this.listX + 18,
                        this.y + 52,
                        this.searchWidth(),
                        13,
                        ModLanguageManager.get("shop.modern.search"));
        this.searchBox.setMaxLength(64);
        this.searchBox.setValue(this.search);
        this.searchBox.setResponder(
                var1 -> {
                    this.search = var1;
                    this.listOffset = 0;
                    this.rebuild();
                });
        this.addRenderableWidget(this.searchBox);
        CurrencyModel.request();
        this.rebuild();
    }

    private String tr(String var1) {
        return ModLanguageManager.getString("shop.modern." + var1);
    }

    private static String plain(Component var0) {
        return var0 == null ? "" : ChatFormatting.stripFormatting(var0.getString());
    }

    private Component displayName(ShopOffer var1) {
        return ShopOfferPresentation.displayName(var1);
    }

    private String name(ShopOffer var1) {
        return plain(this.displayName(var1));
    }

    private int visibleRows() {
        return Math.max(1, (this.listBottom - this.listTop) / 31);
    }

    private int maxListOffset() {
        return Math.max(0, this.offers.size() - this.visibleRows());
    }

    private int categoryTop() {
        return this.y + 64;
    }

    private int categoryBottom() {
        return this.footerY() - 8 - this.sideActionCount() * 25;
    }

    private int visibleCategories() {
        return Math.max(1, (this.categoryBottom() - this.categoryTop()) / 24);
    }

    private int buttonY() {
        return this.y + this.h - 42;
    }

    private int footerY() {
        return this.buttonY();
    }

    private int sideActionCount() {
        return this.actions.size() + (ShopModel.get().hasRepair() ? 1 : 0);
    }

    private int resetX() {
        return this.listX + this.listW - 27;
    }

    private boolean qualityWrapped() {
        return this.listW < 170;
    }

    private int qualityW() {
        return this.qualityWrapped() ? Math.min(90, this.listW - 16) : 66;
    }

    private int qualityX() {
        return this.qualityWrapped()
                ? this.listX + 8
                : (ShopModel.get().hasReset() ? this.resetX() - 6 : this.listX + this.listW - 10)
                        - this.qualityW();
    }

    private int qualityY() {
        return this.y + (this.qualityWrapped() ? 72 : 50);
    }

    private int searchWidth() {
        return this.qualityWrapped()
                ? Math.max(24, this.listW - 33 - (ShopModel.get().hasReset() ? 22 : 0))
                : Math.max(24, this.qualityX() - this.listX - 24);
    }

    private int qualityMenuX() {
        return Math.min(this.qualityX(), this.x + this.w - this.qualityMenuW() - 10);
    }

    private int qualityMenuW() {
        return Math.min(144, this.w - 20);
    }

    private int qualityMenuY() {
        return this.qualityY() + 19;
    }

    private int qualityVisible() {
        return Math.max(1, Math.min(8, (this.footerY() - this.qualityMenuY() - 4) / 18));
    }

    private int qualityMaxOffset() {
        return Math.max(0, this.qualities.size() - this.qualityVisible());
    }

    private int qualityMenuH() {
        return Math.min(this.qualityVisible(), this.qualities.size()) * 18 + 4;
    }

    private boolean inQualityMenu(double var1, double var3) {
        return this.qualityOpen
                && hit(
                        var1,
                        var3,
                        this.qualityMenuX(),
                        this.qualityMenuY(),
                        this.qualityMenuW(),
                        this.qualityMenuH());
    }

    private void selectQuality(String var1) {
        this.qualityFilter = var1;
        this.qualityOpen = false;
        this.listOffset = 0;
        this.detailScroll = 0;
        this.selectedTrigger = -1;
        this.rebuild();
    }

    private void toggleQuality() {
        this.qualityOpen = !this.qualityOpen;
        this.blurSearch();
        if (this.qualityOpen) {
            int var1 = this.qualities.indexOf(this.qualityFilter);
            this.qualityOffset = Math.max(0, Math.min(this.qualityMaxOffset(), var1));
        }
    }

    private ShopAction resetAction() {
        return ShopModel.get().catalog().actions().stream()
                .filter(var0 -> var0.kind() == ShopAction.Kind.RESET || var0.trigger() == 423)
                .findFirst()
                .orElse(
                        new ShopAction(
                                423,
                                ModLanguageManager.get("shop.action.reset"),
                                ModLanguageManager.get("shop.action.reset.tip"),
                                ShopAction.Kind.RESET));
    }

    private ItemStack held() {
        return this.minecraft.player == null
                ? ItemStack.EMPTY
                : this.minecraft.player.getMainHandItem();
    }

    private boolean canRepairHeld() {
        ItemStack var1 = this.held();
        return ShopModel.get().hasRepair()
                && !var1.isEmpty()
                && var1.isDamageableItem()
                && var1.getDamageValue() > 0
                && ShopSmithData.heldQuote(var1) != null;
    }

    private void repairHeld() {
        if (!this.canRepairHeld()) {
            if (ShopSmithData.heldQuote(this.held()) == null) {
                ShopSmithData.retryMissing(ShopModel.get());
            }
        } else {
            ItemStack var1 = this.held();
            this.repairConfirmedStack = var1.copy();
            MutableComponent var2 =
                    Component.literal(
                            plain(var1.getHoverName())
                                    + "\n"
                                    + ModLanguageManager.format(
                                            "smith.ui.heldPrice",
                                            "price",
                                            ShopSmithData.heldQuote(var1)));
            this.confirmation =
                    new ShopAction(
                            414,
                            ModLanguageManager.get("smith.ui.repairHeld"),
                            var2,
                            ShopAction.Kind.MENDING_REPAIR);
        }
    }

    private int previousX() {
        return this.listX + 12;
    }

    private int nextX() {
        return this.listX + this.listW - 38;
    }

    private int detailTop() {
        return this.y + (this.selected() == null ? 75 : (this.h < 300 ? 111 : 137));
    }

    private int detailBottom() {
        return this.buttonY() - 35;
    }

    private int maxDetailScroll() {
        return Math.max(0, this.detailHeight - Math.max(1, this.detailBottom() - this.detailTop()));
    }

    private boolean isPriceKnown(ShopOffer var1) {
        String var2 = plain(var1.price()).strip();
        return var1.price() != null && !var2.isEmpty() && !var2.equals("?") && !var2.equals("？");
    }

    private ShopOffer selected() {
        for (ShopOffer var2 : this.offers) {
            if (var2.trigger() == this.selectedTrigger) {
                return var2;
            }
        }

        return null;
    }

    private void rebuild() {
        ShopModel var1 = ShopModel.get();
        ShopCatalog var2 = var1.catalog();
        var2.inferType();
        String var3 = var1.seller() == null ? "" : var1.seller().npcId();
        if (this.type != var2.type()
                || this.lastRepair != var1.isRepairView()
                || !this.lastSeller.equals(var3)) {
            this.filter = "";
            this.qualityFilter = "";
            this.qualityOpen = false;
            this.qualityOffset = 0;
            this.selectedTrigger = -1;
            this.listOffset = 0;
            this.detailScroll = 0;
            this.confirmation = null;
        }

        this.lastSeller = var3;
        this.type = var2.type();
        this.listTop = this.y + (this.qualityWrapped() ? 104 : 83);
        if (this.type == ShopType.DAHAL) {
            this.qualityFilter = "";
            this.qualityOpen = false;
            this.qualityOffset = 0;
        }

        this.carriedSpells206 = OwnedSpellFlags206.carried(this.minecraft.player);
        this.lastRepair = var1.isRepairView();
        if (this.lastVersion != var1.version()) {
            CurrencyModel.request();
        }

        this.lastVersion = var1.version();
        if (this.searchBox != null) {
            this.searchBox.setWidth(this.searchWidth());
        }

        ShopSmithData.sync(var1);
        ArrayList<ShopOffer> var4 =
                new ArrayList<>(this.lastRepair ? var1.repairOffers() : var2.offers());
        if (var1.hasRepair() && !this.lastRepair) {
            for (ShopOffer var6 : ShopSmithData.tools()) {
                if (var4.stream().noneMatch(var1x -> var1x.trigger() == var6.trigger())) {
                    var4.add(var6);
                }
            }
        }

        ArrayList var10 = new ArrayList();
        var10.add("");
        if (!this.lastRepair) {
            var10.addAll(var2.categories());
        }

        if (var4.stream().anyMatch(ShopSmithData::isTool) && !var10.contains("@smith_tools")) {
            var10.add("@smith_tools");
        }

        this.categories = List.copyOf(var10);
        this.qualities = ShopQuality.available(var4, this.qualityFilter);
        this.qualityOffset = Math.max(0, Math.min(this.qualityOffset, this.qualityMaxOffset()));
        if (!this.categories.contains(this.filter)) {
            this.filter = "";
        }

        String var11 = this.search.toLowerCase(Locale.ROOT).strip();
        this.offers =
                var4.stream()
                        .filter(
                                var1x ->
                                        this.filter.isEmpty()
                                                || this.filter.equals(var1x.category())
                                                || this.filter.equals("@smith_tools")
                                                        && ShopSmithData.isTool(var1x))
                        .filter(
                                var1x ->
                                        this.type == ShopType.DAHAL
                                                || ShopQuality.matches(var1x, this.qualityFilter))
                        .filter(
                                var2x ->
                                        OwnedSpellFlags206.visible(
                                                var2, var2x, this.carriedSpells206))
                        .filter(
                                var2x ->
                                        var11.isEmpty()
                                                || this.name(var2x)
                                                        .toLowerCase(Locale.ROOT)
                                                        .contains(var11))
                        .toList();
        this.listOffset = Math.max(0, Math.min(this.listOffset, this.maxListOffset()));
        this.categoryOffset =
                Math.max(
                        0,
                        Math.min(
                                this.categoryOffset,
                                Math.max(0, this.categories.size() - this.visibleCategories())));
        if (this.selected() == null) {
            this.selectedTrigger = -1;
            this.detailScroll = 0;
        }

        ArrayList<ShopAction> var7 = new ArrayList();
        if (this.lastRepair) {
            var7.add(
                    new ShopAction(
                            -2,
                            ModLanguageManager.get("shop.action.back"),
                            null,
                            ShopAction.Kind.BACK));
        }

        for (ShopAction var9 : var2.actions()) {
            if (var9.kind() != ShopAction.Kind.RESET
                    && var9.trigger() != 423
                    && !var9.isMending()
                    && var7.stream().noneMatch(var1x -> var1x.trigger() == var9.trigger())) {
                var7.add(var9);
            }
        }

        this.actions = List.copyOf(var7);
        this.smithVersion = ShopSmithData.version();
    }

    @Override
    public void tick() {
        super.tick();
        ShopSmithData.tick(ShopModel.get());
        if (ShopModel.get().version() != this.lastVersion
                || ShopSmithData.version() != this.smithVersion
                || this.type == ShopType.DAHAL
                        && !OwnedSpellFlags206.carried(this.minecraft.player)
                                .equals(this.carriedSpells206)) {
            this.rebuild();
        }
    }

    @Override
    public void removed() {
        MenuScale207.reset(this);
        CurrencyModel.close();
        super.removed();
    }

    private static boolean hit(double var0, double var2, int var4, int var5, int var6, int var7) {
        return var0 >= var4 && var0 < var4 + var6 && var2 >= var5 && var2 < var5 + var7;
    }

    private boolean inSearch(double var1, double var3) {
        return this.searchBox != null
                && hit(
                        var1,
                        var3,
                        this.searchBox.getX(),
                        this.searchBox.getY(),
                        this.searchBox.getWidth(),
                        this.searchBox.getHeight());
    }

    private void blurSearch() {
        if (this.searchBox != null) {
            this.searchBox.setFocused(false);
        }

        this.setFocused(null);
    }

    private void choose(ShopOffer var1) {
        if (this.selectedTrigger != var1.trigger()) {
            this.detailScroll = 0;
        }

        this.selectedTrigger = var1.trigger();
    }

    private void purchase() {
        ShopOffer var1 = this.selected();
        if (var1 != null
                && var1.trigger() >= 0
                && this.isPriceKnown(var1)
                && !ShopModel.get().isCollecting()) {
            long var2 = System.currentTimeMillis();
            if (var2 - this.lastPurchaseMs >= 350L) {
                this.lastPurchaseMs = var2;
                ShopModel.get().buy(var1);
                this.requestNoticeUntil = var2 + 1600L;
            }
        }
    }

    private void run(ShopAction var1) {
        ShopModel.get().run(var1);
        if (var1.kind() == ShopAction.Kind.RESET || var1.trigger() == 423) {
            ShopSmithData.retry(ShopModel.get());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
        var1 = MenuScale207.event(this, var1);
        double var3 = var1.x();
        double var5 = var1.y();
        int var7 = var1.button();
        if (this.confirmation != null) {
            if (var7 != 0) {
                return true;
            }

            int var17 = this.x + this.w / 2;
            int var18 = this.y + this.h / 2;
            if (hit(var3, var5, var17 - 122, var18 + 32, 116, 20)) {
                ShopAction var10 = this.confirmation;
                if (var10.trigger() == 414) {
                    if (!this.canRepairHeld()) {
                        this.confirmation = null;
                        this.repairConfirmedStack = null;
                        return true;
                    }

                    String var11 =
                            plain(this.held().getHoverName())
                                    + "\n"
                                    + ModLanguageManager.format(
                                            "smith.ui.heldPrice",
                                            "price",
                                            ShopSmithData.heldQuote(this.held()));
                    if (this.repairConfirmedStack == null
                            || !ItemStack.isSameItemSameComponents(
                                    this.repairConfirmedStack, this.held())
                            || !var11.equals(plain(var10.tip()))) {
                        this.confirmation = null;
                        this.repairHeld();
                        return true;
                    }
                }

                this.confirmation = null;
                this.repairConfirmedStack = null;
                ShopModel.get().run(var10);
                if (var10.trigger() == 423) {
                    ShopSmithData.retry(ShopModel.get());
                }
            } else if (hit(var3, var5, var17 + 6, var18 + 32, 116, 20)) {
                this.confirmation = null;
                this.repairConfirmedStack = null;
            }

            return true;
        } else {
            if (this.qualityOpen) {
                if (this.inQualityMenu(var3, var5)) {
                    if (var7 == 0) {
                        int var16 =
                                this.qualityOffset + (int) (var5 - this.qualityMenuY() - 2.0) / 18;
                        if (var16 >= 0
                                && var16 < this.qualities.size()
                                && var16 < this.qualityOffset + this.qualityVisible()) {
                            this.selectQuality(this.qualities.get(var16));
                        }
                    }

                    return true;
                }

                this.qualityOpen = false;
                if (hit(var3, var5, this.qualityX(), this.qualityY(), this.qualityW(), 17)) {
                    return true;
                }
            }

            if (hit(var3, var5, this.qualityX(), this.qualityY(), this.qualityW(), 17)) {
                if (this.type == ShopType.DAHAL) {
                    if (var7 == 0) {
                        this.toggleOwnedSpells();
                    }
                } else if (var7 == 0) {
                    this.toggleQuality();
                } else if (var7 == 1) {
                    this.selectQuality("");
                }

                return true;
            } else {
                if (var7 == 1 && this.inSearch(var3, var5)) {
                    this.searchBox.setValue("");
                    this.blurSearch();
                    return true;
                }

                if (!this.inSearch(var3, var5)) {
                    this.blurSearch();
                }

                if (var7 == 1) {
                    ShopOffer var15 = this.rowAt(var3, var5);
                    if (var15 != null && var15.trigger() == this.selectedTrigger) {
                        this.selectedTrigger = -1;
                        this.detailScroll = 0;
                        return true;
                    } else {
                        return super.mouseClicked(var1, var2);
                    }
                } else {
                    if (var7 != 0) {
                        return super.mouseClicked(var1, var2);
                    }

                    if (hit(var3, var5, this.x + this.w - 24, this.y + 8, 16, 16)) {
                        this.onClose();
                        return true;
                    }

                    for (int var8 = this.categoryOffset;
                            var8
                                    < Math.min(
                                            this.categories.size(),
                                            this.categoryOffset + this.visibleCategories());
                            var8++) {
                        if (hit(
                                var3,
                                var5,
                                this.x + 8,
                                this.categoryTop() + (var8 - this.categoryOffset) * 24,
                                this.railW - 16,
                                21)) {
                            this.filter = this.categories.get(var8);
                            this.listOffset = 0;
                            this.detailScroll = 0;
                            this.selectedTrigger = -1;
                            this.rebuild();
                            return true;
                        }
                    }

                    if (ShopModel.get().hasRepair()
                            && hit(var3, var5, this.x + 8, this.footerY(), this.railW - 16, 20)) {
                        this.repairHeld();
                        return true;
                    }

                    for (int var13 = 0; var13 < this.actions.size(); var13++) {
                        int var9 =
                                this.footerY()
                                        - 25
                                                * (this.actions.size()
                                                        - var13
                                                        - (ShopModel.get().hasRepair() ? 0 : 1));
                        if (hit(var3, var5, this.x + 8, var9, this.railW - 16, 20)) {
                            this.run(this.actions.get(var13));
                            return true;
                        }
                    }

                    if (ShopModel.get().hasReset()
                            && hit(var3, var5, this.resetX() - 2, this.y + 50, 20, 17)) {
                        this.run(this.resetAction());
                        return true;
                    } else {
                        ShopOffer var14 = this.rowAt(var3, var5);
                        if (var14 != null) {
                            this.choose(var14);
                            return true;
                        } else if (hit(
                                var3,
                                var5,
                                this.detailsX + 12,
                                this.buttonY(),
                                this.detailsW - 24,
                                20)) {
                            this.purchase();
                            return true;
                        } else if (hit(var3, var5, this.previousX(), this.buttonY(), 26, 20)) {
                            this.listOffset = Math.max(0, this.listOffset - this.visibleRows());
                            return true;
                        } else if (hit(var3, var5, this.nextX(), this.buttonY(), 26, 20)) {
                            this.listOffset =
                                    Math.min(
                                            this.maxListOffset(),
                                            this.listOffset + this.visibleRows());
                            return true;
                        } else if (this.maxListOffset() > 0
                                && hit(
                                        var3,
                                        var5,
                                        this.listX + this.listW - 7,
                                        this.listTop,
                                        7,
                                        this.listBottom - this.listTop)) {
                            this.dragging = "list";
                            this.scrollTo(var5);
                            return true;
                        } else if (this.maxDetailScroll() > 0
                                && hit(
                                        var3,
                                        var5,
                                        this.detailsX + this.detailsW - 13,
                                        this.detailTop(),
                                        6,
                                        this.detailBottom() - this.detailTop())) {
                            this.dragging = "details";
                            this.scrollTo(var5);
                            return true;
                        } else {
                            return super.mouseClicked(var1, var2);
                        }
                    }
                }
            }
        }
    }

    private ShopOffer rowAt(double var1, double var3) {
        if (!hit(
                var1,
                var3,
                this.listX + 8,
                this.listTop,
                this.listW - 18,
                this.listBottom - this.listTop)) {
            return null;
        }

        int var5 = (int) (var3 - this.listTop) / 31 + this.listOffset;
        return var5 >= this.listOffset
                        && var5 < Math.min(this.offers.size(), this.listOffset + this.visibleRows())
                ? this.offers.get(var5)
                : null;
    }

    private void scrollTo(double var1) {
        if ("list".equals(this.dragging)) {
            double var3 = (var1 - this.listTop) / Math.max(1, this.listBottom - this.listTop);
            this.listOffset =
                    Math.max(
                            0,
                            Math.min(
                                    this.maxListOffset(),
                                    (int) Math.round(var3 * this.maxListOffset())));
        } else {
            double var5 =
                    (var1 - this.detailTop()) / Math.max(1, this.detailBottom() - this.detailTop());
            this.detailScroll =
                    Math.max(
                            0,
                            Math.min(
                                    this.maxDetailScroll(),
                                    (int) Math.round(var5 * this.maxDetailScroll())));
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
        var1 = MenuScale207.event(this, var1);
        var2 = MenuScale207.coordinate(this, var2);
        var4 = MenuScale207.coordinate(this, var4);
        if (this.dragging != null) {
            this.scrollTo(var1.y());
            return true;
        } else {
            return super.mouseDragged(var1, var2, var4);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent var1) {
        var1 = MenuScale207.event(this, var1);
        if (this.dragging != null) {
            this.dragging = null;
            return true;
        } else {
            return super.mouseReleased(var1);
        }
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        var1 = MenuScale207.coordinate(this, var1);
        var3 = MenuScale207.coordinate(this, var3);
        if (this.confirmation == null && !this.qualityOpen) {
            ShopOffer var9 = this.compareHover206(var1, var3);
            if (var9 != null
                    && ShopComparison.scroll(
                            this.minecraft.player, ShopComparison.kind(var9), var9.stack(), var7)) {
                return true;
            }
        }

        if (var7 == 0.0) {
            return super.mouseScrolled(var1, var3, var5, var7);
        }

        int var12 = var7 > 0.0 ? -1 : 1;
        if (this.confirmation != null) {
            return true;
        }

        if (this.qualityOpen) {
            if (!this.inQualityMenu(var1, var3)
                    && !hit(var1, var3, this.qualityX(), this.qualityY(), this.qualityW(), 17)) {
                return true;
            }

            this.qualityOffset =
                    Math.max(0, Math.min(this.qualityMaxOffset(), this.qualityOffset + var12));
            return true;
        } else {
            if (hit(
                    var1,
                    var3,
                    this.x,
                    this.categoryTop(),
                    this.railW,
                    this.categoryBottom() - this.categoryTop())) {
                this.categoryOffset =
                        Math.max(
                                0,
                                Math.min(
                                        Math.max(
                                                0,
                                                this.categories.size() - this.visibleCategories()),
                                        this.categoryOffset + var12));
            } else if (hit(
                    var1,
                    var3,
                    this.detailsX,
                    this.detailTop(),
                    this.detailsW,
                    this.detailBottom() - this.detailTop())) {
                this.detailScroll =
                        Math.max(
                                0,
                                Math.min(this.maxDetailScroll(), this.detailScroll + var12 * 15));
            } else {
                if (!hit(
                        var1,
                        var3,
                        this.listX,
                        this.listTop,
                        this.listW,
                        this.listBottom - this.listTop)) {
                    return super.mouseScrolled(var1, var3, var5, var7);
                }

                this.listOffset =
                        Math.max(0, Math.min(this.maxListOffset(), this.listOffset + var12));
            }

            return true;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent var1) {
        if (var1.key() == 256) {
            if (this.confirmation != null) {
                this.confirmation = null;
                this.repairConfirmedStack = null;
                return true;
            } else if (this.qualityOpen) {
                this.qualityOpen = false;
                return true;
            } else if (this.searchBox != null && this.searchBox.isFocused()) {
                this.blurSearch();
                return true;
            } else {
                this.onClose();
                return true;
            }
        } else {
            if (this.qualityOpen) {
                return true;
            }

            if (this.searchBox != null && this.searchBox.isFocused()) {
                return this.searchBox.keyPressed(var1) || super.keyPressed(var1);
            }

            if (this.confirmation != null) {
                return true;
            }

            if (var1.key() != 264 && var1.key() != 265) {
                return super.keyPressed(var1);
            }

            if (this.offers.isEmpty()) {
                return true;
            }

            int var2 = -1;

            for (int var3 = 0; var3 < this.offers.size(); var3++) {
                if (this.offers.get(var3).trigger() == this.selectedTrigger) {
                    var2 = var3;
                }
            }

            var2 =
                    Math.max(
                            0,
                            Math.min(this.offers.size() - 1, var2 + (var1.key() == 264 ? 1 : -1)));
            this.choose(this.offers.get(var2));
            if (var2 < this.listOffset) {
                this.listOffset = var2;
            }

            if (var2 >= this.listOffset + this.visibleRows()) {
                this.listOffset = var2 - this.visibleRows() + 1;
            }

            return true;
        }
    }

    private void text(GuiGraphics var1, String var2, int var3, int var4, int var5, int var6) {
        String var7 = var2 == null ? "" : var2;
        if (this.font.width(var7) > var6) {
            var7 =
                    this.font.plainSubstrByWidth(var7, Math.max(1, var6 - this.font.width("…")))
                            + "…";
        }

        var1.drawString(this.font, var7, var3, var4, var5, false);
    }

    private void button(
            GuiGraphics var1, int var2, int var3, int var4, int var5, String var6, boolean var7) {
        QuestJournalArt.button(
                var1,
                this.font,
                var2,
                var3,
                var4,
                var5,
                var6,
                hit(this.lastMouseX, this.lastMouseY, var2, var3, var4, var5),
                var7,
                false);
    }

    private void item(GuiGraphics var1, ShopOffer var2, int var3, int var4, int var5) {
        Identifier var6 =
                ShopIcons.spellId(var2) <= 0 && ShopDyeRendering.allowSprite(var2.sprite(), var2)
                        ? ShopModel.spriteTexture(var2.sprite())
                        : null;
        var1.pose().pushMatrix();
        var1.pose().translate(var3, var4);
        var1.pose().scale(var5 / 16.0F, var5 / 16.0F);
        if (var6 != null) {
            var1.blit(RenderPipelines.GUI_TEXTURED, var6, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
        } else {
            var1.renderItem(var2.stack(), 0, 0);
            var1.renderItemDecorations(this.font, var2.stack(), 0, 0);
        }

        var1.pose().popMatrix();
        int var7 = ShopIcons.spellId(var2);
        if (var7 > 0 && !ShopIcons.isLauncher(var2.stack())) {
            int var8 = Math.max(8, var5 / 2);
            var1.pose().pushMatrix();
            var1.pose().translate(var3 + var5 - var8 + 2, var4 + var5 - var8 + 2);
            var1.pose().scale(var8 / 16.0F, var8 / 16.0F);
            var1.renderItem(ShopIcons.launcher(var7), 0, 0);
            var1.pose().popMatrix();
        }
    }

    private void scrollbar(
            GuiGraphics var1,
            int var2,
            int var3,
            int var4,
            int var5,
            int var6,
            int var7,
            int var8) {
        if (var6 > 0 && var4 > var3) {
            int var9 = var4 - var3;
            int var10 = Math.min(var9, Math.max(9, var9 * var7 / Math.max(1, var8)));
            int var11 = var3 + var5 * Math.max(0, var9 - var10) / var6;
            GuiBackdrop1132.fill(var1, var2, var3, var2 + 2, var4, 678317612, this);
            GuiBackdrop1132.fill(var1, var2, var11, var2 + 2, var11 + var10, -1435611604, this);
        }
    }

    private void shopParchment(
            GuiGraphics var1, int var2, int var3, int var4, int var5, boolean var6) {
        Identifier var7 =
                Identifier.fromNamespaceAndPath(
                        "att2",
                        var6
                                ? "textures/quest_book/parchment_left.png"
                                : "textures/quest_book/parchment_right.png");
        int[] var8 = new int[] {var2, var2 + 8, var2 + var4 - 8};
        int[] var9 = new int[] {var3, var3 + 8, var3 + var5 - 8};
        int[] var10 = new int[] {8, var4 - 16, 8};
        int[] var11 = new int[] {8, var5 - 16, 8};
        int[] var12 = new int[] {0, 16, 276};
        int[] var13 = new int[] {0, 16, 344};
        int[] var14 = new int[] {16, 260, 16};
        int[] var15 = new int[] {16, 328, 16};

        for (int var16 = 0; var16 < 3; var16++) {
            for (int var17 = 0; var17 < 3; var17++) {
                if (var10[var17] > 0 && var11[var16] > 0) {
                    var1.blit(
                            RenderPipelines.GUI_TEXTURED,
                            var7,
                            var8[var17],
                            var9[var16],
                            var12[var17],
                            var13[var16],
                            var10[var17],
                            var11[var16],
                            var14[var17],
                            var15[var16],
                            292,
                            360);
                }
            }
        }
    }

    private void navigationButton(
            GuiGraphics var1, int var2, int var3, boolean var4, boolean var5) {
        this.button(var1, var2, var3, 26, 20, "", var5);
        int var6 = var5 ? -793924 : -6649232;
        int var7 = var2 + 13;

        for (int var8 = 0; var8 < 8; var8++) {
            int var9 = 3 - Math.min(var8, 7 - var8);
            int var10 = var4 ? var7 + 1 - var9 : var7 - 2 + var9;
            GuiBackdrop1132.fill(
                    var1, var10, var3 + 6 + var8, var10 + 2, var3 + 7 + var8, var6, this);
        }
    }

    @Override
    public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
        var2 = MenuScale207.coordinate(this, var2);
        var3 = MenuScale207.coordinate(this, var3);
        MenuScale207.begin(this, var1);
        GuiBackdrop1132.blur(var1, this);
        GuiBackdrop1132.gradient(var1, 0, 0, this.width, this.height, -821030116, -317386734, this);
        QuestJournalArt.cover(var1, this.x, this.y, this.w, this.h);
        GuiBackdrop1132.fill(
                var1, this.x + 6, this.y + 6, this.x + this.w - 6, this.y + 43, -13030880, this);
        this.shopParchment(var1, this.listX, this.y + 46, this.listW, this.h - 54, true);
        this.shopParchment(var1, this.detailsX, this.y + 46, this.detailsW, this.h - 54, false);
        MenuScale207.end(var1);
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        var2 = MenuScale207.coordinate(this, var2);
        var3 = MenuScale207.coordinate(this, var3);
        MenuScale207.begin(this, var1);
        this.lastMouseX = var2;
        this.lastMouseY = var3;
        super.render(var1, var2, var3, var4);
        if (this.w > 0) {
            this.renderHeader(var1);
            this.renderCategories(var1);
            this.renderRows(var1);
            this.renderDetails(var1);
            if (this.confirmation != null) {
                this.renderConfirmation(var1);
            } else if (this.qualityOpen) {
                this.renderQualityMenu(var1);
            } else {
                this.renderTooltip(var1, var2, var3);
            }
        }

        MenuScale207.end(var1);
    }

    private void renderHeader(GuiGraphics var1) {
        ShopModel var2 = ShopModel.get();
        ShopCatalog var3 = var2.catalog();
        Component var4 =
                var2.isRepairView() && var2.repairTitle() != null
                        ? var2.repairTitle()
                        : (var2.seller() != null
                                ? ShopNpcNames.resolve(var2.seller().npcId())
                                : ModLanguageManager.get(this.type.titleKey()));
        this.text(var1, plain(var4), this.x + 14, this.y + 13, -793924, this.w - 200);
        String var5 = ModLanguageManager.getString(this.type.titleKey());
        if (ShopSmithData.level() != null) {
            var5 =
                    var5
                            + " · "
                            + ModLanguageManager.format(
                                    "smith.ui.level", "n", ShopSmithData.level());
        }

        if (ShopSmithData.discount() != null) {
            var5 =
                    var5
                            + " · "
                            + ModLanguageManager.format(
                                    "smith.ui.discount", "n", ShopSmithData.discount());
        }

        this.text(var1, var5, this.x + 14, this.y + 28, -4215155, this.w - 200);
        String var6 = var3.usesEsc() ? "ESC" : "CHRONOTON";
        String var7 =
                ScoreCache.get(var6).isPresent()
                        ? String.valueOf(ScoreCache.get(var6).getAsInt())
                        : "?";
        String var8 = var3.usesEsc() ? "CHRONOTON" : "ESC";
        String var9 =
                ScoreCache.get(var8).isPresent()
                        ? String.valueOf(ScoreCache.get(var8).getAsInt())
                        : "?";
        this.text(
                var1,
                (var3.usesEsc() ? "ESC" : "Chronotons") + ": " + var7,
                this.x + this.w - 179,
                this.y + 11,
                -3562150,
                147);
        this.text(
                var1,
                (var3.usesEsc() ? "Chronotons" : "ESC") + ": " + var9,
                this.x + this.w - 179,
                this.y + 25,
                -4215155,
                147);
        QuestJournalArt.closeSeal(
                var1,
                this.x + this.w - 17,
                this.y + 17,
                hit(this.lastMouseX, this.lastMouseY, this.x + this.w - 24, this.y + 8, 16, 16));
    }

    private void renderCategories(GuiGraphics var1) {
        this.text(var1, this.tr("categories"), this.x + 13, this.y + 51, -3562150, this.railW - 25);

        for (int var2 = this.categoryOffset;
                var2
                        < Math.min(
                                this.categories.size(),
                                this.categoryOffset + this.visibleCategories());
                var2++) {
            int var3 = this.categoryTop() + (var2 - this.categoryOffset) * 24;
            String var4 = this.categories.get(var2);
            boolean var5 = var4.equals(this.filter);
            boolean var6 =
                    hit(this.lastMouseX, this.lastMouseY, this.x + 8, var3, this.railW - 16, 21);
            if (var5 || var6) {
                GuiBackdrop1132.fill(
                        var1,
                        this.x + 8,
                        var3,
                        this.x + this.railW - 8,
                        var3 + 21,
                        var5 ? -10271951 : -12308189,
                        this);
            }

            if (var5) {
                GuiBackdrop1132.fill(
                        var1, this.x + 8, var3, this.x + 10, var3 + 21, -3562150, this);
            }

            String var7 =
                    var4.equals("@smith_tools")
                            ? ModLanguageManager.getString("smith.ui.tools")
                            : (var4.isEmpty()
                                    ? ModLanguageManager.getString("shop.category.all")
                                    : var4.replace('<', ' ')
                                            .replace('>', ' ')
                                            .replace('-', ' ')
                                            .strip());
            this.text(
                    var1, var7, this.x + 15, var3 + 6, var5 ? -793924 : -4150644, this.railW - 31);
        }

        if (ShopModel.get().hasRepair()) {
            this.button(
                    var1,
                    this.x + 8,
                    this.footerY(),
                    this.railW - 16,
                    20,
                    ModLanguageManager.getString("smith.ui.repairHeld"),
                    this.canRepairHeld());
        }

        for (int var8 = 0; var8 < this.actions.size(); var8++) {
            int var9 =
                    this.footerY()
                            - 25
                                    * (this.actions.size()
                                            - var8
                                            - (ShopModel.get().hasRepair() ? 0 : 1));
            this.button(
                    var1,
                    this.x + 8,
                    var9,
                    this.railW - 16,
                    20,
                    plain(this.actions.get(var8).label()),
                    true);
        }
    }

    private void renderRows(GuiGraphics var1) {
        QuestJournalArt.magnifier(var1, this.listX + 8, this.y + 54, -9548244);
        QuestJournalArt.dottedRule(
                var1,
                this.listX + 8,
                this.listX + this.listW - (ShopModel.get().hasReset() ? 30 : 8),
                this.y + 68,
                -1433769412);
        if (ShopModel.get().hasReset()) {
            this.renderResetIcon(var1);
        }

        this.renderQualityControl(var1);
        String var2 =
                this.offers.isEmpty()
                        ? "0"
                        : (this.listW < 150
                                ? String.valueOf(this.offers.size())
                                : this.listOffset
                                        + 1
                                        + "–"
                                        + Math.min(
                                                this.offers.size(),
                                                this.listOffset + this.visibleRows())
                                        + " / "
                                        + this.offers.size());
        this.text(
                var1,
                this.tr("products"),
                this.listX + 8,
                this.y + (this.qualityWrapped() ? 94 : 73),
                -9548244,
                this.listW - (this.listW < 150 ? 50 : 76));
        this.text(
                var1,
                var2,
                this.listX + this.listW - (this.listW < 150 ? 44 : 68),
                this.y + (this.qualityWrapped() ? 94 : 73),
                -9548244,
                this.listW < 150 ? 38 : 62);
        var1.enableScissor(
                this.listX + 6, this.listTop, this.listX + this.listW - 5, this.listBottom);

        for (int var3 = this.listOffset;
                var3 < Math.min(this.offers.size(), this.listOffset + this.visibleRows());
                var3++) {
            ShopOffer var4 = this.offers.get(var3);
            int var5 = this.listTop + (var3 - this.listOffset) * 31;
            boolean var6 = var4.trigger() == this.selectedTrigger;
            boolean var7 =
                    hit(
                            this.lastMouseX,
                            this.lastMouseY,
                            this.listX + 8,
                            var5,
                            this.listW - 18,
                            31);
            if (var6 || var7) {
                GuiBackdrop1132.fill(
                        var1,
                        this.listX + 6,
                        var5,
                        this.listX + this.listW - 9,
                        var5 + 31 - 2,
                        var6 ? 1116494370 : 578440990,
                        this);
            }

            if (var6) {
                GuiBackdrop1132.fill(
                        var1, this.listX + 6, var5, this.listX + 8, var5 + 31 - 2, -7463132, this);
            }

            this.item(var1, var4, this.listX + 11, var5 + 5, 20);
            this.text(var1, this.name(var4), this.listX + 37, var5 + 4, -12966894, this.listW - 49);
            this.text(
                    var1,
                    var4.price() == null ? this.tr("unknownPrice") : plain(var4.price()),
                    this.listX + 37,
                    var5 + 17,
                    -9548244,
                    this.listW - 49);
            QuestJournalArt.dottedRule(
                    var1, this.listX + 37, this.listX + this.listW - 11, var5 + 31 - 1, 1519020604);
        }

        if (this.offers.isEmpty()) {
            String var8 =
                    ShopModel.get().isCollecting()
                            ? ModLanguageManager.getString("shop.loading")
                            : (!this.search.isEmpty()
                                    ? this.tr("noResults")
                                    : ModLanguageManager.getString("shop.empty"));
            int var9 = this.listTop + 15;

            for (FormattedCharSequence var11 :
                    this.font.split(Component.literal(var8), Math.max(24, this.listW - 20))) {
                var1.drawString(this.font, var11, this.listX + 10, var9, -9548244, false);
                var9 += 13;
            }
        }

        var1.disableScissor();
        this.scrollbar(
                var1,
                this.listX + this.listW - 7,
                this.listTop,
                this.listBottom,
                this.listOffset,
                this.maxListOffset(),
                this.visibleRows(),
                this.offers.size());
        this.navigationButton(var1, this.previousX(), this.buttonY(), false, this.listOffset > 0);
        this.navigationButton(
                var1, this.nextX(), this.buttonY(), true, this.listOffset < this.maxListOffset());
        this.text(
                var1,
                this.tr("selectHint"),
                this.listX + 43,
                this.buttonY() + 6,
                -9548244,
                Math.max(1, this.listW - 86));
    }

    private void renderDetails(GuiGraphics var1) {
        ShopOffer var2 = this.selected();
        int var3 = this.detailsX + 12;
        int var4 = this.detailsW - 28;
        this.text(var1, this.tr("details"), var3, this.y + 54, -9548244, var4);
        if (var2 != null) {
            boolean var5 = this.h < 300;
            this.item(
                    var1,
                    var2,
                    var5 ? var3 : this.detailsX + (this.detailsW - 32) / 2,
                    this.y + 68,
                    var5 ? 24 : 32);
            int var6 = var5 ? var3 + 32 : var3;
            int var7 = var5 ? var4 - 32 : var4;
            int var8 = this.y + (var5 ? 71 : 105);
            List var9 = this.font.split(Component.literal(this.name(var2)), Math.max(24, var7));

            for (int var10 = 0; var10 < Math.min(2, var9.size()); var10++) {
                var1.drawString(
                        this.font,
                        (FormattedCharSequence) var9.get(var10),
                        var6,
                        var8,
                        -12966894,
                        false);
                var8 += 12;
            }

            QuestJournalArt.ornamentRule(
                    var1, var3, var3 + var4, this.y + (var5 ? 103 : 130), -1433769412);
        }

        ArrayList<Component> var13 = new ArrayList();
        if (var2 == null) {
            if (ShopModel.get().isRepairView() && !ShopModel.get().repairLines().isEmpty()) {
                var13.addAll(ShopModel.get().repairLines());
            } else {
                var13.add(ModLanguageManager.get("shop.modern.pickItem"));
                var13.add(ModLanguageManager.get("shop.modern.purchaseHint"));
            }
        } else {
            List var14 = ShopTooltips.forOffer(var2, this.displayName(var2));

            for (int var16 = 1; var16 < var14.size(); var16++) {
                Component var19 = (Component) var14.get(var16);
                if (var2.price() == null || !var19.getString().equals(var2.price().getString())) {
                    var13.add(var19);
                }
            }

            if (var13.isEmpty()) {
                var13.add(ModLanguageManager.get("shop.modern.noDescription"));
            }
        }

        ShopCatalog var15 = ShopModel.get().catalog();
        if (var2 == null) {
            for (Component var28 :
                    new Component[] {var15.discount(), var15.powderStock(), var15.remaining()}) {
                if (var28 != null && !ShopTellraws.isForceResetAction(var28.getString())) {
                    var13.add(var28);
                }
            }
        }

        int var18 = 14;

        for (Component var25 : var13) {
            var18 += this.font.split(var25, Math.max(24, var4 - 5)).size() * 12 + 3;
        }

        this.detailHeight = var18;
        this.detailScroll = Math.min(this.detailScroll, this.maxDetailScroll());
        if (this.detailTop() < this.detailBottom()) {
            var1.enableScissor(var3 - 3, this.detailTop(), var3 + var4 + 3, this.detailBottom());
            int var22 = this.detailTop() + 14 - this.detailScroll;

            for (Component var29 : var13) {
                for (FormattedCharSequence var12 : this.font.split(var29, Math.max(24, var4 - 5))) {
                    if (var22 + 12 >= this.detailTop() && var22 < this.detailBottom()) {
                        var1.drawString(this.font, var12, var3, var22, -9548244, false);
                    }

                    var22 += 12;
                }

                var22 += 3;
            }

            var1.disableScissor();
            this.scrollbar(
                    var1,
                    this.detailsX + this.detailsW - 13,
                    this.detailTop(),
                    this.detailBottom(),
                    this.detailScroll,
                    this.maxDetailScroll(),
                    Math.max(1, this.detailBottom() - this.detailTop()),
                    this.detailHeight);
        }

        int var23 = this.buttonY() - 28;
        this.text(var1, this.tr("price"), var3, var23, -9548244, var4);
        String var27 =
                var2 == null
                        ? "—"
                        : (var2.price() == null ? this.tr("unknownPrice") : plain(var2.price()));
        this.text(var1, var27, var3, var23 + 12, -12966894, var4);
        boolean var30 =
                var2 != null
                        && var2.trigger() >= 0
                        && this.isPriceKnown(var2)
                        && !ShopModel.get().isCollecting();
        this.button(
                var1,
                this.detailsX + 12,
                this.buttonY(),
                this.detailsW - 24,
                20,
                this.tr("buy"),
                var30);
        if (System.currentTimeMillis() < this.requestNoticeUntil) {
            GuiBackdrop1132.fill(
                    var1,
                    this.detailsX + 8,
                    this.y + this.h - 78,
                    this.detailsX + this.detailsW - 8,
                    this.y + this.h - 48,
                    -252980320,
                    this);
            this.text(
                    var1,
                    this.tr("sent"),
                    this.detailsX + 13,
                    this.y + this.h - 65,
                    -12966894,
                    this.detailsW - 26);
        }
    }

    private void renderConfirmation(GuiGraphics var1) {
        GuiBackdrop1132.fill(
                var1, this.x, this.y, this.x + this.w, this.y + this.h, -1442840576, this);
        int var2 = this.x + this.w / 2;
        int var3 = this.y + this.h / 2;
        QuestJournalArt.cover(var1, var2 - 135, var3 - 60, 270, 124);
        this.shopParchment(var1, var2 - 129, var3 - 54, 258, 112, false);
        this.text(
                var1,
                this.confirmation.trigger() == 414
                        ? ModLanguageManager.getString("smith.ui.confirmRepair")
                        : this.tr("confirmReset"),
                var2 - 117,
                var3 - 40,
                -12966894,
                234);
        Component var4 =
                this.confirmation.tip() != null
                        ? this.confirmation.tip()
                        : this.confirmation.label();
        int var5 = var3 - 20;

        for (FormattedCharSequence var7 : this.font.split(var4, 226)) {
            var1.drawString(this.font, var7, var2 - 117, var5, -9548244, false);
            var5 += 12;
            if (var5 > var3 + 15) {
                break;
            }
        }

        this.button(var1, var2 - 122, var3 + 32, 116, 20, this.tr("confirm"), true);
        this.button(var1, var2 + 6, var3 + 32, 116, 20, this.tr("cancel"), true);
    }

    private void toggleOwnedSpells() {
        this.blurSearch();
        HUDConfig var1 = HUDConfig.get();
        var1.shopHideOwnedSpells = !var1.shopHideOwnedSpells;
        this.selectedTrigger = -1;
        this.detailScroll = 0;
        this.listOffset = 0;
        this.rebuild();
        HUDConfig.save();
    }

    private void renderQualityControl(GuiGraphics var1) {
        int var2 = this.qualityX();
        int var3 = this.qualityY();
        int var4 = this.qualityW();
        boolean var5 = hit(this.lastMouseX, this.lastMouseY, var2, var3, var4, 17);
        if (this.type == ShopType.DAHAL) {
            boolean var16 = HUDConfig.get().shopHideOwnedSpells;
            int var17 = var2 + 4;
            int var8 = var3 + 4;
            byte var9 = 9;
            int var10 = var5 ? -12242659 : -9544644;
            GuiBackdrop1132.fill(var1, var17, var8, var17 + var9, var8 + 1, var10, this);
            GuiBackdrop1132.fill(
                    var1, var17, var8 + var9 - 1, var17 + var9, var8 + var9, var10, this);
            GuiBackdrop1132.fill(var1, var17, var8 + 1, var17 + 1, var8 + var9 - 1, var10, this);
            GuiBackdrop1132.fill(
                    var1, var17 + var9 - 1, var8 + 1, var17 + var9, var8 + var9 - 1, var10, this);
            if (var16) {
                int var11 = -12889811;

                for (int var12 = 0; var12 < 2; var12++) {
                    GuiBackdrop1132.fill(
                            var1,
                            var17 + 1 + var12,
                            var8 + 3 + var12,
                            var17 + 3 + var12,
                            var8 + 5 + var12,
                            var11,
                            this);
                }

                for (int var19 = 0; var19 < 4; var19++) {
                    GuiBackdrop1132.fill(
                            var1,
                            var17 + 3 + var19,
                            var8 + 5 - var19,
                            var17 + 5 + var19,
                            var8 + 7 - var19,
                            var11,
                            this);
                }
            }

            String var18 = ModLanguageManager.getString("shop.hide_owned.short");
            int var20 = this.font.width(var18);
            int var13 = var17 + var9 + 5;
            int var14 = Math.max(1, var2 + var4 - 4 - var13);
            float var15 = Math.min(1.0F, (float) var14 / Math.max(1, var20));
            var1.pose().pushMatrix();
            var1.pose()
                    .translate(
                            var13 + (var14 - var20 * var15) / 2.0F,
                            var3 + (17.0F - 9.0F * var15) / 2.0F);
            var1.pose().scale(var15, var15);
            var1.drawString(this.font, var18, 0, 0, var10, false);
            var1.pose().popMatrix();
        } else {
            GuiBackdrop1132.fill(
                    var1,
                    var2,
                    var3,
                    var2 + var4,
                    var3 + 17,
                    !this.qualityOpen && !var5 ? 410407728 : 864183101,
                    this);
            GuiBackdrop1132.fill(var1, var2, var3 + 16, var2 + var4, var3 + 17, -2004720835, this);
            this.text(
                    var1,
                    ShopQuality.label(this.qualityFilter),
                    var2 + 4,
                    var3 + 5,
                    ShopQuality.color(this.qualityFilter, -9548244),
                    var4 - 16);
            int var6 = var2 + var4 - 9;

            for (int var7 = 0; var7 < 3; var7++) {
                GuiBackdrop1132.fill(
                        var1,
                        var6 - 2 + var7,
                        var3 + 7 + var7,
                        var6 + 3 - var7,
                        var3 + 8 + var7,
                        -9548244,
                        this);
            }
        }
    }

    private void renderQualityMenu(GuiGraphics var1) {
        int var2 = this.qualityMenuX();
        int var3 = this.qualityMenuY();
        int var4 = this.qualityMenuW();
        int var5 = this.qualityMenuH();
        GuiBackdrop1132.fill(
                var1, var2 + 2, var3 + 2, var2 + var4 + 2, var3 + var5 + 2, 1711276032, this);
        GuiBackdrop1132.fill(
                var1, var2 - 1, var3 - 1, var2 + var4 + 1, var3 + var5 + 1, -8232131, this);
        GuiBackdrop1132.fill(var1, var2, var3, var2 + var4, var3 + var5, -992858, this);

        for (int var6 = this.qualityOffset;
                var6 < Math.min(this.qualities.size(), this.qualityOffset + this.qualityVisible());
                var6++) {
            String var7 = this.qualities.get(var6);
            int var8 = var3 + 2 + (var6 - this.qualityOffset) * 18;
            boolean var9 = hit(this.lastMouseX, this.lastMouseY, var2 + 2, var8, var4 - 4, 18);
            if (var7.equals(this.qualityFilter) || var9) {
                GuiBackdrop1132.fill(
                        var1,
                        var2 + 2,
                        var8,
                        var2 + var4 - 2,
                        var8 + 18,
                        var7.equals(this.qualityFilter) ? 1149395773 : 578970429,
                        this);
            }

            this.text(
                    var1,
                    ShopQuality.label(var7),
                    var2 + 7,
                    var8 + 5,
                    ShopQuality.color(var7, -12966894),
                    var4 - 19);
        }

        this.scrollbar(
                var1,
                var2 + var4 - 4,
                var3 + 2,
                var3 + var5 - 2,
                this.qualityOffset,
                this.qualityMaxOffset(),
                this.qualityVisible(),
                this.qualities.size());
    }

    private void renderResetIcon(GuiGraphics var1) {
        int var2 = this.resetX() + 7;
        int var3 = this.y + 58;
        boolean var4 =
                hit(this.lastMouseX, this.lastMouseY, this.resetX() - 2, this.y + 50, 20, 17);
        if (var4) {
            GuiBackdrop1132.fill(
                    var1,
                    this.resetX() - 2,
                    this.y + 50,
                    this.resetX() + 18,
                    this.y + 67,
                    578970429,
                    this);
        }

        int var5 = var4 ? -12966894 : -9548244;

        for (int var6 = -5; var6 <= 5; var6++) {
            for (int var7 = -5; var7 <= 5; var7++) {
                int var8 = var7 * var7 + var6 * var6;
                if (var8 >= 14 && var8 <= 30 && (var7 <= 0 || var6 >= -1)) {
                    GuiBackdrop1132.fill(
                            var1,
                            var2 + var7,
                            var3 + var6,
                            var2 + var7 + 1,
                            var3 + var6 + 1,
                            var5,
                            this);
                }
            }
        }

        GuiBackdrop1132.fill(var1, var2 + 3, var3 - 5, var2 + 5, var3 - 1, var5, this);
        GuiBackdrop1132.fill(var1, var2 + 1, var3 - 2, var2 + 5, var3, var5, this);
    }

    private List<Component> repairTooltip() {
        ArrayList var1 = new ArrayList();
        ItemStack var2 = this.held();
        var1.add(ModLanguageManager.get("smith.ui.repairHeld"));
        if (var2.isEmpty()) {
            var1.add(ModLanguageManager.get("smith.ui.emptyHand"));
        } else {
            var1.add(var2.getHoverName());
            ItemRarity var3 = ItemRarity.fromStack(var2);
            var1.add(
                    Component.literal(
                            ModLanguageManager.format(
                                    "smith.ui.quality",
                                    "name",
                                    var3 == null ? "?" : ShopSmithData.rarityName(var3.id))));
            String var4 = ShopSmithData.heldQuote(var2);
            if (var4 == null) {
                ShopSmithData.retryMissing(ShopModel.get());
            }

            var1.add(
                    Component.literal(
                            var4 == null
                                    ? ModLanguageManager.getString("smith.ui.pendingPrice")
                                    : ModLanguageManager.format(
                                            "smith.ui.heldPrice", "price", var4)));
            if (!var2.isDamageableItem()) {
                var1.add(ModLanguageManager.get("smith.ui.notRepairable"));
            } else if (var2.getDamageValue() <= 0) {
                var1.add(ModLanguageManager.get("smith.ui.undamaged"));
            }
        }

        var1.add(ModLanguageManager.get("smith.ui.priceTable"));
        List var10 = ShopSmithData.quoteTable();
        if (var10.isEmpty()) {
            var1.add(ModLanguageManager.get("smith.ui.pendingPrice"));
        } else {
            for (String var5 : List.of("com", "unc", "rar", "epi_esc", "epi", "leg", "ult")) {
                String var6 = ShopSmithData.quote(var5);
                if (var6 != null) {
                    int var7 =
                            switch (var5) {
                                case "com" -> 11184810;
                                case "unc" -> 43520;
                                case "rar" -> 5592575;
                                case "epi_esc" -> 16733695;
                                case "epi" -> 11141290;
                                case "leg" -> 16755200;
                                case "ult" -> 5635925;
                                default -> 16777215;
                            };
                    var1.add(
                            Component.empty()
                                    .append(
                                            Component.literal(ShopSmithData.rarityName(var5))
                                                    .withColor(var7))
                                    .append(Component.literal(": " + var6)));
                }
            }
        }

        return var1;
    }

    private ShopOffer compareHover206(double var1, double var3) {
        ShopOffer var5 = this.rowAt(var1, var3);
        if (var5 == null
                && hit(
                        var1,
                        var3,
                        this.detailsX + 8,
                        this.y + 65,
                        this.detailsW - 16,
                        Math.max(1, this.detailTop() - this.y - 69))) {
            var5 = this.selected();
        }

        return var5;
    }

    private void renderTooltip(GuiGraphics var1, int var2, int var3) {
        if (hit(var2, var3, this.qualityX(), this.qualityY(), this.qualityW(), 17)) {
            if (this.type == ShopType.DAHAL) {
                MenuScale207.tooltipSimple(
                        var1,
                        this.font,
                        List.of(ModLanguageManager.get("shop.hide_owned.tooltip")),
                        Optional.empty(),
                        var2,
                        var3,
                        this);
                return;
            }

            MenuScale207.tooltipSimple(
                    var1,
                    this.font,
                    List.of(
                            ModLanguageManager.get("shop.quality.label"),
                            Component.literal(ShopQuality.label(this.qualityFilter)),
                            ModLanguageManager.get("shop.quality.tip")),
                    Optional.empty(),
                    var2,
                    var3,
                    this);
        } else if (ShopModel.get().hasRepair()
                && hit(var2, var3, this.x + 8, this.footerY(), this.railW - 16, 20)) {
            MenuScale207.tooltipSimple(
                    var1, this.font, this.repairTooltip(), Optional.empty(), var2, var3, this);
        } else if (ShopModel.get().hasReset()
                && hit(var2, var3, this.resetX() - 2, this.y + 50, 20, 17)) {
            ShopAction var14 = this.resetAction();
            ArrayList var15 = new ArrayList();
            var15.add(var14.label());
            if (var14.tip() != null) {
                var15.add(var14.tip());
            }

            var15.add(ModLanguageManager.get("smith.ui.resetTip"));
            MenuScale207.tooltipSimple(var1, this.font, var15, Optional.empty(), var2, var3, this);
        } else {
            ShopOffer var4 = this.rowAt(var2, var3);
            boolean var5 = false;
            if (var4 == null
                    && hit(
                            var2,
                            var3,
                            this.detailsX + 8,
                            this.y + 65,
                            this.detailsW - 16,
                            Math.max(1, this.detailTop() - this.y - 69))) {
                var4 = this.selected();
                var5 = true;
            }

            if (var4 != null) {
                boolean var6 = ShopOriginalItems.contains(var4);
                ArrayList<Component> var7 =
                        new ArrayList<>(
                                var6
                                        ? ShopTooltips.of(ShopQuality.tooltipStack(var4))
                                        : ShopTooltips.forOffer(var4, this.displayName(var4)));
                var7 = new ArrayList<>(ShopQuality.tooltipLines(var4, var7));
                ShopComparison.Kind var8 = ShopComparison.kind(var4);
                boolean var9 =
                        ShopComparison.isHeld(
                                this.minecraft.getWindow().handle(),
                                KeybindManager.compareKeyCode());
                if (var8 != null && this.minecraft.player != null && var9) {
                    MenuScale207.comparison(
                            var1,
                            var7,
                            this.minecraft.player,
                            var8,
                            var4.stack(),
                            var2,
                            var3,
                            this.width,
                            this.height,
                            this);
                } else if (var6) {
                    MenuScale207.tooltipItem(
                            var1, this.font, ShopQuality.tooltipStack(var4), var2, var3, this);
                } else {
                    ArrayList<Component> var10 = var5 ? var7 : new ArrayList();
                    if (!var5) {
                        var10.add(ShopQuality.toolTitle(var4, this.displayName(var4)));
                        if (ShopTooltips.usefulLore(var4.lore())) {
                            for (Component var12 : ShopTooltips.flattenLore(var4.lore())) {
                                String var13 = plain(var12).strip();
                                if (!var13.isEmpty()
                                        && var10.stream()
                                                .noneMatch(
                                                        var1x ->
                                                                plain(var1x)
                                                                        .strip()
                                                                        .equals(var13))) {
                                    var10.add(var12);
                                }
                            }
                        }

                        if (var4.price() != null) {
                            var10.add(Component.literal(plain(var4.price())));
                        }

                        var10.add(
                                ModLanguageManager.get("shop.modern.selectHint")
                                        .withStyle(ChatFormatting.GRAY));
                    }

                    if (ShopComparison.shouldShowHint(var8, var9)) {
                        var10.add(ShopComparison.compareHint().withStyle(ChatFormatting.GRAY));
                    }

                    MenuScale207.tooltipSimple(
                            var1, this.font, var10, Optional.empty(), var2, var3, this);
                }
            } else if (hit(
                            var2,
                            var3,
                            this.detailsX + 10,
                            this.buttonY() - 30,
                            this.detailsW - 20,
                            25)
                    && this.selected() != null
                    && this.selected().price() != null) {
                MenuScale207.tooltipSimple(
                        var1,
                        this.font,
                        List.of(Component.literal(plain(this.selected().price()))),
                        Optional.empty(),
                        var2,
                        var3,
                        this);
            } else if (hit(var2, var3, this.x + 12, this.y + 26, this.w - 200, 12)) {
                ArrayList var16 = new ArrayList();
                var16.add(ModLanguageManager.get(this.type.titleKey()));
                if (ShopSmithData.level() != null) {
                    var16.add(
                            Component.literal(
                                    ModLanguageManager.format(
                                            "smith.ui.level", "n", ShopSmithData.level())));
                }

                if (ShopSmithData.discount() != null) {
                    var16.add(
                            Component.literal(
                                    ModLanguageManager.format(
                                            "smith.ui.discount", "n", ShopSmithData.discount())));
                }

                ShopCatalog var18 = ShopModel.get().catalog();

                for (Component var22 :
                        new Component[] {
                            var18.discount(), var18.powderStock(), var18.remaining()
                        }) {
                    if (var22 != null && !ShopTellraws.isForceResetAction(var22.getString())) {
                        var16.add(var22);
                    }
                }

                if (!var16.isEmpty()) {
                    MenuScale207.tooltipSimple(
                            var1, this.font, var16, Optional.empty(), var2, var3, this);
                }
            }
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
}
