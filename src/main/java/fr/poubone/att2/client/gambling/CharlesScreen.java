package fr.poubone.att2.client.gambling;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.data.CurrencyModel;
import fr.poubone.att2.client.data.ScoreCache;
import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.quest.QuestJournalArt;
import fr.poubone.att2.client.screen.GuiBackdrop1132;
import fr.poubone.att2.client.shop.MenuScale207;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

public final class CharlesScreen extends Screen {
    private static final SoundEvent OPEN =
            SoundEvent.createVariableRangeEvent(
                    Identifier.fromNamespaceAndPath("att2", "ui.book.open"));
    private static CharlesScreen.Tab lastTab = CharlesScreen.Tab.BETS;
    private CharlesScreen.Tab tab;
    private int x;
    private int y;
    private int w;
    private int h;
    private int offset;
    private int selectedTrigger = -1;
    private int mx;
    private int my;
    private int rulesScroll;
    private int rulesHeight;
    private int boardLeft;
    private int boardTop;
    private int cellW;
    private int cellH;
    private int boardRows;
    private int boardCols;
    private boolean rulesOpen;
    private CharlesScreen.Pending confirmation;
    private long lastActionMs;
    private final ScratchClickGuard flipClicks = new ScratchClickGuard();
    private final ScratchAutoReveal autoReveal = new ScratchAutoReveal();

    private CharlesScreen(CharlesScreen.Tab var1) {
        super(ModLanguageManager.get("charles.title"));
        this.tab = var1 == null ? CharlesScreen.Tab.BETS : var1;
        lastTab = this.tab;
    }

    public static void open(CharlesScreen.Tab var0) {
        if (!FlashbackCompat.isInReplay()) {
            if (HUDConfig.get().charlesAutoOpen) {
                Minecraft var1 = Minecraft.getInstance();
                if (var1.screen instanceof CharlesScreen var2) {
                    boolean var10002 = false;
                } else {
                    var1.setScreen(new CharlesScreen(var0));
                    var1.getSoundManager().play(SimpleSoundInstance.forUI(OPEN, 1.0F));
                }
            }
        }
    }

