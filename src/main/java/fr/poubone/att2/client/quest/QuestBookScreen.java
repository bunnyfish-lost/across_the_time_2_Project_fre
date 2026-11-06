package fr.poubone.att2.client.quest;

import fr.poubone.att2.client.data.Att2Triggers;
import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.screen.GuiBackdrop1132;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class QuestBookScreen extends Screen {
    private static final SoundEvent BOOK_OPEN =
            SoundEvent.createVariableRangeEvent(
                    Identifier.fromNamespaceAndPath("att2", "ui.book.open"));
    private static final SoundEvent BOOK_TURN =
            SoundEvent.createVariableRangeEvent(
                    Identifier.fromNamespaceAndPath("att2", "ui.book.turn_page"));
    private static final int INNER = 16;
    private static final int GROUP_H = 19;
    private static final int ROW_H = 20;
    private static final int DETAIL_TOP_PADDING = 14;
    private int bookX;
    private int bookY;
    private int bookW;
    private int bookH;
    private int leftX;
    private int rightX;
    private int pageY;
    private int pageW;
    private int pageH;
    private int textX;
    private int textW;
    private int rightTextX;
    private int listTop;
    private int listBottom;
    private int leftScroll;
    private int leftContentHeight;
    private int currentPage;
    private int maxPage;
    private String selectedKey;
    private String searchText = "";
    private EditBox searchBox;
    private boolean draggingLeftScroll;
    private final List<List<QuestBookScreen.Row>> pages = new ArrayList<>();
    private String snapshot = "";
    private int hoverX;
    private int hoverY;

    private QuestBookScreen() {
        super(ModLanguageManager.get("quest_book.title"));
        this.selectedKey = QuestModel.get().getSelectedKey();
    }

    public static void open() {
        if (!HUDConfig.get().questMenuEnabled) {
            Att2Triggers.send(2315);
        } else {
            Minecraft var0 = Minecraft.getInstance();
            QuestModel.get().requestSideQuests();
            if (HUDConfig.get().questBookShowDaily
                    || QuestTracking.pinned() != null && QuestTracking.pinned().isDaily()) {
                QuestModel.get().requestDailyQuests();
            }

            var0.setScreen(new QuestBookScreen());
            QuestModel.get().refreshSelectedDetails();
            var0.getSoundManager().play(SimpleSoundInstance.forUI(BOOK_OPEN, 1.0F));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.bookW = Math.min(600, Math.max(1, this.width - 28));
        this.bookH = Math.min(318, Math.max(1, this.height - 40));
        this.bookX = (this.width - this.bookW) / 2;
        this.bookY = (this.height - this.bookH) / 2 + 8;
        this.pageW = (this.bookW - 24) / 2;
        this.pageH = this.bookH - 14;
        this.pageY = this.bookY + 7;
        this.leftX = this.bookX + 7;
        this.rightX = this.leftX + this.pageW + 10;
        this.textX = this.leftX + 16;
        this.rightTextX = this.rightX + 16;
        this.textW = this.pageW - 32;
        this.listTop = this.pageY + (this.stackedToolbar() ? 54 : 36);
        this.listBottom = this.pageY + this.pageH - 29;
        this.draggingLeftScroll = false;
        int var1 = this.searchWidth();
        this.searchBox =
                new QuestJournalSearchBox(
                        this.font,
                        this.rightTextX + 13,
                        this.pageY + 15,
                        var1 - 15,
                        12,
                        ModLanguageManager.get("quest_book.search"));
        this.searchBox.setBordered(false);
        this.searchBox.setMaxLength(64);
        this.searchBox.setTextColor(-12966894);
        this.searchBox.setTextColorUneditable(-1771155924);
        this.searchBox.setValue(this.searchText);
        this.searchBox.setResponder(
                var1x -> {
                    this.searchText = var1x;
                    this.currentPage = 0;
                    this.rebuild();
                });
        this.addRenderableWidget(this.searchBox);
        this.rebuild();
    }

    private int ribbonX() {
        return this.bookX + this.bookW - 200;
    }

    private int ribbonW() {
        return 50;
    }

    private int pageFooterY() {
        return this.pageY + this.pageH - 26;
    }

    private int refreshWidth() {
        return Math.min(
                Math.max(24, (this.textW - 6) / 2),
                Math.min(
                        72,
                        Math.max(
                                40,
                                this.font.width(ModLanguageManager.getString("quest_book.reload"))
                                        + 12)));
    }

    private int completedWidth() {
        return Math.min(
                Math.max(24, (this.textW - 6) / 2),
                Math.min(
                        76,
                        Math.max(
                                40,
                                this.font.width(
                                                ModLanguageManager.getString(
                                                        "quest_book.tab.completed"))
                                        + 16)));
    }

    private boolean stackedToolbar() {
        return this.textW < 55 + this.completedWidth() + this.refreshWidth() + 12;
    }

    private int searchWidth() {
        return this.stackedToolbar()
                ? this.textW
                : this.textW - this.completedWidth() - this.refreshWidth() - 12;
    }

    private int refreshX() {
        return this.rightTextX + this.textW - this.refreshWidth();
    }

    private int completedX() {
        return this.refreshX() - 6 - this.completedWidth();
    }

    private int toolbarY() {
        return this.pageY + (this.stackedToolbar() ? 31 : 13);
    }

    private int guideX() {
        return this.textX + 78;
    }

    private boolean canInspectQuest() {
        QuestInfo var1 = this.selected();
        return var1 != null && (var1.isMain() || var1.hasDetails());
    }

    private void inspectQuest() {
        QuestInfo var1 = this.selected();
        if (var1 != null && this.canInspectQuest()) {
            this.onClose();
            QuestTracking.track(var1);
        }
    }

    private List<FormattedCharSequence> selectedTitleLines() {
        QuestInfo var1 = this.selected();
        if (var1 == null) {
            return List.of();
        }

        int var2 = Math.max(24, (int) (this.textW / 1.35F) - 2);
        return this.font.split(Component.literal(var1.plainName()), var2);
    }

    private int titleExtraHeight() {
        return Math.max(0, this.selectedTitleLines().size() - 1) * 14;
    }

    private int leftViewTop() {
        QuestInfo var1 = this.selected();
        if (var1 == null) {
            return this.pageY + 42;
        }

        int var2 = this.titleExtraHeight();
        return var1.isMain() || var1.isDaily() && var1.remainingSeconds() >= 0
                ? this.pageY + 100 + var2
                : this.pageY + 70 + var2;
    }

    private int leftViewBottom() {
        return this.pageFooterY() - 8;
    }

    private int leftViewWidth() {
        return Math.max(20, this.textW - 6);
    }

    private int leftMaxScroll() {
        return Math.max(
                0,
                this.leftContentHeight - Math.max(1, this.leftViewBottom() - this.leftViewTop()));
    }

    private List<QuestInfo> quests() {
        QuestModel var1 = QuestModel.get();
        HUDConfig var2 = HUDConfig.get();
        ArrayList<QuestInfo> var3 = new ArrayList();
        if (var2.questBookShowMain) {
            var3.add(var1.getMainQuest());
        }

        if (var2.questBookShowSide) {
            for (QuestInfo var5 : var1.getSideQuests()) {
                if (var2.questBookShowCompleted || var5.status() != QuestStatus.COMPLETED) {
                    var3.add(var5);
                }
            }
        }

        if (var2.questBookShowDaily) {
            for (QuestInfo var8 : var1.getDailyQuests()) {
                if (var2.questBookShowCompleted || var8.status() != QuestStatus.COMPLETED) {
                    var3.add(var8);
                }
            }
        }

        String var7 = this.searchText.toLowerCase(Locale.ROOT).trim();
        if (!var7.isEmpty()) {
            var3.removeIf(var1x -> !var1x.plainName().toLowerCase(Locale.ROOT).contains(var7));
        }

        QuestInfo var9 = QuestTracking.pinned();
        if (var9 != null) {
            var3.removeIf(var1x -> var1x.key().equals(var9.key()));
            var3.add(0, var9);
        }

        return var3;
    }

    private String groupName(QuestInfo var1) {
        if (var1.isMain()) {
            return ModLanguageManager.getString("quest_book.filter.main");
        } else {
            return var1.isDaily()
                    ? ModLanguageManager.getString("quest_book.filter.daily")
                            + " · "
                            + var1.cityName().getString()
                    : ModLanguageManager.getString("quest_book.filter.side");
        }
    }

    private int addPinnedRow(List<QuestBookScreen.Row> var1, QuestInfo var2) {
        int var3 = this.listTop;
        if (var2 != null) {
            if (this.listBottom - this.listTop >= 78) {
                var1.add(
                        new QuestBookScreen.Row(
                                null, ModLanguageManager.getString("quest_book.pinned"), var3));
                var3 += 19;
            }

            var1.add(new QuestBookScreen.Row(var2, null, var3));
            var3 += 20;
        }

        return var3;
    }

    private void rebuild() {
        List<QuestInfo> var1 = this.quests();
        QuestInfo var2 = QuestTracking.pinned();
        StringBuilder var3 = new StringBuilder();

        for (QuestInfo var5 : var1) {
            var3.append(var5.key())
                    .append(':')
                    .append(var5.plainName())
                    .append(':')
                    .append(var5.status())
                    .append(':')
                    .append(var5.remainingSeconds())
                    .append(';');
        }

        var3.append('|')
                .append(this.bookW)
                .append('|')
                .append(this.bookH)
                .append('|')
                .append(var2 == null ? "" : var2.key());
        var3.append('|').append(this.listTop).append('|').append(this.listBottom);
        if (!var3.toString().equals(this.snapshot)) {
            this.snapshot = var3.toString();
            this.pages.clear();
            ArrayList var14 = new ArrayList();
            this.pages.add(var14);
            int var15 = this.addPinnedRow(var14, var2);
            String var6 = null;
            boolean var7 = false;
            boolean var8 = this.listBottom - this.listTop >= (var2 == null ? 39 : 78);

            for (QuestInfo var10 : var1) {
                if (var2 == null || !var10.key().equals(var2.key())) {
                    String var11 = this.groupName(var10);
                    boolean var12 = var8 && !var11.equals(var6);
                    int var13 = 20 + (var12 ? 19 : 0);
                    if (var15 + var13 > this.listBottom && var7) {
                        var14 = new ArrayList();
                        this.pages.add(var14);
                        var15 = this.addPinnedRow(var14, var2);
                        var12 = var8;
                        var7 = false;
                    }

                    if (var12) {
                        var14.add(new QuestBookScreen.Row(null, var11, var15));
                        var15 += 19;
                    }

                    var14.add(new QuestBookScreen.Row(var10, null, var15));
                    var15 += 20;
                    var6 = var11;
                    var7 = true;
                }
            }

            this.maxPage = this.pages.size() - 1;
            this.currentPage = Mth.clamp(this.currentPage, 0, this.maxPage);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.rebuild();
    }

    public String getSelectedKey() {
        return this.selectedKey;
    }

    public float textScale() {
        return 1.0F;
    }

    public void selectQuest(QuestInfo var1) {
        if (!var1.key().equals(this.selectedKey)) {
            this.leftScroll = 0;
        }

        this.selectedKey = var1.key();
        QuestModel.get().setSelected(var1);
        QuestModel.get().requestDetails(var1);
    }

    private QuestInfo selected() {
        QuestInfo var1 = QuestModel.get().findByKey(this.selectedKey);
        if (var1 != null) {
            return var1;
        }

        QuestInfo var2 = QuestTracking.pinned();
        return var2 != null && var2.key().equals(this.selectedKey) ? var2 : null;
    }

    private void changePage(int var1) {
        int var2 = Mth.clamp(var1, 0, this.maxPage);
        if (var2 != this.currentPage) {
            this.currentPage = var2;
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(BOOK_TURN, 1.0F));
        }
    }

    private URI guidePage() {
        QuestInfo var1 = this.selected();
        if (var1 == null) {
            return null;
        }

        int var2 = var1.isMain() ? QuestModel.get().getMainStep() : var1.number();
        return GuideLinks.page(
                var1.type(), var2, var1.city(), ModLanguageManager.getCurrentLanguage());
    }

    private void openGuide() {
        URI var1 = this.guidePage();
        if (var1 != null) {
            try {
                Util.getPlatform().openUri(var1);
            } catch (RuntimeException var3) {
                System.err.println("[ATT2] Could not open quest guide: " + var3.getMessage());
            }
        }
    }

    private void refresh() {
        QuestModel.get().requestSideQuests();
        if (HUDConfig.get().questBookShowDaily
                || QuestTracking.pinned() != null && QuestTracking.pinned().isDaily()) {
            QuestModel.get().requestDailyQuests();
        }

        QuestModel.get().refreshSelectedDetails();
    }

    private void toggleCategory(QuestType var1, boolean var2) {
        HUDConfig var3 = HUDConfig.get();
        if (var2) {
            var3.questBookShowMain = var1 == QuestType.MAIN;
            var3.questBookShowSide = var1 == QuestType.SIDE;
            var3.questBookShowDaily = var1 == QuestType.DAILY;
        } else {
            switch (var1) {
                case MAIN:
                    var3.questBookShowMain = !var3.questBookShowMain;
                    break;
                case SIDE:
                    var3.questBookShowSide = !var3.questBookShowSide;
                    break;
                case DAILY:
                    var3.questBookShowDaily = !var3.questBookShowDaily;
            }
        }

        if (var3.questBookShowDaily && !QuestModel.get().hasDailyQuestData()) {
            QuestModel.get().requestDailyQuests();
        }

        HUDConfig.save();
        this.currentPage = 0;
        this.snapshot = "";
        this.rebuild();
    }

    private boolean active(QuestType var1) {
        HUDConfig var2 = HUDConfig.get();

        return switch (var1) {
            case MAIN -> var2.questBookShowMain;
            case SIDE -> var2.questBookShowSide;
            case DAILY -> var2.questBookShowDaily;
        };
    }

    private static boolean inside(
            double var0, double var2, int var4, int var5, int var6, int var7) {
        return var0 >= var4 && var0 < var4 + var6 && var2 >= var5 && var2 < var5 + var7;
    }

    private boolean overLeftScrollbar(double var1, double var3) {
        return this.leftMaxScroll() > 0
                && inside(
                        var1,
                        var3,
                        this.textX + this.leftViewWidth() - 2,
                        this.leftViewTop(),
                        8,
                        this.leftViewBottom() - this.leftViewTop());
    }

    private void scrollLeftTo(double var1) {
        int var3 = this.leftViewTop();
        int var4 = this.leftViewBottom();
        int var5 = var4 - var3;
        int var6 = Math.max(8, var5 * var5 / Math.max(1, this.leftContentHeight));
        double var7 = (var1 - var3 - var6 / 2.0) / Math.max(1, var5 - var6);
        this.leftScroll =
                Mth.clamp((int) Math.round(var7 * this.leftMaxScroll()), 0, this.leftMaxScroll());
    }

    private void unfocusSearch() {
        if (this.searchBox != null) {
            this.searchBox.setFocused(false);
        }

        this.setFocused(null);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
        double var3 = var1.x();
        double var5 = var1.y();
        int var7 = var1.button();
        boolean var8 =
                this.searchBox != null
                        && inside(
                                var3,
                                var5,
                                this.searchBox.getX(),
                                this.searchBox.getY(),
                                this.searchBox.getWidth(),
                                this.searchBox.getHeight());
        if (var7 == 1 && var8) {
            this.searchBox.setValue("");
            this.unfocusSearch();
            return true;
        }

        if (!var8) {
            this.unfocusSearch();
        }

        if (var7 == 2) {
            if (inside(
                    var3,
                    var5,
                    this.rightTextX,
                    this.listTop,
                    this.textW,
                    this.listBottom - this.listTop)) {
                for (QuestBookScreen.Row var18 : this.pages.get(this.currentPage)) {
                    if (var18.quest != null
                            && inside(
                                    var3, var5, this.rightTextX - 4, var18.y, this.textW + 6, 19)) {
                        QuestTracking.togglePin(var18.quest);
                        this.currentPage = 0;
                        this.snapshot = "";
                        this.rebuild();
                        return true;
                    }
                }
            }

            return super.mouseClicked(var1, var2);
        } else if (var7 == 1) {
            if (inside(
                    var3,
                    var5,
                    this.rightTextX,
                    this.listTop,
                    this.textW,
                    this.listBottom - this.listTop)) {
                for (QuestBookScreen.Row var17 : this.pages.get(this.currentPage)) {
                    if (var17.quest != null
                            && inside(
                                    var3, var5, this.rightTextX - 4, var17.y, this.textW + 6, 19)) {
                        if (var17.quest.key().equals(this.selectedKey)) {
                            this.selectedKey = null;
                            this.leftScroll = 0;
                            QuestModel.get().setSelected(null);
                        }

                        return true;
                    }
                }
            }

            return super.mouseClicked(var1, var2);
        } else {
            if (var7 != 0) {
                return super.mouseClicked(var1, var2);
            }

            if (inside(var3, var5, this.bookX + this.bookW - 12, this.bookY - 7, 22, 22)) {
                this.onClose();
                return true;
            }

            for (int var9 = 0; var9 < 3; var9++) {
                int var10 = this.ribbonX() + var9 * 54;
                if (inside(var3, var5, var10, this.bookY - 15, this.ribbonW(), 23)) {
                    this.toggleCategory(QuestType.values()[var9], !var1.hasShiftDown());
                    return true;
                }
            }

            if (inside(var3, var5, this.refreshX(), this.toolbarY(), this.refreshWidth(), 16)) {
                this.refresh();
                return true;
            }

            if (inside(var3, var5, this.completedX(), this.toolbarY(), this.completedWidth(), 16)) {
                HUDConfig var13 = HUDConfig.get();
                var13.questBookShowCompleted = !var13.questBookShowCompleted;
                HUDConfig.save();
                this.currentPage = 0;
                this.snapshot = "";
                this.rebuild();
                return true;
            }

            if (inside(
                    var3,
                    var5,
                    this.rightTextX,
                    this.listTop,
                    this.textW,
                    this.listBottom - this.listTop)) {
                for (QuestBookScreen.Row var16 : this.pages.get(this.currentPage)) {
                    if (var16.quest != null
                            && inside(
                                    var3, var5, this.rightTextX - 4, var16.y, this.textW + 6, 19)) {
                        this.selectQuest(var16.quest);
                        return true;
                    }
                }
            }

            int var12 = this.pageFooterY();
            if (inside(var3, var5, this.rightTextX - 2, var12, 24, 16)) {
                this.changePage(this.currentPage - 1);
                return true;
            }

            if (inside(var3, var5, this.rightTextX + this.textW - 24, var12, 24, 16)) {
                this.changePage(this.currentPage + 1);
                return true;
            }

            if (inside(var3, var5, this.textX, var12, 72, 17)) {
                this.inspectQuest();
                return true;
            }

            if (inside(var3, var5, this.guideX(), var12, 72, 17)) {
                if (this.guidePage() != null) {
                    this.openGuide();
                }

                return true;
            } else if (this.overLeftScrollbar(var3, var5)) {
                this.draggingLeftScroll = true;
                this.scrollLeftTo(var5);
                return true;
            } else {
                return super.mouseClicked(var1, var2);
            }
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
        if (this.draggingLeftScroll && var1.button() == 0) {
            this.scrollLeftTo(var1.y());
            return true;
        } else {
            return super.mouseDragged(var1, var2, var4);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent var1) {
        if (this.draggingLeftScroll && var1.button() == 0) {
            this.draggingLeftScroll = false;
            return true;
        } else {
            return super.mouseReleased(var1);
        }
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        if (var7 != 0.0
                && inside(
                        var1,
                        var3,
                        this.textX,
                        this.leftViewTop(),
                        this.textW,
                        this.leftViewBottom() - this.leftViewTop())
                && this.leftMaxScroll() > 0) {
            this.leftScroll =
                    Mth.clamp(
                            this.leftScroll
                                    - (int) Math.signum(var7)
                                            * Math.max(9, (int) (Math.abs(var7) * 9.0)),
                            0,
                            this.leftMaxScroll());
            return true;
        } else if (var7 != 0.0
                && inside(
                        var1,
                        var3,
                        this.rightTextX,
                        this.listTop,
                        this.textW,
                        this.listBottom - this.listTop)) {
            this.changePage(this.currentPage - (int) Math.signum(var7));
            return true;
        } else {
            return super.mouseScrolled(var1, var3, var5, var7);
        }
    }

    @Override
    public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
        super.renderBackground(var1, var2, var3, var4);
        if (this.pageW > 0) {
            GuiBackdrop1132.fill(var1, 0, 0, this.width, this.height, -1509949440, this);
            QuestJournalArt.cover(var1, this.bookX, this.bookY, this.bookW, this.bookH);
            QuestPaper111.page(var1, this.leftX, this.pageY, this.pageW, this.pageH, true);
            QuestPaper111.page(var1, this.rightX, this.pageY, this.pageW, this.pageH, false);
            QuestJournalArt.gutterShadow(
                    var1, this.leftX + this.pageW, this.pageY, this.pageH, true);
            QuestJournalArt.gutterShadow(var1, this.rightX, this.pageY, this.pageH, false);
            QuestJournalArt.spine(
                    var1, this.leftX + this.pageW, this.bookY + 4, this.bookY + this.bookH - 4);
            String var5 = ModLanguageManager.getString("quest_book.journal");
            if ("zh".equals(ModLanguageManager.getCurrentLanguage())) {
                var5 = "醒神之书";
            }

            var1.pose().pushMatrix();
            var1.pose().translate(this.bookX + 4, this.bookY - 17);
            var1.pose().scale(1.35F, 1.35F);
            var1.drawString(this.font, var5, 0, 0, -3562150, false);
            var1.pose().popMatrix();
            String var6 = ModLanguageManager.getString("quest_book.title");
            var1.drawString(
                    this.font,
                    " · " + var6,
                    this.bookX + 8 + Math.round(this.font.width(var5) * 1.35F),
                    this.bookY - 14,
                    -4679070,
                    false);
            QuestType[] var7 = QuestType.values();

            for (int var8 = 0; var8 < var7.length; var8++) {
                QuestType var9 = var7[var8];
                String var10 =
                        ModLanguageManager.getString(
                                "quest_book.tab." + var9.name().toLowerCase(Locale.ROOT));
                int var11 = this.ribbonX() + var8 * 54;
                boolean var12 = inside(var2, var3, var11, this.bookY - 15, this.ribbonW(), 23);
                QuestRibbon1130.ribbon(
                        var1,
                        this.font,
                        var11,
                        this.bookY - 15,
                        this.ribbonW(),
                        var9,
                        var10,
                        this.active(var9),
                        var12);
                QuestJournalArt.ribbonTail(
                        var1, var11, this.bookY, this.ribbonW(), 5, var9, this.active(var9), var12);
            }

            QuestJournalArt.closeSeal(
                    var1,
                    this.bookX + this.bookW - 2,
                    this.bookY + 1,
                    inside(var2, var3, this.bookX + this.bookW - 12, this.bookY - 7, 22, 22));
            this.drawStaticLeft(var1, var2, var3);
            this.drawStaticRight(var1, var2, var3);
        }
    }

    private void drawStaticLeft(GuiGraphics var1, int var2, int var3) {
        QuestInfo var4 = this.selected();
        if (var4 == null) {
            var1.drawString(
                    this.font,
                    ModLanguageManager.get("quest_book.objective"),
                    this.textX,
                    this.pageY + 17,
                    -9548244,
                    false);
            QuestJournalArt.ornamentRule(
                    var1, this.textX, this.textX + this.textW, this.pageY + 39, -1433769412);
        } else {
            String var5 =
                    ModLanguageManager.getString(
                            "quest_book.filter." + var4.type().name().toLowerCase(Locale.ROOT));
            if (var4.type() == QuestType.SIDE) {
                var5 = var5 + "  #" + var4.number();
            }

            var1.drawString(this.font, var5, this.textX, this.pageY + 15, -9548244, false);
            String var6 =
                    ModLanguageManager.getString(
                            "quest_book.status." + var4.status().name().toLowerCase(Locale.ROOT));
            QuestJournalArt.stamp(
                    var1,
                    this.font,
                    this.textX + this.textW,
                    this.pageY + 12,
                    var6,
                    QuestJournalArt.statusColor(var4.status()));
            List<FormattedCharSequence> var7 = this.selectedTitleLines();
            var1.pose().pushMatrix();
            var1.pose().translate(this.textX, this.pageY + 32);
            var1.pose().scale(1.35F, 1.35F);
            byte var8 = 0;

            for (FormattedCharSequence var10 : var7) {
                var1.drawString(this.font, var10, 0, var8, -12966894, false);
                var8 = (byte) (var8 + 10);
            }

            var1.pose().popMatrix();
            int var14 = this.titleExtraHeight();
            QuestJournalArt.ornamentRule(
                    var1,
                    this.textX,
                    this.textX + this.textW,
                    this.pageY + 55 + var14,
                    -1433769412);
            int var15 = this.pageY + 65 + var14;
            if (var4.isMain()) {
                int var11 = QuestModel.get().getMainStep();
                String var12 =
                        ModLanguageManager.format(
                                "quest_book.main_progress",
                                "step",
                                var11 < 0 ? "?" : var11,
                                "max",
                                QuestModel.get().getMainMaxStep());
                var1.drawString(this.font, var12, this.textX, var15, -9548244, false);
                if (var11 >= 0 && QuestModel.get().getMainMaxStep() > 0) {
                    QuestJournalArt.progressBar(
                            var1,
                            this.textX,
                            var15 + 12,
                            Math.min(150, this.textW),
                            (float) var11 / QuestModel.get().getMainMaxStep());
                }
            } else if (var4.isDaily()) {
                var1.drawString(this.font, var4.cityName(), this.textX, var15, -9548244, false);
                if (var4.remainingSeconds() >= 0) {
                    var1.drawString(
                            this.font,
                            var4.formatRemainingTime(),
                            this.textX,
                            var15 + 12,
                            -9548244,
                            false);
                }
            }

            var1.drawString(
                    this.font,
                    ModLanguageManager.get("quest_book.objective"),
                    this.textX,
                    this.leftViewTop() - 4,
                    -12966894,
                    false);
        }

        int var13 = this.pageFooterY();
        QuestJournalArt.ornamentRule(
                var1, this.textX, this.textX + this.textW, var13 - 5, 1519020604);
        QuestJournalArt.button(
                var1,
                this.font,
                this.textX,
                var13,
                72,
                16,
                ModLanguageManager.getString("quest_book.inspect"),
                inside(var2, var3, this.textX, var13, 72, 17),
                this.canInspectQuest(),
                false);
        QuestJournalArt.button(
                var1,
                this.font,
                this.guideX(),
                var13,
                72,
                16,
                ModLanguageManager.getString("quest_book.guide"),
                inside(var2, var3, this.guideX(), var13, 72, 17),
                this.guidePage() != null,
                true);
    }

    private void drawStaticRight(GuiGraphics var1, int var2, int var3) {
        QuestJournalArt.magnifier(var1, this.rightTextX, this.pageY + 17, -9548244);
        QuestJournalArt.dottedRule(
                var1,
                this.rightTextX,
                this.rightTextX + this.searchWidth(),
                this.pageY + 27,
                -1433769412);
        boolean var4 =
                inside(var2, var3, this.completedX(), this.toolbarY(), this.completedWidth(), 16);
        QuestJournalArt.checkbox(
                var1,
                this.completedX(),
                this.toolbarY() + 3,
                HUDConfig.get().questBookShowCompleted,
                var4);
        String var5 = ModLanguageManager.getString("quest_book.tab.completed");
        int var6 = this.completedWidth() - 13;
        if (this.font.width(var5) > var6) {
            var5 =
                    this.font.plainSubstrByWidth(var5, Math.max(1, var6 - this.font.width("…")))
                            + "…";
        }

        var1.drawString(
                this.font, var5, this.completedX() + 12, this.toolbarY() + 4, -9548244, false);
        QuestJournalArt.button(
                var1,
                this.font,
                this.refreshX(),
                this.toolbarY(),
                this.refreshWidth(),
                16,
                ModLanguageManager.getString("quest_book.reload"),
                inside(var2, var3, this.refreshX(), this.toolbarY(), this.refreshWidth(), 16),
                true,
                false);
        int var7 = this.pageFooterY();
        if (this.currentPage > 0) {
            QuestJournalArt.pageArrow(
                    var1,
                    this.rightTextX - 2,
                    var7 + 2,
                    false,
                    inside(var2, var3, this.rightTextX - 2, var7, 24, 16));
        }

        if (this.currentPage < this.maxPage) {
            QuestJournalArt.pageArrow(
                    var1,
                    this.rightTextX + this.textW - 24,
                    var7 + 2,
                    true,
                    inside(var2, var3, this.rightTextX + this.textW - 24, var7, 24, 16));
        }

        String var8 = this.currentPage + 1 + " / " + (this.maxPage + 1);
        var1.drawString(
                this.font,
                var8,
                this.rightTextX + (this.textW - this.font.width(var8)) / 2,
                var7 + 3,
                -12966894,
                false);
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        this.hoverX = var2;
        this.hoverY = var3;
        super.render(var1, var2, var3, var4);
        this.renderDetails(var1);
        this.renderList(var1, var2, var3);
        this.renderTooltip(var1, var2, var3);
    }

    private List<QuestBookScreen.Block> detailBlocks(QuestInfo var1) {
        ArrayList var2 = new ArrayList();
        if (var1 == null) {
            var2.add(
                    new QuestBookScreen.Block(
                            ModLanguageManager.get("quest_book.description1"), -9548244, 8));
            var2.add(
                    new QuestBookScreen.Block(
                            ModLanguageManager.get("quest_book.description2"), -9548244, 0));
            return var2;
        }

        List<Component> var3 = QuestModel.get().getDescription(var1);
        if (var3.isEmpty()) {
            var2.add(
                    new QuestBookScreen.Block(
                            QuestModel.get().isCapturing()
                                    ? ModLanguageManager.get("quest_book.loading")
                                    : ModLanguageManager.get("quest_book.no_description"),
                            -9548244,
                            0));
        } else {
            for (Component var5 : var3) {
                var2.add(new QuestBookScreen.Block(var5, -12966894, 3));
            }
        }

        return var2;
    }

    private void renderDetails(GuiGraphics var1) {
        int var2 = this.leftViewTop();
        int var3 = this.leftViewBottom();
        int var4 = this.leftViewWidth();
        if (var2 < var3) {
            List<QuestBookScreen.Block> var5 = this.detailBlocks(this.selected());
            int var6 = Math.max(32, var4 - 24);
            int var7 = 16;

            for (QuestBookScreen.Block var9 : var5) {
                var7 += this.font.split(var9.text, var6).size() * 12 + var9.gap;
            }

            this.leftContentHeight = var7;
            this.leftScroll = Mth.clamp(this.leftScroll, 0, this.leftMaxScroll());
            QuestDetailClip1112.enable(var1, this.textX - 4, var2, this.textX + var4 + 4, var3);
            int var13 = var2 + 14 - this.leftScroll;

            for (QuestBookScreen.Block var10 : var5) {
                for (FormattedCharSequence var12 : this.font.split(var10.text, var6)) {
                    if (var13 + 11 >= var2 && var13 < var3) {
                        QuestDetailClip1112.draw(
                                var1, this.font, var12, this.textX, var13, var10.color, false);
                    }

                    var13 += 12;
                }

                var13 += var10.gap;
            }

            var1.disableScissor();
            if (this.leftMaxScroll() > 0) {
                int var15 = var3 - var2;
                int var16 = Math.max(8, var15 * var15 / this.leftContentHeight);
                int var17 = this.textX + var4 - 1;
                int var18 =
                        var2 + this.leftScroll * Math.max(0, var15 - var16) / this.leftMaxScroll();
                GuiBackdrop1132.fill(var1, var17, var2, var17 + 2, var3, 678317612, this);
                GuiBackdrop1132.fill(
                        var1, var17, var18, var17 + 2, var18 + var16, -1435611604, this);
            }
        }
    }

    private void renderList(GuiGraphics var1, int var2, int var3) {
        if (!this.pages.isEmpty()) {
            var1.enableScissor(
                    this.rightTextX - 4,
                    this.listTop,
                    this.rightTextX + this.textW + 4,
                    this.listBottom);
            List<QuestBookScreen.Row> var4 = this.pages.get(this.currentPage);

            for (QuestBookScreen.Row var6 : var4) {
                if (var6.quest == null) {
                    String var7 = var6.group;
                    var1.drawString(this.font, var7, this.rightTextX, var6.y + 6, -9548244, false);
                    int var8 = this.rightTextX + this.font.width(var7) + 5;
                    if (var8 < this.rightTextX + this.textW) {
                        GuiBackdrop1132.fill(
                                var1,
                                var8,
                                var6.y + 10,
                                this.rightTextX + this.textW,
                                var6.y + 11,
                                -1433769412,
                                this);
                    }
                } else {
                    QuestInfo var16 = var6.quest;
                    boolean var18 = var16.key().equals(this.selectedKey);
                    boolean var9 =
                            inside(var2, var3, this.rightTextX - 4, var6.y, this.textW + 6, 19);
                    if (var18) {
                        GuiBackdrop1132.fill(
                                var1,
                                this.rightTextX - 4,
                                var6.y,
                                this.rightTextX + this.textW + 2,
                                var6.y + 20 - 2,
                                1116494370,
                                this);
                        GuiBackdrop1132.fill(
                                var1,
                                this.rightTextX - 4,
                                var6.y,
                                this.rightTextX - 2,
                                var6.y + 20 - 2,
                                -7463132,
                                this);
                    } else if (var9) {
                        GuiBackdrop1132.fill(
                                var1,
                                this.rightTextX - 4,
                                var6.y,
                                this.rightTextX + this.textW + 2,
                                var6.y + 20 - 2,
                                578440990,
                                this);
                    }

                    if (QuestTracking.isPinned(var16)) {
                        GuiBackdrop1132.fill(
                                var1,
                                this.rightTextX - 4,
                                var6.y + 3,
                                this.rightTextX - 2,
                                var6.y + 16,
                                -6785214,
                                this);
                        GuiBackdrop1132.fill(
                                var1,
                                this.rightTextX - 5,
                                var6.y + 3,
                                this.rightTextX - 1,
                                var6.y + 5,
                                -6785214,
                                this);
                    }

                    QuestJournalArt.seal(var1, this.rightTextX + 5, var6.y + 9, var16.status());
                    String var10 =
                            var16.isMain()
                                    ? "#" + Math.max(0, QuestModel.get().getMainStep())
                                    : (var16.isDaily()
                                            ? var16.formatRemainingTime()
                                            : "#" + var16.number());
                    int var11 = Math.max(24, this.textW - 23 - this.font.width(var10));
                    String var12 = var16.plainName();
                    if (this.font.width(var12) > var11) {
                        var12 =
                                this.font.plainSubstrByWidth(var12, var11 - this.font.width("…"))
                                        + "…";
                    }

                    int var13 = var16.status() == QuestStatus.COMPLETED ? -9548244 : -12966894;
                    var1.drawString(
                            this.font, var12, this.rightTextX + 14, var6.y + 5, var13, false);
                    if (var16.status() == QuestStatus.COMPLETED) {
                        GuiBackdrop1132.fill(
                                var1,
                                this.rightTextX + 14,
                                var6.y + 9,
                                this.rightTextX + 14 + this.font.width(var12),
                                var6.y + 10,
                                -1771155924,
                                this);
                    }

                    var1.drawString(
                            this.font,
                            var10,
                            this.rightTextX + this.textW - 2 - this.font.width(var10),
                            var6.y + 5,
                            -9548244,
                            false);
                    if (!var18) {
                        QuestJournalArt.dottedRule(
                                var1,
                                this.rightTextX + 14,
                                this.rightTextX + this.textW,
                                var6.y + 20 - 1,
                                1519020604);
                    }
                }
            }

            var1.disableScissor();
            if (var4.isEmpty()) {
                MutableComponent var14 =
                        !QuestModel.get().isWaitingForSideQuests()
                                        && !QuestModel.get().isWaitingForDailyQuests()
                                ? ModLanguageManager.get("quest_book.empty")
                                : ModLanguageManager.get("quest_book.loading");
                int var15 = this.listTop + 8;

                for (FormattedCharSequence var19 : this.font.split(var14, this.textW)) {
                    var1.drawString(this.font, var19, this.rightTextX, var15, -9548244, false);
                    var15 += 12;
                }
            }
        }
    }

    private void renderTooltip(GuiGraphics var1, int var2, int var3) {
        ArrayList var4 = new ArrayList();
        if (inside(var2, var3, this.bookX + this.bookW - 12, this.bookY - 7, 22, 22)) {
            var4.add(ModLanguageManager.get("quest_book.close"));
        }

        for (int var5 = 0; var5 < 3; var5++) {
            if (inside(
                    var2, var3, this.ribbonX() + var5 * 54, this.bookY - 15, this.ribbonW(), 23)) {
                String var6 = QuestType.values()[var5].name().toLowerCase(Locale.ROOT);
                var4.add(ModLanguageManager.get("quest_book.filter." + var6));
                var4.add(
                        ModLanguageManager.get("quest_book.filter.only")
                                .withStyle(ChatFormatting.GRAY));
                var4.add(
                        ModLanguageManager.get("quest_book.filter.toggle")
                                .withStyle(ChatFormatting.GRAY));
            }
        }

        if (this.searchBox != null
                && inside(
                        var2,
                        var3,
                        this.searchBox.getX(),
                        this.searchBox.getY(),
                        this.searchBox.getWidth(),
                        this.searchBox.getHeight())) {
            var4.add(ModLanguageManager.get("quest_book.search.clear"));
        }

        if (inside(var2, var3, this.completedX(), this.toolbarY(), this.completedWidth(), 16)) {
            var4.add(ModLanguageManager.get("quest_book.filter.completed"));
        }

        if (inside(var2, var3, this.textX, this.pageFooterY(), 72, 17)) {
            var4.add(ModLanguageManager.get("quest_book.inspect.tip"));
        }

        if (inside(var2, var3, this.guideX(), this.pageFooterY(), 72, 17)
                && this.guidePage() != null) {
            var4.add(ModLanguageManager.get("quest_book.guide.open"));
        }

        if (inside(var2, var3, this.refreshX(), this.toolbarY(), this.refreshWidth(), 16)) {
            var4.add(ModLanguageManager.get("quest_book.reload"));
        }

        if (inside(
                var2,
                var3,
                this.rightTextX,
                this.listTop,
                this.textW,
                this.listBottom - this.listTop)) {
            for (QuestBookScreen.Row var8 : this.pages.get(this.currentPage)) {
                if (var8.quest != null
                        && inside(var2, var3, this.rightTextX - 4, var8.y, this.textW + 6, 19)) {
                    var4.add(var8.quest.name());
                    var4.add(
                            ModLanguageManager.get(
                                    "quest_book.status."
                                            + var8.quest.status().name().toLowerCase(Locale.ROOT)));
                    var4.add(
                            ModLanguageManager.get("quest_book.tooltip.left_click")
                                    .withStyle(ChatFormatting.GRAY));
                    var4.add(
                            ModLanguageManager.get(
                                            QuestTracking.isPinned(var8.quest)
                                                    ? "quest_book.pin.remove"
                                                    : "quest_book.pin.add")
                                    .withStyle(ChatFormatting.GRAY));
                    if (var8.quest.key().equals(this.selectedKey)) {
                        var4.add(
                                ModLanguageManager.get("quest_book.tooltip.right_click")
                                        .withStyle(ChatFormatting.GRAY));
                    }
                    break;
                }
            }
        }

        if (!var4.isEmpty()) {
            var1.setTooltipForNextFrame(this.font, var4, Optional.empty(), var2, var3);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent var1) {
        if (var1.key() == 256) {
            this.onClose();
            return true;
        } else if (this.searchBox != null && this.searchBox.isFocused()) {
            return this.searchBox.keyPressed(var1) || super.keyPressed(var1);
        } else if (var1.key() == 267) {
            this.changePage(this.currentPage + 1);
            return true;
        } else if (var1.key() == 266) {
            this.changePage(this.currentPage - 1);
            return true;
        } else {
            return super.keyPressed(var1);
        }
    }

    @Override
    public void removed() {
        QuestModel.get().onBookClosed();
        super.removed();
    }

    private static final class Block {
        final Component text;
        final int color;
        final int gap;

        Block(Component var1, int var2, int var3) {
            this.text = var1;
            this.color = var2;
            this.gap = var3;
        }
    }

    private static final class Row {
        final QuestInfo quest;
        final String group;
        final int y;

        Row(QuestInfo var1, String var2, int var3) {
            this.quest = var1;
            this.group = var2;
            this.y = var3;
        }
    }
}
