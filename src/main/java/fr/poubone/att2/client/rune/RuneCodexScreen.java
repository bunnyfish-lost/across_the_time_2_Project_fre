package fr.poubone.att2.client.rune;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.data.ScoreCache;
import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.hud.ModToast;
import fr.poubone.att2.client.screen.GuiBackdrop1132;
import fr.poubone.att2.client.shop.MenuScale207;
import fr.poubone.att2.client.shop.ShopTooltips;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

public final class RuneCodexScreen extends Screen {
    private static final String[] TABS = new String[] {"符文合成", "符文之语", "箭矢制作", "其他配方"};
    private static final int TEXT = -1449262;
    private static final int MUTED = -5593704;
    private static final int GOLD = -2178433;
    private static final int LINE = -12567501;
    private static RuneCodexScreen.ViewMemory lastView;
    private boolean restoreSelection;
    private RuneCatalog.Tab tab;
    private int category;
    private int x;
    private int y;
    private int w;
    private int h;
    private int listX;
    private int listW;
    private int rightX;
    private int rightW;
    private int contentTop;
    private int listTop;
    private int listBottom;
    private int footer;
    private int navColumns;
    private int navRows;
    private int listOffset;
    private int detailScroll;
    private int detailHeight;
    private int cooldown;
    private int mx;
    private int my;
    private String search = "";
    private String selectedKey;
    private EditBox searchBox;
    private boolean subscribed;
    private String armedRecipe;
    private List<RuneWorkshopRecipes119.Recipe> entries = List.of();
    private int[] owned = new int[0];
    private int[] pouchOwned = new int[0];
    private List<RuneCodexScreen.Tip> tips = new ArrayList<>();

    private RuneCodexScreen(RuneCatalog.Tab var1) {
        super(Component.literal("符文室"));
        this.tab = normalizeTab(var1);
        this.category = category(this.tab);
        Minecraft var2 = Minecraft.getInstance();
        RuneCodexScreen.ViewMemory var3 = lastView;
        if (var2 != null
                && var2.level != null
                && var3 != null
                && var3.world().get() == var2.level) {
            this.tab = normalizeTab(var3.tab());
            this.category = category(this.tab);
            this.selectedKey = var3.selectedKey();
            this.search = var3.search();
            this.listOffset = var3.listOffset();
            this.detailScroll = var3.detailScroll();
            this.restoreSelection = true;
        }
    }

    private static int category(RuneCatalog.Tab var0) {
        return switch (var0) {
            case SYNTH_LOW, SYNTH_MID, SYNTH_HIGH -> 0;
            case WORDS -> 1;
            case ARROWS -> 2;
            case OTHER -> 3;
            case BONUS -> 0;
        };
    }

    private static RuneCatalog.Tab normalizeTab(RuneCatalog.Tab var0) {
        return var0 != null && var0 != RuneCatalog.Tab.BONUS ? var0 : RuneCatalog.Tab.SYNTH_LOW;
    }

    public RuneCatalog.Tab tab() {
        return this.tab;
    }

    public static void open(RuneCatalog.Tab var0) {
        if (!FlashbackCompat.isInReplay() && HUDConfig.get().runeMenuEnabled) {
            Minecraft var1 = Minecraft.getInstance();
            if (var1.screen instanceof RuneCodexScreen var2) {
                var2.switchTab(var0);
            } else {
                var1.setScreen(new RuneCodexScreen(var0));
            }
        }
    }