    public static void toggle() {
        Minecraft var0 = Minecraft.getInstance();
        if (var0.screen instanceof CharlesScreen) {
            var0.setScreen(null);
        } else if (var0.screen == null) {
            open(lastTab);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        MenuScale207.prepare(this);
        this.w = Math.min(640, Math.max(1, this.width - 20));
        this.h = Math.min(362, Math.max(1, this.height - 20));
        this.x = (this.width - this.w) / 2;
        this.y = (this.height - this.h) / 2;
        CurrencyModel.request();
        lastTab = this.tab;
        if (this.needsData()) {
            this.requestCurrentTab();
        }
    }

    @Override
    public void removed() {
        MenuScale207.reset(this);
        this.autoReveal.reset();
        CurrencyModel.close();
        super.removed();
    }

    private GamblingModel model() {
        return GamblingModel.get();
    }

    private String tr(String var1) {
        return ModLanguageManager.getString("casino.modern." + var1);
    }

    private String fmt(String var1, Object... var2) {
        return ModLanguageManager.format("casino.modern." + var1, var2);
    }

    private static String plain(Component var0) {
        return var0 == null ? "" : ChatFormatting.stripFormatting(var0.getString());
    }

    private String tabLabel(CharlesScreen.Tab var1) {
        return ModLanguageManager.getString(
                var1 == CharlesScreen.Tab.BETS ? "charles.tab.bets" : "charles.tab.grids");
    }

    private boolean compactHeader() {
        return this.w < 520;
    }

    private int ruleY() {
        return this.y + (this.compactHeader() ? 68 : 62);
    }

    private int bodyTitleY() {
        return this.ruleY() + 10;
    }

    private int contentTop() {
        return this.ruleY() + 29;
    }

    private int footerY() {
        return this.y + this.h - 52;
    }

    private int pageY() {
        return this.footerY() - 22;
    }

    private int contentBottom() {
        return this.pageY() - 6;
    }

    private int columns() {
        return Math.min(4, Math.max(1, (this.w - 44) / 95));
    }

    private int cardW() {
        return (this.w - 44 - (this.columns() - 1) * 10) / this.columns();
    }

    private int cardX(int var1) {
        return this.x + 22 + var1 * (this.cardW() + 10);
    }

    private int cardH() {
        return Math.max(1, this.contentBottom() - this.contentTop());
    }

    private int maxOffset() {
        return Math.max(0, (this.options().size() - 1) / this.columns() * this.columns());
    }

    private int pageCount() {
        return Math.max(1, (this.options().size() + this.columns() - 1) / this.columns());
    }

    private int primaryW() {
        return Math.min(150, Math.max(96, (this.w - 44) / 3));
    }

    private int primaryX() {
        return this.x + (this.w - this.primaryW()) / 2;
    }

    private int utilityW() {
        return Math.min(64, Math.max(42, (this.w - 60) / 6));
    }

    private int rulesX() {
        return this.x + this.w - 22 - this.utilityW();
    }

    private int refreshX() {
        return this.rulesX() - this.utilityW() - 6;
    }

    private int utilityY() {
        return this.ruleY() + 5;
    }

    private int tabW() {
        return Math.min(78, Math.max(58, (this.w - 60) / 4));
    }

    private int tabsX() {
        return this.x + (this.w - 2 * this.tabW() - 6) / 2;
    }

    private int tabsY() {
        return this.y + (this.compactHeader() ? 41 : 15);
    }

    private boolean boardShowing() {
        return this.tab == CharlesScreen.Tab.GRIDS && this.model().hasGrid();
    }

    private boolean needsData() {
        return this.tab == CharlesScreen.Tab.BETS
                ? this.model().getBets().isEmpty()
                : !this.model().hasGrid() && this.model().getGridChoices().isEmpty();
    }

    private void requestCurrentTab() {
        if (this.tab == CharlesScreen.Tab.BETS) {
            this.model().requestBets();
        } else {
            this.model().requestGridMenu();
        }
    }

    private void switchTab(CharlesScreen.Tab var1, boolean var2) {
        if (var1 != null) {
            if (this.tab != var1) {
                this.autoReveal.reset();
                this.flipClicks.clear();
                this.offset = 0;
                this.selectedTrigger = -1;
                this.confirmation = null;
                this.rulesOpen = false;
                this.rulesScroll = 0;
            }

            this.tab = var1;
            lastTab = var1;
            if (var2) {
                this.model().clearNotEnoughChronotons();
                if (this.needsData()) {
                    this.requestCurrentTab();
                }
            }
        }
    }

    private List<CharlesScreen.Option> options() {
        ArrayList var1 = new ArrayList();
        if (this.tab == CharlesScreen.Tab.BETS) {
            for (GamblingModel.BetEntry var3 : this.model().getBets()) {
                var1.add(
                        new CharlesScreen.Option(
                                var3.trigger(),
                                var3.label(),
                                var3.price(),
                                var3.locked(),
                                false,
                                var3,
                                null));
            }
        } else {
            for (GamblingModel.GridChoice var5 : this.model().getGridChoices()) {
                var1.add(
                        new CharlesScreen.Option(
                                var5.trigger(),
                                var5.label(),
                                var5.price(),
                                var5.locked(),
                                var5.isContinue(),
                                null,
                                var5));
            }
        }

        return var1;
    }

    private CharlesScreen.Option selected() {
        if (this.selectedTrigger < 0) {
            return null;
        }

        for (CharlesScreen.Option var2 : this.options()) {
            if (var2.trigger() == this.selectedTrigger) {
                return var2;
            }
        }

        return null;
    }

    private boolean affordable(int var1) {
        return var1 < 0
                || ScoreCache.get("CHRONOTON").isEmpty()
                || ScoreCache.get("CHRONOTON").getAsInt() >= var1;
    }

    private boolean canUse(CharlesScreen.Option var1) {
        return var1 != null
                && !var1.locked()
                && var1.trigger() >= 0
                && !this.model().isWaiting()
                && !this.model().isBetPending()
                && (var1.resume() || var1.price() >= 0 && this.affordable(var1.price()));
    }

    private void choose(CharlesScreen.Option var1) {
        if (var1.trigger() >= 0) {
            this.selectedTrigger = var1.trigger();
        }
    }

    private void actionSelected() {
        CharlesScreen.Option var1 = this.selected();
        if (this.canUse(var1)) {
            if (var1.resume()) {
                if (this.debounce()) {
                    this.model().chooseGrid(var1.grid());
                }
            } else {
                this.confirmation =
                        new CharlesScreen.Pending(
                                this.tab == CharlesScreen.Tab.BETS
                                        ? CharlesScreen.Action.BET
                                        : CharlesScreen.Action.BUY_GRID,
                                var1);
            }
        }
    }

    private boolean debounce() {
        long var1 = System.currentTimeMillis();
        if (var1 - this.lastActionMs < 400L) {
            return false;
        }

        this.lastActionMs = var1;
        return true;
    }

    private void confirm() {
        CharlesScreen.Pending var1 = this.confirmation;
        if (var1 != null) {
            if (var1.action() == CharlesScreen.Action.RESET_GRID) {
                if (this.boardShowing()
                        && !this.model().isWaiting()
                        && !this.model().isBetPending()) {
                    if (this.debounce()) {
                        this.confirmation = null;
                        this.flipClicks.clear();
                        this.model().resetGrid();
                    }
                }
            } else {
                CharlesScreen.Option var2 = null;

                for (CharlesScreen.Option var4 : this.options()) {
                    if (var4.trigger() == var1.option().trigger()) {
                        var2 = var4;
                    }
                }

                if (var2 == null || !this.canUse(var2)) {
                    this.confirmation = null;
                } else if (var2.price() != var1.option().price()) {
                    this.confirmation = new CharlesScreen.Pending(var1.action(), var2);
                } else {
                    if (this.debounce()) {
                        this.confirmation = null;
                        if (var1.action() == CharlesScreen.Action.BET) {
                            this.model().placeBet(var2.bet());
                            this.onClose();
                        } else {
                            this.model().chooseGrid(var2.grid());
                        }
                    }
                }
            }
        }
    }

    private static boolean hit(double var0, double var2, int var4, int var5, int var6, int var7) {
        return var0 >= var4 && var0 < var4 + var6 && var2 >= var5 && var2 < var5 + var7;
    }

    private CharlesScreen.Option cardAt(double var1, double var3) {
        if (this.boardShowing()) {
            return null;
        }

        List var5 = this.options();

        for (int var6 = 0; var6 < this.columns() && this.offset + var6 < var5.size(); var6++) {
            if (hit(var1, var3, this.cardX(var6), this.contentTop(), this.cardW(), this.cardH())) {
                return (CharlesScreen.Option) var5.get(this.offset + var6);
            }
        }

        return null;
    }

    private void layoutBoard() {
        List<List<GamblingModel.GridCell>> var1 = this.model().getGrid();
        this.boardRows = Math.max(1, var1.size());
        this.boardCols = Math.max(1, var1.stream().mapToInt(List::size).max().orElse(1));
        int var2 = Math.max(1, this.w - 44);
        int var3 = Math.max(1, this.footerY() - 10 - this.contentTop());
        int var4 = this.gridGap();
        this.cellW =
                Math.max(1, Math.min(64, (var2 - (this.boardCols - 1) * var4) / this.boardCols));
        this.cellH =
                Math.max(1, Math.min(42, (var3 - (this.boardRows - 1) * var4) / this.boardRows));
        this.boardLeft =
                this.x + (this.w - this.boardCols * this.cellW - (this.boardCols - 1) * var4) / 2;
        this.boardTop =
                this.contentTop()
                        + Math.max(
                                0,
                                (var3 - this.boardRows * this.cellH - (this.boardRows - 1) * var4)
                                        / 2);
    }

    private int gridGap() {
        return this.h >= 260 && this.w >= 400 ? 5 : 3;
    }

    private GamblingModel.GridCell cellAt(double var1, double var3) {
        this.layoutBoard();
        int var5 = this.cellW + this.gridGap();
        int var6 = this.cellH + this.gridGap();
        int var7 = (int) Math.floor((var3 - this.boardTop) / var6);
        int var8 = (int) Math.floor((var1 - this.boardLeft) / var5);
        if (var7 >= 0 && var7 < this.model().getGrid().size() && var8 >= 0) {
            List var9 = this.model().getGrid().get(var7);
            return var8 < var9.size()
                            && hit(
                                    var1,
                                    var3,
                                    this.boardLeft + var8 * var5,
                                    this.boardTop + var7 * var6,
                                    this.cellW,
                                    this.cellH)
                    ? (GamblingModel.GridCell) var9.get(var8)
                    : null;
        } else {
            return null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.offset = Math.max(0, Math.min(this.offset, this.maxOffset()));
        this.tickAutoReveal();
    }

    private ScratchAutoReveal.View autoView() {
        OptionalInt var1 = ScoreCache.readLive("CHRONOTON");
        if (var1.isEmpty()) {
            var1 = ScoreCache.get("CHRONOTON");
        }

        return new ScratchAutoReveal.View(
                this.model().getGrid(),
                this.model().getNextFlipPrice(),
                this.model().isNextFlipAffordable(),
                var1,
                this.model().isNearTable(),
                this.model().isWaiting(),
                this.model().isNotEnoughChronotons(),
                ScratchPriceUpdates.revision());
    }

    private void tickAutoReveal() {
        if (FlashbackCompat.isInReplay()) {
            this.autoReveal.reset();
        } else if (!this.boardShowing()) {
            this.autoReveal.reset();
        } else {
            if (this.rulesOpen || this.confirmation != null) {
                this.autoReveal.stop(ScratchAutoReveal.Reason.STOPPED);
            }

            long var1 = System.currentTimeMillis();
            ScratchAutoReveal.View var3 = this.autoView();
            ScratchAutoReveal.Target var4 = this.autoReveal.next(var3, var1);
            if (var4 != null) {
                if (this.flipClicks.accept(var4.cell(), var3.board(), var1)) {
                    this.autoReveal.submitted(var4, var3, var1);
                    this.model().flip(var4.cell());
                } else {
                    this.autoReveal.stop(ScratchAutoReveal.Reason.BUSY);
                }
            }
        }
    }

    private boolean canAutoReveal() {
        return this.boardShowing()
                && !this.rulesOpen
                && this.confirmation == null
                && this.autoReveal.canStart(this.autoView());
    }

    private void toggleAutoReveal() {
        if (this.autoReveal.active()) {
            this.autoReveal.stop(ScratchAutoReveal.Reason.STOPPED);
        } else if (this.canAutoReveal()) {
            this.autoReveal.start(this.autoView(), System.currentTimeMillis());
        }
    }

    private String revealLabel() {
        int var1 = 0;
        int var2 = 0;

        for (List<GamblingModel.GridCell> var4 : this.model().getGrid()) {
            for (GamblingModel.GridCell var6 : var4) {
                var1++;
                if (!var6.isLocked()) {
                    var2++;
                }
            }
        }

        return this.w < 420
                ? var2 + " / " + var1
                : this.fmt("revealed", "done", var2, "total", var1);
    }

    private int autoButtonW() {
        return Math.max(60, Math.min(86, this.resetX() - this.x - 22 - 6 - 40 - 8));
    }

    private int autoButtonX() {
        return this.x
                + 22
                + Math.min(
                        this.font.width(this.revealLabel()),
                        Math.max(1, this.resetX() - this.x - 22 - 6 - this.autoButtonW() - 8))
                + 8;
    }

    private int boardButtonW() {
        return Math.min(96, Math.max(60, (this.w - 44) / 4));
    }

    private int backX() {
        return this.x + this.w - 22 - this.boardButtonW();
    }

    private int resetX() {
        return this.backX() - this.boardButtonW() - 6;
    }

    private int popupW() {
        return Math.min(300, this.w - 32);
    }

    private int popupX() {
        return this.x + (this.w - this.popupW()) / 2;
    }

    private int popupY() {
        return this.y + (this.h - 152) / 2;
    }

    private int rulesTop() {
        return this.y + 50;
    }

    private int rulesBottom() {
        return this.y + this.h - 39;
    }

    private int rulesMax() {
        return Math.max(0, this.rulesHeight - Math.max(1, this.rulesBottom() - this.rulesTop()));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
        var1 = MenuScale207.event(this, var1);
        double var3 = var1.x();
        double var5 = var1.y();
        int var7 = var1.button();
        if (this.confirmation != null) {
            if (var7 == 0) {
                int var16 = (this.popupW() - 36) / 2;
                if (hit(var3, var5, this.popupX() + 14, this.popupY() + 119, var16, 20)) {
                    this.confirm();
                } else if (hit(
                        var3, var5, this.popupX() + 22 + var16, this.popupY() + 119, var16, 20)) {
                    this.confirmation = null;
                }
            }

            return true;
        } else if (!this.rulesOpen) {
            if (var7 == 1) {
                CharlesScreen.Option var15 = this.cardAt(var3, var5);
                if (var15 != null && var15.trigger() == this.selectedTrigger) {
                    this.selectedTrigger = -1;
                    return true;
                } else {
                    return super.mouseClicked(var1, var2);
                }
            } else {
                if (var7 != 0) {
                    return super.mouseClicked(var1, var2);
                }

                if (hit(var3, var5, this.x + this.w - 39, this.y + 14, 21, 20)) {
                    this.onClose();
                    return true;
                }

                for (CharlesScreen.Tab var11 : CharlesScreen.Tab.values()) {
                    if (hit(
                            var3,
                            var5,
                            this.tabsX() + var11.ordinal() * (this.tabW() + 6),
                            this.tabsY(),
                            this.tabW(),
                            20)) {
                        this.switchTab(var11, true);
                        return true;
                    }
                }

                if (hit(var3, var5, this.rulesX(), this.utilityY(), this.utilityW(), 18)) {
                    this.autoReveal.stop(ScratchAutoReveal.Reason.STOPPED);
                    this.rulesOpen = true;
                    this.rulesScroll = 0;
                    return true;
                }

                if (!this.boardShowing()
                        && hit(var3, var5, this.refreshX(), this.utilityY(), this.utilityW(), 18)) {
                    if (!this.model().isWaiting() && !this.model().isBetPending()) {
                        this.model().clearNotEnoughChronotons();
                        this.requestCurrentTab();
                    }

                    return true;
                } else {
                    if (this.boardShowing()) {
                        if (hit(
                                var3,
                                var5,
                                this.autoButtonX(),
                                this.footerY(),
                                this.autoButtonW(),
                                22)) {
                            this.toggleAutoReveal();
                            return true;
                        }

                        GamblingModel.GridCell var13 = this.cellAt(var3, var5);
                        if (var13 != null) {
                            if (this.autoReveal.active()) {
                                return true;
                            }

                            if (var13.isLocked()
                                    && var13.trigger() >= 0
                                    && !this.model().isWaiting()
                                    && this.model().isNearTable()
                                    && this.model().getNextFlipPrice() >= 0
                                    && this.model().isNextFlipAffordable()
                                    && this.affordable(this.model().getNextFlipPrice())
                                    && this.flipClicks.accept(
                                            var13,
                                            this.model().getGrid(),
                                            System.currentTimeMillis())) {
                                this.model().flip(var13);
                            }

                            return true;
                        }

                        if (hit(
                                var3,
                                var5,
                                this.resetX(),
                                this.footerY(),
                                this.boardButtonW(),
                                22)) {
                            this.autoReveal.reset();
                            if (!this.model().isWaiting()) {
                                this.confirmation =
                                        new CharlesScreen.Pending(
                                                CharlesScreen.Action.RESET_GRID, null);
                            }

                            return true;
                        }

                        if (hit(
                                var3,
                                var5,
                                this.backX(),
                                this.footerY(),
                                this.boardButtonW(),
                                22)) {
                            this.autoReveal.reset();
                            if (!this.model().isWaiting()) {
                                this.model().requestGridMenu();
                            }

                            return true;
                        }
                    } else {
                        CharlesScreen.Option var14 = this.cardAt(var3, var5);
                        if (var14 != null) {
                            this.choose(var14);
                            return true;
                        }

                        if (hit(var3, var5, this.x + this.w / 2 - 68, this.pageY(), 40, 18)) {
                            this.offset = Math.max(0, this.offset - this.columns());
                            return true;
                        }

                        if (hit(var3, var5, this.x + this.w / 2 + 28, this.pageY(), 40, 18)) {
                            this.offset = Math.min(this.maxOffset(), this.offset + this.columns());
                            return true;
                        }

                        if (hit(var3, var5, this.primaryX(), this.footerY(), this.primaryW(), 23)) {
                            this.actionSelected();
                            return true;
                        }
                    }

                    return super.mouseClicked(var1, var2);
                }
            }
        } else {
            if (var7 == 0
                    && (hit(var3, var5, this.x + this.w - 41, this.y + 24, 20, 20)
                            || hit(
                                    var3,
                                    var5,
                                    this.x + (this.w - 90) / 2,
                                    this.y + this.h - 32,
                                    90,
                                    20))) {
                this.rulesOpen = false;
            }

            return true;
        }
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        var1 = MenuScale207.coordinate(this, var1);
        var3 = MenuScale207.coordinate(this, var3);
        if (var7 == 0.0) {
            return super.mouseScrolled(var1, var3, var5, var7);
        } else if (this.confirmation != null) {
            return true;
        } else if (this.rulesOpen) {
            this.rulesScroll =
                    Math.max(
                            0,
                            Math.min(this.rulesMax(), this.rulesScroll + (var7 > 0.0 ? -13 : 13)));
            return true;
        } else if (!this.boardShowing()
                && hit(
                        var1,
                        var3,
                        this.x + 20,
                        this.contentTop(),
                        this.w - 40,
                        this.contentBottom() - this.contentTop())) {
            this.offset =
                    Math.max(
                            0,
                            Math.min(
                                    this.maxOffset(),
                                    this.offset + (var7 > 0.0 ? -this.columns() : this.columns())));
            return true;
        } else {
            return super.mouseScrolled(var1, var3, var5, var7);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent var1) {
        if (var1.key() == 256) {
            if (this.confirmation != null) {
                this.confirmation = null;
            } else if (this.rulesOpen) {
                this.rulesOpen = false;
            } else {
                this.onClose();
            }

            return true;
        } else {
            if (this.confirmation != null || this.rulesOpen) {
                return true;
            }

            if (!this.boardShowing()
                    && (var1.key() == 262
                            || var1.key() == 263
                            || var1.key() == 264
                            || var1.key() == 265)) {
                List var2 = this.options();
                if (var2.isEmpty()) {
                    return true;
                }

                int var3 = -1;

                for (int var4 = 0; var4 < var2.size(); var4++) {
                    if (((CharlesScreen.Option) var2.get(var4)).trigger() == this.selectedTrigger) {
                        var3 = var4;
                    }
                }

                int var6 = var1.key() != 262 && var1.key() != 264 ? -1 : 1;
                var3 = Math.max(0, Math.min(var2.size() - 1, var3 + var6));
                this.choose((CharlesScreen.Option) var2.get(var3));
                this.offset = var3 / this.columns() * this.columns();
                return true;
            } else if (!this.boardShowing() && var1.key() == 267) {
                this.offset = Math.min(this.maxOffset(), this.offset + this.columns());
                return true;
            } else if (!this.boardShowing() && var1.key() == 266) {
                this.offset = Math.max(0, this.offset - this.columns());
                return true;
            } else {
                return super.keyPressed(var1);
            }
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

    private void centered(GuiGraphics var1, String var2, int var3, int var4, int var5, int var6) {
        String var7 = var2 == null ? "" : var2;
        if (this.font.width(var7) > var5) {
            var7 =
                    this.font.plainSubstrByWidth(var7, Math.max(1, var5 - this.font.width("…")))
                            + "…";
        }

        var1.drawString(this.font, var7, var3 - this.font.width(var7) / 2, var4, var6, false);
    }

    private void button(
            GuiGraphics var1, int var2, int var3, int var4, int var5, String var6, boolean var7) {
        CasinoPackArt.button(
                var1,
                this.font,
                var2,
                var3,
                var4,
                var5,
                var6,
                hit(this.mx, this.my, var2, var3, var4, var5),
                var7);
    }

    private void nav(GuiGraphics var1, int var2, int var3, boolean var4, boolean var5) {
        this.button(var1, var2, var3, 40, 18, "", var5);
        int var6 = var2 + 20;
        int var7 = var5 ? -1255768 : -6123148;

        for (int var8 = 0; var8 < 8; var8++) {
            int var9 = 3 - Math.min(var8, 7 - var8);
            int var10 = var4 ? var6 + 1 - var9 : var6 - 2 + var9;
            GuiBackdrop1132.fill(
                    var1, var10, var3 + 5 + var8, var10 + 2, var3 + 6 + var8, var7, this);
        }
    }

    private String price(int var1) {
        return var1 < 0
                ? this.tr("unknownPrice")
                : ModLanguageManager.format("charles.price", "price", var1);
    }

    private Component optionName(CharlesScreen.Option var1) {
        return CasinoOptionText.name(var1.label());
    }

    private void centeredOption(GuiGraphics var1, Component var2, int var3, int var4, int var5) {
        Component var6 = CasinoOptionText.fit(this.font, var2, var5);
        var1.drawString(this.font, var6, var3 - this.font.width(var6) / 2, var4, -9022672, false);
    }

    private void cellIcon(
            GuiGraphics var1, GamblingModel.CellType var2, int var3, int var4, int var5) {
        var1.blit(
                RenderPipelines.GUI_TEXTURED,
                var2.texture,
                var3,
                var4,
                0.0F,
                0.0F,
                var5,
                var5,
                var2.textureSize,
                var2.textureSize,
                var2.textureSize,
                var2.textureSize);
    }

    @Override
    public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
        var2 = MenuScale207.coordinate(this, var2);
        var3 = MenuScale207.coordinate(this, var3);
        MenuScale207.begin(this, var1);
        GuiBackdrop1132.blur(var1, this);
        GuiBackdrop1132.gradient(var1, 0, 0, this.width, this.height, -821030116, -317386734, this);
        GuiBackdrop1132.fill(
                var1,
                this.x + 3,
                this.y + 4,
                this.x + this.w + 3,
                this.y + this.h + 4,
                -2013265920,
                this);
        CasinoPackArt.page(var1, this.x, this.y, this.w, this.h);
        MenuScale207.end(var1);
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        var2 = MenuScale207.coordinate(this, var2);
        var3 = MenuScale207.coordinate(this, var3);
        MenuScale207.begin(this, var1);
        this.mx = var2;
        this.my = var3;
        super.render(var1, var2, var3, var4);
        this.renderHeader(var1);
        if (!this.boardShowing()) {
            this.flipClicks.clear();
        }

        if (this.boardShowing()) {
            this.renderBoard(var1);
        } else {
            this.renderOptions(var1);
        }

        this.renderFooter(var1);
        if (this.confirmation != null) {
            this.renderConfirmation(var1);
        } else if (this.rulesOpen) {
            this.renderRules(var1);
        } else {
            this.renderTooltip(var1, var2, var3);
        }

        MenuScale207.end(var1);
    }

    private void renderHeader(GuiGraphics var1) {
        this.text(
                var1,
                ModLanguageManager.getString("charles.title"),
                this.x + 18,
                this.y + 15,
                -13229295,
                this.w < 520 ? this.w - 189 : Math.max(50, this.tabsX() - this.x - 35));
        if (!this.compactHeader()) {
            this.text(
                    var1,
                    this.tr("casino") + " · " + this.tabLabel(this.tab),
                    this.x + 18,
                    this.y + 35,
                    -9022672,
                    Math.max(50, this.tabsX() - this.x - 35));
        }

        OptionalInt var2 = ScoreCache.get("CHRONOTON");
        int var3 = this.x + this.w - 159;
        CasinoPackArt.coin(var1, var3, this.y + 11, 20);
        this.text(var1, "Chronotons", var3 + 23, this.y + 12, -9022672, 92);
        this.text(
                var1,
                var2.isPresent() ? String.valueOf(var2.getAsInt()) : "?",
                var3 + 23,
                this.y + 27,
                -13229295,
                92);
        this.button(var1, this.x + this.w - 39, this.y + 14, 21, 20, "×", true);

        for (CharlesScreen.Tab var7 : CharlesScreen.Tab.values()) {
            int var8 = this.tabsX() + var7.ordinal() * (this.tabW() + 6);
            CasinoPackArt.button(
                    var1,
                    this.font,
                    var8,
                    this.tabsY(),
                    this.tabW(),
                    20,
                    this.tabLabel(var7),
                    var7 == this.tab || hit(this.mx, this.my, var8, this.tabsY(), this.tabW(), 20),
                    true);
        }

        QuestJournalArt.ornamentRule(
                var1, this.x + 21, this.x + this.w - 21, this.ruleY(), -1719508163);
        int var9 =
                this.boardShowing()
                        ? Math.max(20, this.w / 2 - 48)
                        : Math.max(36, this.refreshX() - this.x - 29);
        this.text(
                var1,
                this.boardShowing()
                        ? this.tr("board")
                        : (this.tab == CharlesScreen.Tab.BETS
                                ? this.tr("wagers")
                                : this.tr("boards")),
                this.x + 22,
                this.bodyTitleY(),
                -13229295,
                var9);
        this.button(
                var1, this.rulesX(), this.utilityY(), this.utilityW(), 18, this.tr("rules"), true);
        if (!this.boardShowing()) {
            this.button(
                    var1,
                    this.refreshX(),
                    this.utilityY(),
                    this.utilityW(),
                    18,
                    this.tr("refresh"),
                    !this.model().isWaiting() && !this.model().isBetPending());
        } else {
            String var10 = this.price(this.model().getNextFlipPrice());
            this.text(
                    var1,
                    var10,
                    this.x + this.w / 2 - 20,
                    this.bodyTitleY(),
                    this.model().isNextFlipAffordable() ? -9022672 : -7921890,
                    Math.max(20, this.rulesX() - this.x - this.w / 2 + 10));
        }
    }

    private void renderOptions(GuiGraphics var1) {
        List var2 = this.options();
        this.offset = Math.max(0, Math.min(this.offset, this.maxOffset()));

        for (int var3 = 0; var3 < this.columns() && this.offset + var3 < var2.size(); var3++) {
            CharlesScreen.Option var4 = (CharlesScreen.Option) var2.get(this.offset + var3);
            int var5 = this.cardX(var3);
            int var6 = this.contentTop();
            int var7 = this.cardW();
            int var8 = this.cardH();
            boolean var9 = var4.trigger() >= 0 && var4.trigger() == this.selectedTrigger;
            CasinoPackArt.sheet(var1, var5, var6, var7, var8);
            if (var9) {
                GuiBackdrop1132.fill(
                        var1, var5 + 6, var6 + 8, var5 + 8, var6 + var8 - 7, -7921890, this);
            }

            if (hit(this.mx, this.my, var5, var6, var7, var8) && !var9) {
                GuiBackdrop1132.fill(
                        var1,
                        var5 + 8,
                        var6 + 8,
                        var5 + var7 - 8,
                        var6 + var8 - 7,
                        343298864,
                        this);
            }

            var1.enableScissor(var5 + 8, var6 + 4, var5 + var7 - 8, var6 + var8 - 4);
            this.centeredOption(var1, this.optionName(var4), var5 + var7 / 2, var6 + 10, var7 - 22);
            String var10 =
                    var4.locked()
                            ? this.tr("locked")
                            : (var4.resume()
                                    ? this.tr("resume")
                                    : (var4.price() < 0 ? "?" : String.valueOf(var4.price())));
            int var11 =
                    var4.locked()
                            ? -9022672
                            : (this.affordable(var4.price()) ? -13229295 : -7921890);
            if (var8 >= 100) {
                CasinoPackArt.coin(var1, var5 + (var7 - 26) / 2, var6 + 29, 26);
                int var12 = this.font.width(var10);
                float var13 = Math.min(1.5F, Math.max(0.8F, (var7 - 24.0F) / Math.max(1, var12)));
                var1.pose().pushMatrix();
                var1.pose().translate(var5 + var7 / 2.0F, var6 + 65);
                var1.pose().scale(var13, var13);
                var1.drawString(this.font, var10, -var12 / 2, 0, var11, false);
                var1.pose().popMatrix();
                this.centered(
                        var1,
                        var4.resume() ? this.tr("resumeHintShort") : "Chronotons",
                        var5 + var7 / 2,
                        var6 + 84,
                        var7 - 20,
                        -9022672);
                this.centered(
                        var1,
                        var9 ? this.tr("selected") : this.tr("select"),
                        var5 + var7 / 2,
                        var6 + var8 - 17,
                        var7 - 20,
                        var9 ? -7921890 : -9022672);
            } else if (var8 >= 67) {
                CasinoPackArt.coin(var1, var5 + (var7 - 18) / 2, var6 + 25, 18);
                this.centered(var1, var10, var5 + var7 / 2, var6 + 47, var7 - 22, var11);
                if (var8 >= 82) {
                    this.centered(
                            var1,
                            var9 ? this.tr("selected") : "Chronotons",
                            var5 + var7 / 2,
                            var6 + var8 - 17,
                            var7 - 20,
                            var9 ? -7921890 : -9022672);
                }
            } else {
                CasinoPackArt.coin(var1, var5 + 10, var6 + 25, 13);
                this.centered(var1, var10, var5 + var7 / 2 + 6, var6 + 27, var7 - 36, var11);
            }

            var1.disableScissor();
        }

        if (var2.isEmpty()) {
            String var14 =
                    this.model().isWaiting()
                            ? this.tr("loading")
                            : ModLanguageManager.getString(
                                    this.tab == CharlesScreen.Tab.BETS
                                            ? "charles.bets.empty"
                                            : "charles.grids.empty");
            int var15 = this.contentTop() + 12;
            var1.enableScissor(
                    this.x + 20, this.contentTop(), this.x + this.w - 20, this.contentBottom());

            for (FormattedCharSequence var17 :
                    this.font.split(Component.literal(var14), this.w - 52)) {
                var1.drawString(this.font, var17, this.x + 26, var15, -9022672, false);
                var15 += 12;
            }

            var1.disableScissor();
        }

        this.nav(var1, this.x + this.w / 2 - 68, this.pageY(), false, this.offset > 0);
        this.nav(
                var1, this.x + this.w / 2 + 28, this.pageY(), true, this.offset < this.maxOffset());
        this.centered(
                var1,
                this.offset / this.columns() + 1 + " / " + this.pageCount(),
                this.x + this.w / 2,
                this.pageY() + 5,
                52,
                -9022672);
        QuestJournalArt.ornamentRule(
                var1, this.x + 22, this.x + this.w - 22, this.footerY() - 6, -1719508163);
    }

    private void renderBoard(GuiGraphics var1) {
        this.layoutBoard();
        List var2 = this.model().getGrid();

        for (int var3 = 0; var3 < var2.size(); var3++) {
            for (int var4 = 0; var4 < ((List) var2.get(var3)).size(); var4++) {
                GamblingModel.GridCell var5 =
                        (GamblingModel.GridCell) ((List) var2.get(var3)).get(var4);
                int var6 = this.boardLeft + var4 * (this.cellW + this.gridGap());
                int var7 = this.boardTop + var3 * (this.cellH + this.gridGap());
                if (var5.isLocked()) {
                    CasinoPackArt.chrome(
                            var1,
                            var6,
                            var7,
                            this.cellW,
                            this.cellH,
                            hit(this.mx, this.my, var6, var7, this.cellW, this.cellH),
                            true);
                } else {
                    CasinoPackArt.sheet(var1, var6, var7, this.cellW, this.cellH);
                }

                int var8 = Math.max(1, Math.min(28, Math.min(this.cellW - 8, this.cellH - 6)));
                this.cellIcon(
                        var1,
                        var5.type(),
                        var6 + (this.cellW - var8) / 2,
                        var7 + (this.cellH - var8) / 2,
                        var8);
            }
        }
    }

    private void renderFooter(GuiGraphics var1) {
        if (this.boardShowing()) {
            this.text(
                    var1,
                    this.revealLabel(),
                    this.x + 22,
                    this.footerY() + 6,
                    -13229295,
                    Math.max(1, this.autoButtonX() - this.x - 30));
            this.button(
                    var1,
                    this.autoButtonX(),
                    this.footerY(),
                    this.autoButtonW(),
                    22,
                    this.tr(this.autoReveal.active() ? "stopAuto" : "autoReveal"),
                    this.autoReveal.active() || this.canAutoReveal());
            this.button(
                    var1,
                    this.resetX(),
                    this.footerY(),
                    this.boardButtonW(),
                    22,
                    this.tr("reset"),
                    !this.model().isWaiting());
            this.button(
                    var1,
                    this.backX(),
                    this.footerY(),
                    this.boardButtonW(),
                    22,
                    this.tr("back"),
                    !this.model().isWaiting());
            List var2 = this.model().getCounts();
            if (!var2.isEmpty()) {
                int var3 = Math.min(45, Math.max(1, (this.w - 44) / var2.size()));

                for (int var4 = 0; var4 < var2.size(); var4++) {
                    GamblingModel.CountEntry var5 = (GamblingModel.CountEntry) var2.get(var4);
                    int var6 = this.x + 22 + var4 * var3;
                    if (var6 + var3 > this.x + this.w - 22) {
                        break;
                    }

                    this.cellIcon(var1, var5.type(), var6, this.footerY() + 28, 12);
                    this.text(
                            var1,
                            "×" + var5.count(),
                            var6 + 14,
                            this.footerY() + 30,
                            var5.color() == null ? -9022672 : 0xFF000000 | var5.color().getValue(),
                            Math.max(1, var3 - 15));
                }
            } else {
                this.renderStatus(var1, this.footerY() + 30);
            }
        } else {
            CharlesScreen.Option var7 = this.selected();
            this.text(
                    var1,
                    this.tr("selectedWager"),
                    this.x + 22,
                    this.footerY() + 2,
                    -9022672,
                    Math.max(20, this.primaryX() - this.x - 30));
            this.text(
                    var1,
                    var7 == null
                            ? "—"
                            : (var7.resume()
                                    ? this.tr("resume")
                                    : (var7.price() < 0 ? "?" : var7.price() + " Chronotons")),
                    this.x + 22,
                    this.footerY() + 15,
                    -13229295,
                    Math.max(20, this.primaryX() - this.x - 30));
            this.button(
                    var1,
                    this.primaryX(),
                    this.footerY(),
                    this.primaryW(),
                    23,
                    this.model().isBetPending()
                            ? this.tr("pending")
                            : (var7 != null && var7.resume()
                                    ? this.tr("resume")
                                    : (this.tab == CharlesScreen.Tab.BETS
                                            ? this.tr("placeBet")
                                            : this.tr("buyGrid"))),
                    this.canUse(var7));
            int var8 = this.primaryX() + this.primaryW() + 12;
            this.text(
                    var1,
                    this.tr("selectionSafeShort"),
                    var8,
                    this.footerY() + 5,
                    -9022672,
                    Math.max(1, this.x + this.w - 22 - var8));
            this.renderStatus(var1, this.footerY() + 33);
        }
    }

    private void renderStatus(GuiGraphics var1, int var2) {
        int var4 = -9022672;
        String var3;
        if (this.model().isNotEnoughChronotons()) {
            var3 = ModLanguageManager.getString("charles.not_enough");
            var4 = -7921890;
        } else if (this.boardShowing() && !this.model().isNearTable()) {
            var3 =
                    ModLanguageManager.format(
                            "charles.too_far", "d", (int) this.model().distanceToTable());
            var4 = -7921890;
        } else if (this.model().isWaiting()) {
            var3 = this.tr("loading");
        } else if (this.tab == CharlesScreen.Tab.BETS && this.model().isBetPending()) {
            var3 = this.tr("pending");
        } else if (this.boardShowing() && this.model().isGridComplete()) {
            var3 = ModLanguageManager.getString("charles.grid.complete");
        } else if (this.tab == CharlesScreen.Tab.BETS
                && this.model().getLastBetScore() >= 0
                && System.currentTimeMillis() - this.model().getLastBetScoreTime() < 120000L) {
            var3 =
                    Component.translatable("att2.gambling.score", this.model().getLastBetScore())
                            .getString();
        } else {
            var3 =
                    ModLanguageManager.getString(
                            this.boardShowing() ? "charles.grids.help" : "charles.bets.help");
        }

        this.text(var1, plain(Component.literal(var3)), this.x + 22, var2, var4, this.w - 44);
    }

    private List<Component> rules() {
        ArrayList var1 = new ArrayList();
        if (this.model().isNotEnoughChronotons()) {
            var1.add(ModLanguageManager.get("charles.not_enough").withStyle(ChatFormatting.RED));
        }

        if (this.model().isWaiting()) {
            var1.add(ModLanguageManager.get("casino.modern.loading"));
        }

        var1.add(
                ModLanguageManager.get(
                        this.tab == CharlesScreen.Tab.BETS
                                ? "charles.tab.bets.description"
                                : "charles.tab.grids.description"));
        var1.add(ModLanguageManager.get("casino.modern.selectionSafe"));
        var1.add(ModLanguageManager.get("casino.modern.mapDriven"));
        if (this.tab == CharlesScreen.Tab.BETS) {
            if (this.model().isBetPending()) {
                var1.add(ModLanguageManager.get("charles.bet.pending"));
            }

            if (this.model().getLastBetScore() >= 0
                    && System.currentTimeMillis() - this.model().getLastBetScoreTime() < 120000L) {
                var1.add(
                        Component.translatable(
                                "att2.gambling.score", this.model().getLastBetScore()));
            }

            var1.add(ModLanguageManager.get("charles.bets.help"));
        } else {
            var1.add(ModLanguageManager.get("charles.grids.help"));
            if (this.boardShowing()) {
                var1.add(Component.literal(this.price(this.model().getNextFlipPrice())));
                if (!this.model().isNearTable()) {
                    var1.add(
                            Component.literal(
                                            ModLanguageManager.format(
                                                    "charles.too_far",
                                                    "d",
                                                    (int) this.model().distanceToTable()))
                                    .withStyle(ChatFormatting.RED));
                }

                if (this.model().isGridComplete()) {
                    var1.add(ModLanguageManager.get("charles.grid.complete"));
                }

                for (GamblingModel.CountEntry var3 : this.model().getCounts()) {
                    var1.add(
                            Component.literal(
                                    this.tr("symbol." + var3.type().id) + " × " + var3.count()));
                }
            }
        }

        return var1;
    }

    private void renderRules(GuiGraphics var1) {
        GuiBackdrop1132.fill(
                var1, this.x, this.y, this.x + this.w, this.y + this.h, -1342177280, this);
        CasinoPackArt.page(var1, this.x + 12, this.y + 12, this.w - 24, this.h - 24);
        this.text(var1, this.tr("rules"), this.x + 27, this.y + 27, -13229295, this.w - 85);
        this.button(var1, this.x + this.w - 41, this.y + 24, 20, 20, "×", true);
        int var2 = this.w - 64;
        int var3 = 10;
        List<Component> var4 = this.rules();

        for (Component var6 : var4) {
            var3 += this.font.split(var6, var2).size() * 12 + 8;
        }

        this.rulesHeight = var3;
        this.rulesScroll = Math.max(0, Math.min(this.rulesScroll, this.rulesMax()));
        var1.enableScissor(this.x + 26, this.rulesTop(), this.x + this.w - 26, this.rulesBottom());
        int var10 = this.rulesTop() + 10 - this.rulesScroll;

        for (Component var7 : var4) {
            for (FormattedCharSequence var9 : this.font.split(var7, var2)) {
                var1.drawString(this.font, var9, this.x + 31, var10, -9022672, false);
                var10 += 12;
            }

            var10 += 8;
        }

        var1.disableScissor();
        if (this.rulesMax() > 0) {
            int var12 = this.rulesBottom() - this.rulesTop();
            int var13 = Math.max(8, var12 * var12 / this.rulesHeight);
            int var14 = this.rulesTop() + this.rulesScroll * (var12 - var13) / this.rulesMax();
            GuiBackdrop1132.fill(
                    var1,
                    this.x + this.w - 29,
                    this.rulesTop(),
                    this.x + this.w - 27,
                    this.rulesBottom(),
                    679633725,
                    this);
            GuiBackdrop1132.fill(
                    var1,
                    this.x + this.w - 29,
                    var14,
                    this.x + this.w - 27,
                    var14 + var13,
                    -1434295491,
                    this);
        }

        this.button(
                var1,
                this.x + (this.w - 90) / 2,
                this.y + this.h - 32,
                90,
                20,
                this.tr("closeRules"),
                true);
    }

    private void renderConfirmation(GuiGraphics var1) {
        GuiBackdrop1132.fill(
                var1, this.x, this.y, this.x + this.w, this.y + this.h, -1342177280, this);
        int var2 = this.popupX();
        int var3 = this.popupY();
        int var4 = this.popupW();
        CasinoPackArt.page(var1, var2, var3, var4, 152);
        this.text(
                var1,
                this.tr(
                        this.confirmation.action() == CharlesScreen.Action.RESET_GRID
                                ? "confirmReset"
                                : "confirmFee"),
                var2 + 16,
                var3 + 17,
                -13229295,
                var4 - 32);
        MutableComponent var5 =
                this.confirmation.action() == CharlesScreen.Action.RESET_GRID
                        ? Component.literal(this.tr("resetWarning"))
                        : Component.empty()
                                .append(this.optionName(this.confirmation.option()))
                                .append(
                                        Component.literal(
                                                "\n"
                                                        + this.price(
                                                                this.confirmation.option().price())
                                                        + "\n"
                                                        + this.tr("noGuaranteedReward")));
        var1.enableScissor(var2 + 12, var3 + 35, var2 + var4 - 12, var3 + 110);
        int var6 = var3 + 39;

        for (FormattedCharSequence var8 : this.font.split(var5, var4 - 34)) {
            var1.drawString(this.font, var8, var2 + 17, var6, -9022672, false);
            var6 += 12;
        }

        var1.disableScissor();
        int var9 = (var4 - 36) / 2;
        boolean var10 =
                this.confirmation.action() == CharlesScreen.Action.RESET_GRID
                        ? !this.model().isWaiting() && !this.model().isBetPending()
                        : this.canUse(this.selected());
        this.button(var1, var2 + 14, var3 + 119, var9, 20, this.tr("confirm"), var10);
        this.button(var1, var2 + 22 + var9, var3 + 119, var9, 20, this.tr("cancel"), true);
    }

    private void addHoverBody(List<Component> var1, Component var2) {
        if (var2 != null && var2.getStyle().getHoverEvent() instanceof ShowText var3) {
            var1.add(var3.value());
        }
    }

    private void renderTooltip(GuiGraphics var1, int var2, int var3) {
        ArrayList var4 = new ArrayList();
        if (this.boardShowing()) {
            if (hit(var2, var3, this.autoButtonX(), this.footerY(), this.autoButtonW(), 22)) {
                var4.add(ModLanguageManager.get("casino.modern.autoReveal"));
                var4.add(ModLanguageManager.get("casino.modern.autoTip"));
                var4.add(
                        ModLanguageManager.get(
                                "casino.modern."
                                        + (this.autoReveal.active()
                                                ? "autoRunning"
                                                : "autoStatus."
                                                        + this.autoReveal
                                                                .reason()
                                                                .name()
                                                                .toLowerCase(Locale.ROOT))));
                var4.add(Component.literal(this.price(this.model().getNextFlipPrice())));
                MenuScale207.tooltipSimple(
                        var1, this.font, var4, Optional.empty(), var2, var3, this);
                return;
            }

            GamblingModel.GridCell var5 = this.cellAt(var2, var3);
            if (var5 != null) {
                if (var5.isLocked()) {
                    var4.add(
                            ModLanguageManager.get(
                                    this.autoReveal.active()
                                            ? "casino.modern.autoRunning"
                                            : "casino.modern.clickCard"));
                    var4.add(Component.literal(this.price(this.model().getNextFlipPrice())));
                    if (this.model().getNextFlipPrice() < 0) {
                        var4.add(ModLanguageManager.get("casino.modern.loading"));
                    }

                    if (!this.model().isNearTable()) {
                        var4.add(
                                ModLanguageManager.get("charles.too_far.short")
                                        .withStyle(ChatFormatting.RED));
                    }

                    if (!this.model().isNextFlipAffordable()
                            || !this.affordable(this.model().getNextFlipPrice())) {
                        var4.add(
                                ModLanguageManager.get("charles.not_enough")
                                        .withStyle(ChatFormatting.RED));
                    }
                } else {
                    var4.add(ModLanguageManager.get("casino.modern.symbol." + var5.type().id));
                    this.addHoverBody(var4, var5.original());
                }
            }

            if (hit(
                    var2,
                    var3,
                    this.x + this.w / 2 - 20,
                    this.bodyTitleY(),
                    Math.max(1, this.rulesX() - this.x - this.w / 2 + 10),
                    14)) {
                var4.add(Component.literal(this.price(this.model().getNextFlipPrice())));
            }

            if (hit(var2, var3, this.resetX(), this.footerY(), this.boardButtonW(), 22)) {
                var4.add(ModLanguageManager.get("charles.grid.reset.tip"));
            }

            if (hit(var2, var3, this.backX(), this.footerY(), this.boardButtonW(), 22)) {
                var4.add(ModLanguageManager.get("charles.grid.menu.tip"));
            }

            List<GamblingModel.CountEntry> var6 = this.model().getCounts();
            if (!var6.isEmpty()
                    && hit(var2, var3, this.x + 20, this.footerY() + 26, this.w - 40, 17)) {
                for (GamblingModel.CountEntry var8 : var6) {
                    var4.add(
                            Component.literal(
                                    this.tr("symbol." + var8.type().id) + " × " + var8.count()));
                }
            }
        } else {
            CharlesScreen.Option var9 = this.cardAt(var2, var3);
            if (var9 != null) {
                var4.add(this.optionName(var9));
                var4.add(
                        Component.literal(
                                var9.locked()
                                        ? this.tr("locked")
                                        : (var9.resume()
                                                ? this.tr("resume")
                                                : this.price(var9.price()))));
                this.addHoverBody(var4, var9.label());
                var4.add(ModLanguageManager.get("casino.modern.select"));
            }

            if (hit(var2, var3, this.refreshX(), this.utilityY(), this.utilityW(), 18)) {
                var4.add(ModLanguageManager.get("charles.reload"));
            }
        }

        if (hit(var2, var3, this.rulesX(), this.utilityY(), this.utilityW(), 18)) {
            var4.add(ModLanguageManager.get("casino.modern.rules"));
        }

        if (hit(var2, var3, this.x + 20, this.footerY() + 28, this.w - 40, 19) && var4.isEmpty()) {
            var4.addAll(this.rules());
        }

        if (!var4.isEmpty()) {
            MenuScale207.tooltipSimple(var1, this.font, var4, Optional.empty(), var2, var3, this);
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

    private enum Action {
        BET,
        BUY_GRID,
        RESET_GRID;
    }

    private record Option(
            int trigger,
            Component label,
            int price,
            boolean locked,
            boolean resume,
            GamblingModel.BetEntry bet,
            GamblingModel.GridChoice grid) {}

    private record Pending(CharlesScreen.Action action, CharlesScreen.Option option) {}

    public enum Tab {
        BETS,
        GRIDS;
    }
}