    public void switchTab(RuneCatalog.Tab var1) {
        if (var1 != null) {
            this.tab = normalizeTab(var1);
            this.category = category(this.tab);
            this.selectedKey = null;
            this.search = "";
            this.listOffset = this.detailScroll = 0;
            this.armedRecipe = null;
            if (this.searchBox != null) {
                this.searchBox.setValue("");
            }

            this.rebuild();
            RuneMapDetails207.onTab(this.tab);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        MenuScale207.prepare(this);
        this.clearWidgets();
        this.w = Math.max(1, Math.min(680, this.width - 20));
        this.h = Math.max(1, Math.min(372, this.height - 20));
        this.x = (this.width - this.w) / 2;
        this.y = (this.height - this.h) / 2;
        this.navColumns = TABS.length;
        this.navRows = 1;
        this.contentTop = this.navY() + 23 + 8;
        this.footer = this.y + this.h - 28;
        this.listX = this.x + 8;
        this.listW = Math.max(74, Math.min(222, this.w * 32 / 100));
        this.rightX = this.listX + this.listW + 9;
        this.rightW = Math.max(24, this.x + this.w - 9 - this.rightX);
        this.listTop = this.contentTop + 28;
        this.listBottom = Math.max(this.listTop, this.footer - 5);
        this.searchBox =
                new EditBox(
                        this.font,
                        this.listX + 4,
                        this.contentTop + 4,
                        Math.max(10, this.listW - 12),
                        18,
                        Component.literal("搜索配方"));
        this.searchBox.setMaxLength(64);
        this.searchBox.setValue(this.search);
        this.searchBox.setTextColor(-1449262);
        this.searchBox.setResponder(
                var1 -> {
                    this.search = var1;
                    this.listOffset = 0;
                    this.armedRecipe = null;
                    this.rebuild();
                });
        this.addRenderableWidget(this.searchBox);
        if (!this.subscribed) {
            this.subscribed = true;
            RuneCodexModel.get().onScreenOpened();
        }

        this.rebuild();
        RuneMapDetails207.onTab(this.tab);
    }

    @Override
    public void removed() {
        MenuScale207.reset(this);
        this.armedRecipe = null;
        Minecraft var1 = Minecraft.getInstance();
        if (this.subscribed && var1 != null && var1.level != null) {
            lastView =
                    new RuneCodexScreen.ViewMemory(
                            new WeakReference<>(var1.level),
                            this.tab,
                            this.selectedKey,
                            this.search,
                            this.listOffset,
                            this.detailScroll);
        }

        if (this.subscribed) {
            this.subscribed = false;
            RuneMapDetails207.onClosed();
            RuneWordCraft119.clear();
            RuneCodexModel.get().onScreenClosed();
        }

        super.removed();
    }

    private int visible() {
        return Math.max(1, (this.listBottom - this.listTop) / 29);
    }

    private int maxList() {
        return Math.max(0, this.entries.size() - this.visible());
    }

    private RuneWorkshopRecipes119.Recipe selected() {
        return this.entries.stream()
                .filter(var1 -> var1.key().equals(this.selectedKey))
                .findFirst()
                .orElse(null);
    }

    private void rebuild() {
        String var1 = this.search.toLowerCase(Locale.ROOT).strip();
        this.entries =
                RuneWorkshopRecipes119.category(this.category).stream()
                        .filter(
                                var1x ->
                                        var1.isEmpty()
                                                || var1x.name()
                                                        .getString()
                                                        .toLowerCase(Locale.ROOT)
                                                        .contains(var1)
                                                || var1x.key().contains(var1))
                        .toList();
        if (this.selected() == null) {
            String var2 =
                    this.category == 0
                            ? RuneCatalog.runes(this.tab).stream()
                                    .findFirst()
                                    .map(var0 -> "r:" + var0.id())
                                    .orElse("")
                            : "";
            this.selectedKey =
                    this.entries.stream()
                            .filter(var1x -> var1x.key().equals(var2))
                            .findFirst()
                            .map(RuneWorkshopRecipes119.Recipe::key)
                            .orElse(this.entries.isEmpty() ? null : this.entries.get(0).key());
            this.detailScroll = 0;
            this.armedRecipe = null;

            for (int var3 = 0; var3 < this.entries.size(); var3++) {
                if (this.entries.get(var3).key().equals(this.selectedKey)) {
                    this.listOffset = var3 / this.visible() * this.visible();
                    break;
                }
            }
        }

        this.listOffset = Math.max(0, Math.min(this.listOffset, this.maxList()));
        if (this.restoreSelection) {
            for (int var4 = 0; var4 < this.entries.size(); var4++) {
                if (this.entries.get(var4).key().equals(this.selectedKey)) {
                    if (var4 < this.listOffset || var4 >= this.listOffset + this.visible()) {
                        this.listOffset =
                                Math.max(0, Math.min(this.maxList(), var4 - this.visible() / 2));
                    }
                    break;
                }
            }

            this.restoreSelection = false;
        }

        this.refreshStock();
    }

    private void refreshStock() {
        RuneWorkshopRecipes119.Recipe var1 = this.selected();
        LocalPlayer var2 = Minecraft.getInstance().player;
        List var3 = var1 == null ? List.of() : var1.rows();
        this.owned = new int[var3.size()];
        this.pouchOwned = new int[var3.size()];

        for (int var4 = 0; var4 < var3.size(); var4++) {
            this.owned[var4] = ((RuneWorkshopRecipes119.Row) var3.get(var4)).material().owned(var2);
            this.pouchOwned[var4] =
                    RunePouchStock1113.count(
                            ((RuneWorkshopRecipes119.Row) var3.get(var4)).material(), var2);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.cooldown > 0) {
            this.cooldown--;
        }

        this.refreshStock();
        if (this.armedRecipe != null && this.maximum(this.selected()) < 1) {
            this.armedRecipe = null;
        }
    }

    private int maximum(RuneWorkshopRecipes119.Recipe var1) {
        LocalPlayer var2 = Minecraft.getInstance().player;
        if (var1 == null || var2 == null || var1.mode() != RuneWorkshopRecipes119.Mode.DIRECT) {
            return 0;
        }

        if (var1.rune() != null) {
            if (!RuneStock.unlocked(var1.rune())) {
                return 0;
            }

            OptionalInt var6 = RuneStock.price(var1.rune());
            OptionalInt var7 = RuneStock.escPrice(var1.rune());
            return !var6.isEmpty() && !var7.isEmpty()
                    ? CraftQuantity.maxSynth(
                            RuneStock.powder().orElse(0),
                            var6.getAsInt(),
                            RuneStock.esc(var2),
                            var7.getAsInt())
                    : 0;
        } else {
            if (var1.word() == null) {
                return 0;
            }

            int var3 = Integer.MAX_VALUE;

            for (RuneWorkshopRecipes119.Row var5 : var1.rows()) {
                var3 = Math.min(var3, var5.material().owned(var2) / var5.needed());
            }

            return var3 == Integer.MAX_VALUE ? 0 : var3;
        }
    }

    private boolean busy() {
        return this.cooldown > 0
                || RuneWordCraft119.busy()
                || RuneCodexModel.get().pendingCraftRequests() > 0;
    }

    private void beginCraft() {
        this.armedRecipe = null;
        this.craft(this.selected(), 1);
    }

    private void craft(RuneWorkshopRecipes119.Recipe var1, int var2) {
        if (!FlashbackCompat.isInReplay()
                && var1 != null
                && var1.mode() == RuneWorkshopRecipes119.Mode.DIRECT
                && !this.busy()
                && var2 >= 1
                && var2 <= this.maximum(var1)) {
            int var3 =
                    var1.rune() != null
                            ? var1.rune().craftTrigger()
                            : (var1.word() != null ? var1.word().trigger() : -1);
            if (var3 > 0) {
                if (var1.word() != null) {
                    if (!RuneWordCraft119.start(var1, var2)) {
                        return;
                    }
                } else {
                    RuneCodexModel.get().craft(var3, var2, false, () -> this.maximum(var1) >= 1);
                }

                this.cooldown = 4;
                this.armedRecipe = null;
                ModToast.showInfo(Component.literal("合成请求已发送"));
            }
        }
    }

    private static boolean hit(double var0, double var2, int var4, int var5, int var6, int var7) {
        return var6 > 0
                && var7 > 0
                && var0 >= var4
                && var0 < var4 + var6
                && var2 >= var5
                && var2 < var5 + var7;
    }

    private int buttonWidth() {
        return Math.max(32, Math.min(110, (this.rightW - 8) / 2));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
        var1 = MenuScale207.event(this, var1);
        double var3 = var1.x();
        double var5 = var1.y();
        int var7 = var1.button();
        if (!hit(var3, var5, this.listX + 4, this.contentTop + 4, Math.max(10, this.listW - 12), 18)
                && this.searchBox != null) {
            this.searchBox.setFocused(false);
            this.setFocused(null);
        }

        if (var7 == 1
                && hit(var3, var5, this.listX + 4, this.contentTop + 4, this.listW - 12, 18)) {
            this.searchBox.setValue("");
            return true;
        }

        if (var7 == 0) {
            if (hit(var3, var5, this.x + this.w - 30, this.y + 7, 22, 20)) {
                this.onClose();
                return true;
            }

            int var8 = (this.w - 20) / this.navColumns;

            for (int var9 = 0; var9 < TABS.length; var9++) {
                if (hit(
                        var3,
                        var5,
                        this.x + 10 + var9 % this.navColumns * var8,
                        this.navY() + var9 / this.navColumns * 23,
                        var8 - 3,
                        20)) {
                    this.switchTab(
                            switch (var9) {
                                case 0 -> RuneCatalog.Tab.SYNTH_LOW;
                                case 1 -> RuneCatalog.Tab.WORDS;
                                case 2 -> RuneCatalog.Tab.ARROWS;
                                case 3 -> RuneCatalog.Tab.OTHER;
                                default -> RuneCatalog.Tab.BONUS;
                            });
                    return true;
                }
            }

            if (hit(
                    var3,
                    var5,
                    this.listX,
                    this.listTop,
                    this.listW,
                    this.listBottom - this.listTop)) {
                int var13 = this.listOffset + (int) ((var5 - this.listTop) / 29.0);
                if (var13 < this.entries.size()) {
                    this.selectedKey = this.entries.get(var13).key();
                    this.detailScroll = 0;
                    this.armedRecipe = null;
                    this.refreshStock();
                }

                return true;
            }

            RuneWorkshopRecipes119.Recipe var12 = this.selected();
            int var10 = this.buttonWidth();
            if (var12 != null && hit(var3, var5, this.rightX + 4, this.footer, var10, 21)) {
                if (var12.mode() == RuneWorkshopRecipes119.Mode.DIRECT) {
                    this.beginCraft();
                } else if (var12.trackable()) {
                    this.toggleTracking(var12);
                }

                return true;
            }

            if (var12 != null
                    && var12.mode() == RuneWorkshopRecipes119.Mode.DIRECT
                    && var12.trackable()
                    && hit(var3, var5, this.rightX + 8 + var10, this.footer, var10, 21)) {
                this.toggleTracking(var12);
                return true;
            }
        }

        return super.mouseClicked(var1, var2);
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        var1 = MenuScale207.coordinate(this, var1);
        var3 = MenuScale207.coordinate(this, var3);
        if (hit(var1, var3, this.listX, this.listTop, this.listW, this.listBottom - this.listTop)) {
            this.listOffset =
                    Math.max(
                            0, Math.min(this.maxList(), this.listOffset - (int) Math.signum(var7)));
            return true;
        } else if (hit(
                var1,
                var3,
                this.rightX,
                this.contentTop,
                this.rightW,
                Math.max(0, this.footer - this.contentTop))) {
            this.detailScroll =
                    Math.max(
                            0,
                            Math.min(
                                    Math.max(
                                            0,
                                            this.detailHeight
                                                    - (this.footer - this.contentTop - 8)),
                                    this.detailScroll - (int) (var7 * 18.0)));
            return true;
        } else {
            return super.mouseScrolled(var1, var3, var5, var7);
        }
    }

    private void toggleTracking(RuneWorkshopRecipes119.Recipe var1) {
        if (RecipeTracking119.isTracked(var1.key())) {
            RecipeTracking119.clear();
        } else {
            RecipeTracking119.track(var1);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent var1) {
        return RecipeTracking119.onKey(var1) ? true : super.keyPressed(var1);
    }

    private void text(GuiGraphics var1, String var2, int var3, int var4, int var5, int var6) {
        if (var6 > 0) {
            String var7 =
                    this.font.width(var2) > var6
                            ? this.font.plainSubstrByWidth(
                                            var2, Math.max(1, var6 - this.font.width("…")))
                                    + "…"
                            : var2;
            var1.drawString(this.font, var7, var3, var4, var5, false);
        }
    }

    private void border(GuiGraphics var1, int var2, int var3, int var4, int var5, int var6) {
        GuiBackdrop1132.fill(var1, var2, var3, var2 + var4, var3 + 1, var6, this);
        GuiBackdrop1132.fill(var1, var2, var3 + var5 - 1, var2 + var4, var3 + var5, var6, this);
        GuiBackdrop1132.fill(var1, var2, var3, var2 + 1, var3 + var5, var6, this);
        GuiBackdrop1132.fill(var1, var2 + var4 - 1, var3, var2 + var4, var3 + var5, var6, this);
    }

    private void button(
            GuiGraphics var1,
            int var2,
            int var3,
            int var4,
            int var5,
            String var6,
            boolean var7,
            boolean var8) {
        boolean var9 = hit(this.mx, this.my, var2, var3, var4, var5);
        GuiBackdrop1132.fill(
                var1,
                var2,
                var3,
                var2 + var4,
                var3 + var5,
                var8 ? -13029346 : (var9 && var7 ? -13949154 : -15001322),
                this);
        this.border(var1, var2, var3, var4, var5, var7 ? (var8 ? -7769529 : -12567501) : -13619931);
        String var10 = this.font.plainSubstrByWidth(var6, Math.max(1, var4 - 6));
        var1.drawString(
                this.font,
                var10,
                var2 + (var4 - this.font.width(var10)) / 2,
                var3 + (var5 - 9) / 2,
                var7 ? (var8 ? -2178433 : -1449262) : -5593704,
                false);
    }

    private void icon(GuiGraphics var1, ItemStack var2, int var3, int var4, int var5) {
        var1.pose().pushMatrix();
        var1.pose().translate(var3, var4);
        var1.pose().scale(var5 / 16.0F, var5 / 16.0F);
        var1.renderItem(RuneItemDetails.resolve(var2), 0, 0);
        var1.pose().popMatrix();
        this.tips.add(new RuneCodexScreen.Tip(var2, var3, var4, var5));
    }

    @Override
    public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
        var2 = MenuScale207.coordinate(this, var2);
        var3 = MenuScale207.coordinate(this, var3);
        MenuScale207.begin(this, var1);
        GuiBackdrop1132.fill(var1, 0, 0, this.width, this.height, -1610612736, this);
        GuiBackdrop1132.fill(
                var1, this.x, this.y, this.x + this.w, this.y + this.h, -99610609, this);
        this.border(var1, this.x, this.y, this.w, this.h, -12567501);
        GuiBackdrop1132.fill(
                var1,
                this.rightX - 5,
                this.contentTop,
                this.rightX - 4,
                this.y + this.h - 5,
                -12567501,
                this);
        MenuScale207.end(var1);
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        var2 = MenuScale207.coordinate(this, var2);
        var3 = MenuScale207.coordinate(this, var3);
        MenuScale207.begin(this, var1);
        this.mx = var2;
        this.my = var3;
        this.tips.clear();
        super.render(var1, var2, var3, var4);
        this.text(var1, "符文室", this.x + 12, this.y + 12, -2178433, this.w - 50);
        this.button(var1, this.x + this.w - 30, this.y + 7, 22, 20, "×", true, false);
        this.renderHeaderInfo(var1);
        int var5 = (this.w - 20) / this.navColumns;

        for (int var6 = 0; var6 < TABS.length; var6++) {
            this.button(
                    var1,
                    this.x + 10 + var6 % this.navColumns * var5,
                    this.navY() + var6 / this.navColumns * 23,
                    var5 - 3,
                    20,
                    TABS[var6],
                    true,
                    var6 == this.category);
        }

        this.renderList(var1);
        this.renderDetails(var1);
        RuneWorkshopRecipes119.Recipe var11 = this.hoveredLockedRune();
        if (var11 != null) {
            MenuScale207.tooltip(
                    var1,
                    this.font,
                    List.of(
                            Component.translatableWithFallback(
                                            "att2.runes.recipe.lock", "获得该符文材料或其上下阶材料后解锁")
                                    .withStyle(new ChatFormatting[] {ChatFormatting.RED})),
                    Optional.empty(),
                    this.mx,
                    this.my,
                    null,
                    this);
        } else if (this.hoveredMapRecipe() != null) {
            MenuScale207.tooltip(
                    var1,
                    this.font,
                    ShopTooltips.of(RuneItemDetails.resolve(this.hoveredMapRecipe().icon())),
                    Optional.empty(),
                    this.mx,
                    this.my,
                    null,
                    this);
        } else if (hit(this.mx, this.my, this.bonusX(), this.y + 7, 75, 20)) {
            MenuScale207.tooltip(
                    var1,
                    this.font,
                    this.bonusLines(),
                    Optional.empty(),
                    this.mx,
                    this.my,
                    null,
                    this);
        } else {
            for (RuneCodexScreen.Tip var8 : this.tips) {
                if (hit(this.mx, this.my, var8.x(), var8.y(), var8.size(), var8.size())
                        && var8.y() >= this.contentTop
                        && var8.y() + var8.size() <= this.listBottom) {
                    MenuScale207.tooltip(
                            var1,
                            this.font,
                            ShopTooltips.of(RuneItemDetails.resolve(var8.item())),
                            Optional.empty(),
                            this.mx,
                            this.my,
                            null,
                            this);
                    break;
                }
            }
        }

        MenuScale207.end(var1);
    }

    private RuneWorkshopRecipes119.Recipe hoveredMapRecipe() {
        if (!hit(
                this.mx,
                this.my,
                this.listX,
                this.listTop,
                Math.max(0, this.listW - 5),
                Math.max(0, this.listBottom - this.listTop))) {
            return null;
        } else {
            int var1 = this.listOffset + (this.my - this.listTop) / 29;
            if (var1 >= this.listOffset
                    && var1 < Math.min(this.entries.size(), this.listOffset + this.visible())) {
                RuneWorkshopRecipes119.Recipe var2 = this.entries.get(var1);
                return (var2.rune() != null || var2.word() != null)
                                && hit(
                                        this.mx,
                                        this.my,
                                        this.listX,
                                        this.listTop + (var1 - this.listOffset) * 29,
                                        this.listW - 5,
                                        28)
                        ? var2
                        : null;
            } else {
                return null;
            }
        }
    }

    private static int listUnlockState(RuneWorkshopRecipes119.Recipe var0) {
        if (var0.rune() == null) {
            return 1;
        }

        OptionalInt var1 = ScoreCache.getHolder("RUNE", var0.rune().unlockHolder());
        return var1.isEmpty() ? -1 : (var1.getAsInt() >= 1 ? 1 : 0);
    }

    private RuneWorkshopRecipes119.Recipe hoveredLockedRune() {
        if (!hit(
                this.mx,
                this.my,
                this.listX,
                this.listTop,
                Math.max(0, this.listW - 5),
                Math.max(0, this.listBottom - this.listTop))) {
            return null;
        } else {
            int var1 = this.listOffset + (this.my - this.listTop) / 29;
            if (var1 >= this.listOffset
                    && var1 < Math.min(this.entries.size(), this.listOffset + this.visible())) {
                int var2 = this.listTop + (var1 - this.listOffset) * 29;
                RuneWorkshopRecipes119.Recipe var3 = this.entries.get(var1);
                return hit(this.mx, this.my, this.listX, var2, this.listW - 5, 28)
                                && listUnlockState(var3) == 0
                        ? var3
                        : null;
            } else {
                return null;
            }
        }
    }

    private void renderList(GuiGraphics var1) {
        if (this.listBottom > this.listTop) {
            var1.enableScissor(this.listX, this.listTop, this.listX + this.listW, this.listBottom);

            for (int var2 = this.listOffset;
                    var2 < Math.min(this.entries.size(), this.listOffset + this.visible());
                    var2++) {
                RuneWorkshopRecipes119.Recipe var3 = this.entries.get(var2);
                int var4 = this.listTop + (var2 - this.listOffset) * 29;
                boolean var5 = var3.key().equals(this.selectedKey);
                if (var5 || hit(this.mx, this.my, this.listX, var4, this.listW - 5, 28)) {
                    GuiBackdrop1132.fill(
                            var1,
                            this.listX,
                            var4,
                            this.listX + this.listW - 5,
                            var4 + 28,
                            var5 ? -13620450 : -14606567,
                            this);
                }

                if (var5) {
                    GuiBackdrop1132.fill(
                            var1, this.listX, var4, this.listX + 2, var4 + 28, -5402281, this);
                }

                this.icon(var1, var3.icon(), this.listX + 5, var4 + 5, 18);
                this.text(
                        var1,
                        var3.name().getString(),
                        this.listX + 28,
                        var4 + 3,
                        -2178433,
                        this.listW - 35);
                int var6 = listUnlockState(var3);
                String var7 =
                        var6 == 0
                                ? "未解锁"
                                : (var6 < 0
                                        ? "解锁状态未同步"
                                        : (var3.mode() == RuneWorkshopRecipes119.Mode.DIRECT
                                                ? "可直接合成"
                                                : (var3.mode() == RuneWorkshopRecipes119.Mode.MANUAL
                                                        ? "手动放置"
                                                        : "加成信息")));
                if (RecipeTracking119.isTracked(var3.key())) {
                    var7 = var7 + " · 追踪中";
                }

                int var8 =
                        var6 == 0
                                ? -2781820
                                : (var6 < 0
                                        ? -5593704
                                        : (var3.mode() == RuneWorkshopRecipes119.Mode.DIRECT
                                                ? -5650281
                                                : -5593704));
                this.text(var1, var7, this.listX + 28, var4 + 16, var8, this.listW - 35);
            }

            var1.disableScissor();
            this.text(
                    var1,
                    (this.entries.isEmpty() ? 0 : this.listOffset + 1)
                            + "–"
                            + Math.min(this.entries.size(), this.listOffset + this.visible())
                            + " / "
                            + this.entries.size(),
                    this.listX + 5,
                    this.footer + 5,
                    -5593704,
                    this.listW - 10);
            if (this.maxList() > 0) {
                int var9 =
                        Math.max(
                                8,
                                (this.listBottom - this.listTop)
                                        * this.visible()
                                        / this.entries.size());
                int var10 =
                        this.listTop
                                + this.listOffset
                                        * (this.listBottom - this.listTop - var9)
                                        / this.maxList();
                GuiBackdrop1132.fill(
                        var1,
                        this.listX + this.listW - 3,
                        this.listTop,
                        this.listX + this.listW - 1,
                        this.listBottom,
                        -14146274,
                        this);
                GuiBackdrop1132.fill(
                        var1,
                        this.listX + this.listW - 3,
                        var10,
                        this.listX + this.listW - 1,
                        var10 + var9,
                        -7636912,
                        this);
            }
        }
    }

    private int wrapped(GuiGraphics var1, Component var2, int var3, int var4, int var5, int var6) {
        for (FormattedCharSequence var8 : this.font.split(var2, Math.max(1, var5))) {
            var1.drawString(this.font, var8, var3, var4, var6, false);
            var4 += 12;
        }

        return var4;
    }

    private int centeredHeader(GuiGraphics var1, Component var2, int var3, int var4, int var5) {
        for (FormattedCharSequence var7 : this.font.split(var2, Math.max(1, var5))) {
            int var8 = this.font.width(var7);
            var1.drawString(this.font, var7, var3 + (var5 - var8) / 2, var4, -5593704, false);
            var4 += 12;
        }

        return var4;
    }

    private void resource(
            GuiGraphics var1,
            ItemStack var2,
            String var3,
            String var4,
            int var5,
            int var6,
            int var7,
            boolean var8) {
        this.icon(var1, var2, var5, var6, 16);
        String var9 = var4;
        int var10 = this.font.width(var9);
        this.text(var1, var3, var5 + 21, var6 + 4, -1449262, Math.max(1, var7 - 26 - var10));
        var1.drawString(
                this.font, var9, var5 + var7 - var10, var6 + 4, var8 ? -5650281 : -1065342, false);
    }

    private int renderMaterials(
            GuiGraphics var1, RuneWorkshopRecipes119.Recipe var2, int var3, int var4, int var5) {
        List<RuneWorkshopRecipes119.Row> var6 = var2.rows();
        boolean var7 = var6.stream().anyMatch(var0 -> RunePouchStock1113.isRune(var0.material()));
        if (!var7) {
            this.text(var1, "编号", var3, var4, -5593704, 24);
            this.text(var1, "材料", var3 + 26, var4, -5593704, Math.max(1, var5 - 87));
            String var22 = "已有 / 需要";
            var1.drawString(
                    this.font, var22, var3 + var5 - this.font.width(var22), var4, -5593704, false);
            var4 += 13;

            for (int var23 = 0; var23 < var6.size(); var23++) {
                RuneWorkshopRecipes119.Row var24 = (RuneWorkshopRecipes119.Row) var6.get(var23);
                int var25 = var23 < this.owned.length ? this.owned[var23] : 0;
                String var26 =
                        var24.orders()
                                .toString()
                                .replace("[", "")
                                .replace("]", "")
                                .replace(" ", "");
                this.text(var1, var26, var3, var4 + 4, -5593704, 24);
                this.resource(
                        var1,
                        RecipeTracking119.materialIcon(
                                var24.material(), Minecraft.getInstance().player),
                        var24.material().name().getString(),
                        var25 + " / " + var24.needed(),
                        var3 + 27,
                        var4,
                        var5 - 27,
                        var25 >= var24.needed());
                var4 += 21;
            }

            return var4;
        } else {
            String var8 = "符文包裹里有";
            String var9 = "已有 / 需要";
            RuneMaterialLayout1113.Columns var10 =
                    RuneMaterialLayout1113.columns(
                            var5, this.font.width(var8), this.font.width(var9));
            int var11 =
                    this.wrapped(
                            var1,
                            Component.literal("编号"),
                            var3,
                            var4,
                            var10.ordinalWidth(),
                            -5593704);
            int var12 =
                    this.wrapped(
                            var1,
                            Component.literal("材料"),
                            var3 + var10.materialLeft(),
                            var4,
                            Math.max(1, var10.pouchLeft() - var10.materialLeft() - 4),
                            -5593704);
            int var13 =
                    this.centeredHeader(
                            var1,
                            Component.literal(var8),
                            var3 + var10.pouchLeft(),
                            var4,
                            var10.pouchWidth());
            int var14 =
                    this.wrapped(
                            var1,
                            Component.literal(var9),
                            var3 + var10.countLeft(),
                            var4,
                            var10.countWidth(),
                            -5593704);
            var4 = Math.max(Math.max(var11, var12), Math.max(var13, var14)) + 3;

            for (int var15 = 0; var15 < var6.size(); var15++) {
                RuneWorkshopRecipes119.Row var16 = (RuneWorkshopRecipes119.Row) var6.get(var15);
                int var17 = var15 < this.owned.length ? this.owned[var15] : 0;
                int var18 =
                        var15 < this.pouchOwned.length
                                ? this.pouchOwned[var15]
                                : RunePouchStock1113.count(
                                        var16.material(), Minecraft.getInstance().player);
                String var19 =
                        var16.orders()
                                .toString()
                                .replace("[", "")
                                .replace("]", "")
                                .replace(" ", "");
                this.text(var1, var19, var3, var4 + 4, -5593704, var10.ordinalWidth());
                this.icon(
                        var1,
                        RecipeTracking119.materialIcon(
                                var16.material(), Minecraft.getInstance().player),
                        var3 + var10.materialLeft(),
                        var4,
                        var10.iconSize());
                this.text(
                        var1,
                        var16.material().name().getString(),
                        var3 + var10.nameLeft(),
                        var4 + 4,
                        -1449262,
                        var10.nameWidth());
                if (var18 >= 0) {
                    RuneMaterialLayout1113.number(
                            var1,
                            this.font,
                            Integer.toString(var18),
                            var3 + var10.pouchLeft(),
                            var4 + 4,
                            var10.pouchWidth(),
                            var18 > 0 ? -1449262 : -5593704,
                            true,
                            false);
                }

                RuneMaterialLayout1113.number(
                        var1,
                        this.font,
                        var17 + " / " + var16.needed(),
                        var3 + var10.countLeft(),
                        var4 + 4,
                        var10.countWidth(),
                        var17 >= var16.needed() ? -5650281 : -1065342,
                        false,
                        false);
                var4 += 21;
            }

            return var4;
        }
    }

    private void renderDetails(GuiGraphics var1) {
        RuneWorkshopRecipes119.Recipe var2 = this.selected();
        if (var2 == null) {
            this.text(
                    var1,
                    "没有匹配的配方",
                    this.rightX + 5,
                    this.contentTop + 15,
                    -5593704,
                    this.rightW - 10);
        } else {
            int var3 = this.contentTop + 4;
            int var4 = Math.max(var3, this.footer - 5);
            int var5 = this.rightX + 5;
            int var6 = this.rightW - 14;
            if (var4 > var3) {
                var1.enableScissor(this.rightX, var3, this.rightX + this.rightW, var4);
                int var7 = var3 - this.detailScroll;
                int var8 = var7;
                int var9 = var5 + 31;
                int var10 = Math.max(12, var6 - 31);
                this.icon(var1, var2.icon(), var5, var8 + 2, 24);
                var7 = this.wrapped(var1, var2.name(), var9, var8 + 2, var10, -2178433) + 3;
                var7 =
                        this.wrapped(
                                        var1,
                                        Component.literal(
                                                var2.mode() == RuneWorkshopRecipes119.Mode.DIRECT
                                                        ? "可直接合成"
                                                        : (var2.mode()
                                                                        == RuneWorkshopRecipes119
                                                                                .Mode.MANUAL
                                                                ? "需手动放入地图容器"
                                                                : "当前符文加成")),
                                        var9,
                                        var7,
                                        var10,
                                        -5593704)
                                + 9;
                var7 = Math.max(var7, var8 + 33);
                if (!var2.sequence().isEmpty()) {
                    this.text(
                            var1,
                            var2.mode() == RuneWorkshopRecipes119.Mode.DIRECT ? "单次配方顺序" : "配方顺序",
                            var5,
                            var7,
                            -5593704,
                            var6);
                    var7 += 13;
                    int var11 = var5;

                    for (int var12 = 0; var12 < var2.sequence().size(); var12++) {
                        RuneWorkshopRecipes119.Material var13 = var2.sequence().get(var12);
                        int var14 =
                                Math.max(
                                        53,
                                        Math.min(
                                                102,
                                                37 + this.font.width(var13.name().getString())));
                        var14 = Math.min(var14, Math.max(1, var6));
                        if (var11 + var14 > var5 + var6 && var11 > var5) {
                            var11 = var5;
                            var7 += 25;
                        }

                        GuiBackdrop1132.fill(
                                var1, var11, var7, var11 + var14, var7 + 21, -14606567, this);
                        this.text(var1, var12 + 1 + "", var11 + 3, var7 + 6, -2178433, 12);
                        this.icon(
                                var1,
                                RecipeTracking119.materialIcon(
                                        var13, Minecraft.getInstance().player),
                                var11 + 14,
                                var7 + 3,
                                15);
                        this.text(
                                var1,
                                var13.name().getString()
                                        + (var13.quantity() > 1 ? " ×" + var13.quantity() : ""),
                                var11 + 32,
                                var7 + 6,
                                -1449262,
                                var14 - 35);
                        var11 += var14 + 5;
                    }

                    var7 += 30;
                    var7 = this.renderMaterials(var1, var2, var5, var7, var6);
                }

                if (var2.rune() != null) {
                    RuneCatalog.RuneDef var24 = var2.rune();
                    OptionalInt var27 = RuneStock.price(var24);
                    OptionalInt var30 = RuneStock.escPrice(var24);
                    OptionalInt var35 = RuneStock.powder();
                    String var15 = RuneCostSync1125.state(var24);
                    byte var16 = 1;
                    this.text(
                            var1,
                            var15.isEmpty() ? "合成消耗" : var15,
                            var5,
                            var7,
                            var15.isEmpty() ? -5593704 : -1065342,
                            var6);
                    var7 += 14;
                    this.resource(
                            var1,
                            RuneItems.powder(),
                            "符文粉末",
                            (var35.isPresent() ? var35.getAsInt() : "?")
                                    + " / "
                                    + (var27.isPresent() ? (long) var27.getAsInt() * var16 : "?"),
                            var5,
                            var7,
                            var6,
                            var35.isPresent()
                                    && var27.isPresent()
                                    && var35.getAsInt() >= (long) var27.getAsInt() * var16);
                    var7 += 21;
                    this.resource(
                            var1,
                            RuneItems.esc(),
                            "混沌石",
                            RuneStock.esc(Minecraft.getInstance().player)
                                    + " / "
                                    + (var30.isPresent() ? (long) var30.getAsInt() * var16 : "?"),
                            var5,
                            var7,
                            var6,
                            var30.isPresent()
                                    && RuneStock.esc(Minecraft.getInstance().player)
                                            >= (long) var30.getAsInt() * var16);
                    var7 += 23;
                }

                if (var2.bonus() != null) {
                    OptionalInt var25 = RuneStock.bonus(var2.bonus().holder());
                    this.text(
                            var1,
                            (var25.isPresent() ? var25.getAsInt() : "?") + var2.bonus().suffix(),
                            var5,
                            var7,
                            -1449262,
                            var6);
                    var7 += 20;
                }

                List<Component> var26 = RuneItemDetails.lore(var2.icon());
                if (!var26.isEmpty()) {
                    this.text(var1, "成品属性", var5, var7 + 5, -5593704, var6);
                    var7 += 20;

                    for (Component var31 : var26) {
                        var7 = this.wrapped(var1, var31, var5, var7, var6, -1449262) + 3;
                    }
                }

                if (var2.key().startsWith("i:xp:")) {
                    var7 =
                            this.wrapped(
                                    var1,
                                    Component.literal("此处追踪至少一件该品质物品；实际兑换经验和投入数量由地图决定。"),
                                    var5,
                                    var7 + 4,
                                    var6,
                                    -5593704);
                }

                if (var2.mode() == RuneWorkshopRecipes119.Mode.MANUAL) {
                    var7 =
                            this.wrapped(
                                    var1,
                                    Component.literal("手动放入地图指定容器。追踪只显示材料，不会自动搬动物品。"),
                                    var5,
                                    var7 + 5,
                                    var6,
                                    -5593704);
                }

                this.detailHeight = var7 - (var3 - this.detailScroll) + 8;
                this.detailScroll =
                        Math.max(
                                0,
                                Math.min(
                                        this.detailScroll,
                                        Math.max(0, this.detailHeight - (var4 - var3))));
                var1.disableScissor();
                int var29 = this.buttonWidth();
                if (var2.mode() == RuneWorkshopRecipes119.Mode.DIRECT) {
                    boolean var32 = this.maximum(var2) >= 1;
                    String var36 = var2.rune() != null ? RuneCostSync1125.state(var2.rune()) : "";
                    String var38 =
                            this.busy()
                                    ? RuneWordCraft119.statusLabel()
                                    : (!var36.isEmpty()
                                            ? var36
                                            : (!var32
                                                    ? "材料不足"
                                                    : (var2.word() != null
                                                            ? (var2.key().equals(this.armedRecipe)
                                                                    ? "制作符文"
                                                                    : "制作符文")
                                                            : "合成")));
                    this.button(
                            var1,
                            this.rightX + 4,
                            this.footer,
                            var29,
                            21,
                            var38,
                            var32 && !this.busy(),
                            var2.key().equals(this.armedRecipe));
                    if (var2.trackable()) {
                        this.button(
                                var1,
                                this.rightX + 8 + var29,
                                this.footer,
                                var29,
                                21,
                                RecipeTracking119.isTracked(var2.key()) ? "取消追踪" : "追踪配方",
                                true,
                                RecipeTracking119.isTracked(var2.key()));
                    }
                } else if (var2.trackable()) {
                    this.button(
                            var1,
                            this.rightX + 4,
                            this.footer,
                            var29,
                            21,
                            RecipeTracking119.isTracked(var2.key())
                                    ? "取消追踪"
                                    : (RecipeTracking119.tracked() != null ? "更换追踪配方" : "追踪配方"),
                            true,
                            RecipeTracking119.isTracked(var2.key()));
                }

                if (this.detailHeight > var4 - var3) {
                    int var33 = Math.max(8, (var4 - var3) * (var4 - var3) / this.detailHeight);
                    int var37 =
                            var3
                                    + this.detailScroll
                                            * (var4 - var3 - var33)
                                            / Math.max(1, this.detailHeight - (var4 - var3));
                    GuiBackdrop1132.fill(
                            var1,
                            this.rightX + this.rightW - 3,
                            var37,
                            this.rightX + this.rightW - 1,
                            var37 + var33,
                            -7636912,
                            this);
                }
            }
        }
    }

    private boolean stackedHeader() {
        return this.w < 440 && this.h >= 210;
    }

    private int navY() {
        return this.y + (this.stackedHeader() ? 46 : 34);
    }

    private int bonusX() {
        return this.x + this.w - 112;
    }

    private void renderHeaderInfo(GuiGraphics var1) {
        if (this.w >= 440 || this.stackedHeader()) {
            int var2 = this.stackedHeader() ? this.x + 12 : this.x + 128;
            int var3 = this.stackedHeader() ? this.y + 29 : this.y + 13;
            int var4 = this.stackedHeader() ? this.w - 24 : Math.max(1, this.bonusX() - var2 - 9);
            String var5 =
                    "符文粉末 "
                            + (RuneStock.powder().isPresent() ? RuneStock.powder().getAsInt() : "?")
                            + "  ·  混沌石 "
                            + RuneStock.esc(Minecraft.getInstance().player);
            this.text(var1, var5, var2, var3, -5593704, var4);
        }

        boolean var6 = hit(this.mx, this.my, this.bonusX(), this.y + 7, 75, 20);
        if (var6) {
            GuiBackdrop1132.fill(
                    var1,
                    this.bonusX(),
                    this.y + 7,
                    this.bonusX() + 75,
                    this.y + 27,
                    -14606567,
                    this);
        }

        this.text(var1, "符文加成", this.bonusX() + 5, this.y + 13, var6 ? -2178433 : -5593704, 65);
    }

    private List<Component> bonusLines() {
        ArrayList var1 = new ArrayList();
        var1.add(Component.literal("符文加成").withColor(14598783));

        for (RuneCatalog.BonusDef var3 : RuneCatalog.bonuses()) {
            OptionalInt var4 = RuneStock.bonus(var3.holder());
            String var5 =
                    var4.isPresent() ? Integer.toString(var4.getAsInt()) + var3.suffix() : "?";
            int var6 = var4.isEmpty() ? 11183512 : (var4.getAsInt() < 0 ? 16777045 : 11126935);
            var1.add(
                    ModLanguageManager.get(var3.langKey())
                            .copy()
                            .append("：")
                            .append(Component.literal(var5).withColor(var6)));
        }

        return List.copyOf(var1);
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

    private record Tip(ItemStack item, int x, int y, int size) {}

    private record ViewMemory(
            WeakReference<Object> world,
            RuneCatalog.Tab tab,
            String selectedKey,
            String search,
            int listOffset,
            int detailScroll) {}
}
