package fr.poubone.att2.client.screen;

import fr.poubone.att2.client.data.Att2Triggers;
import fr.poubone.att2.client.data.AttributeCosts110;
import fr.poubone.att2.client.data.AttributeEffects110;
import fr.poubone.att2.client.data.StatManager;
import fr.poubone.att2.client.data.StatUpgradeModel;
import fr.poubone.att2.client.util.ModLanguageManager;
import fr.poubone.att2.client.util.ModTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

public class StatUpgradeScreen extends Screen {
    private final List<StatUpgradeScreen.FlatButton> upgrades = new ArrayList<>();
    private final List<AttributePanelData110.Row> rows = new ArrayList<>();
    private StatUpgradeScreen.FlatButton closeButton;
    private AttributePanelLayout110 layout;
    private int offset;
    private int points;
    private int pendingVersion;
    private int pendingBase;
    private int pendingTicks;
    private String pendingKey;
    private String status = "";
    private boolean retried;
    private boolean prepared;
    private float uiScale = 1.0F;
    private float uiLeft;
    private float uiTop;

    public StatUpgradeScreen() {
        super(Component.literal(AttributePanelData110.t("title")));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.upgrades.clear();
        this.layout = AttributePanelLayout110.fit(this.width, this.height);
        if (!StatUpgradeModel.isListening()) {
            StatUpgradeModel.open();
        }

        for (String var2 : StatManager.STAT_KEYS) {
            StatUpgradeScreen.FlatButton var3 =
                    new StatUpgradeScreen.FlatButton(
                            0,
                            0,
                            this.layout.buttonW(),
                            24,
                            AttributePanelData110.t("add"),
                            var2x -> this.upgrade(var2));
            this.addRenderableWidget(var3);
            this.upgrades.add(var3);
        }

        this.closeButton =
                new StatUpgradeScreen.FlatButton(
                        0, 0, 90, 24, AttributePanelData110.t("close"), var1 -> this.onClose());
        this.addRenderableWidget(this.closeButton);
        this.prepareFrame();
    }

    private void upgrade(String var1) {
        int var2 = StatManager.STAT_KEYS.indexOf(var1);
        if (var2 >= this.offset
                && var2 < this.offset + this.layout.visible()
                && this.pendingKey == null
                && var2 < this.rows.size()) {
            AttributePanelData110.Row var3 = this.rows.get(var2);
            if (var3.ready(this.points)) {
                Integer var4 = Att2Triggers.STAT_UPGRADE.get(var1);
                if (var4 != null) {
                    this.pendingKey = var1;
                    this.pendingVersion = StatUpgradeModel.version();
                    this.pendingBase = var3.base().getAsInt();
                    this.pendingTicks = 0;
                    this.retried = false;
                    this.status = AttributePanelData110.t("waiting");
                    Att2Triggers.send(var4);
                    this.syncWidgets();
                }
            }
        }
    }

    private void updateRows() {
        StatManager.updateAll();
        this.points = StatUpgradeModel.skillPoints();
        this.rows.clear();

        for (String var2 : StatManager.STAT_KEYS) {
            this.rows.add(
                    AttributePanelData110.row(
                            var2, this.minecraft == null ? null : this.minecraft.player));
        }

        if (this.pendingKey != null
                && StatUpgradeModel.hasData()
                && StatUpgradeModel.version() != this.pendingVersion) {
            OptionalInt var3 = StatUpgradeModel.base(this.pendingKey);
            this.status =
                    AttributePanelData110.t(
                            var3.isPresent() && var3.getAsInt() > this.pendingBase
                                    ? "updated"
                                    : "unchanged");
            this.pendingKey = null;
            this.retried = false;
        }
    }

    private void syncWidgets() {
        if (this.layout != null) {
            this.offset = Math.max(0, Math.min(this.layout.maxOffset(), this.offset));

            for (int var1 = 0; var1 < this.upgrades.size(); var1++) {
                StatUpgradeScreen.FlatButton var2 = this.upgrades.get(var1);
                boolean var3 = var1 >= this.offset && var1 < this.offset + this.layout.visible();
                var2.visible = var3;
                if (!var3) {
                    var2.active = false;
                } else {
                    int var4 = this.layout.rowY(var1 - this.offset) + 6;
                    var2.setPosition(this.layout.buttonX(), var4);
                    AttributePanelData110.Row var5 = this.rows.get(var1);
                    var2.active = this.pendingKey == null && var5.ready(this.points);
                    String var6 =
                            var5.maxed()
                                    ? AttributePanelData110.t("maxed")
                                    : (var5.key().equals(this.pendingKey)
                                            ? AttributePanelData110.t("waiting_short")
                                            : (StatUpgradeModel.hasData()
                                                            && !var5.base().isEmpty()
                                                            && !var5.cost().isEmpty()
                                                    ? (var5.cost().getAsInt() > this.points
                                                            ? AttributePanelData110.t("not_enough")
                                                            : AttributePanelData110.t("add"))
                                                    : AttributePanelData110.t("reading_short")));
                    var2.setMessage(Component.literal(var6));
                }
            }

            if (this.closeButton != null) {
                this.closeButton.setPosition(
                        this.layout.buttonX(), this.layout.y() + this.layout.h() - 30);
            }
        }
    }

    @Override
    public void tick() {
        if (this.pendingKey != null && ++this.pendingTicks >= 80 && !this.retried) {
            this.retried = true;
            this.status = AttributePanelData110.t("retrying");
            Att2Triggers.send(2313);
        }
    }

    @Override
    public void onClose() {
        StatUpgradeModel.close();
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void removed() {
        StatUpgradeModel.close();
        super.removed();
    }

    private int localX(double var1) {
        return (int) Math.floor((var1 - this.uiLeft) / this.uiScale);
    }

    private int localY(double var1) {
        return (int) Math.floor((var1 - this.uiTop) / this.uiScale);
    }

    private MouseButtonEvent local(MouseButtonEvent var1) {
        return new MouseButtonEvent(
                (var1.x() - this.uiLeft) / this.uiScale,
                (var1.y() - this.uiTop) / this.uiScale,
                var1.buttonInfo());
    }

    private void pushView(GuiGraphics var1) {
        var1.pose().pushMatrix();
        var1.pose().translate(this.uiLeft, this.uiTop);
        var1.pose().scale(this.uiScale, this.uiScale);
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        if (!this.prepared) {
            this.prepareFrame();
        }

        this.prepared = false;
        int var5 = this.localX(var2);
        int var6 = this.localY(var3);
        this.pushView(var1);
        super.render(var1, var5, var6, var4);
        var1.pose().popMatrix();
        if (var5 >= this.layout.x() + 8
                && var5 < this.layout.x() + this.layout.w() - 7
                && var6 >= this.layout.top()
                && var6 < this.layout.bottom()) {
            int var7 = (var6 - this.layout.top()) / this.layout.rowH();
            if (var7 < this.rows.size()) {
                var1.setTooltipForNextFrame(
                        this.font,
                        AttributePanelData110.tooltip(this.rows.get(var7)),
                        Optional.empty(),
                        var2,
                        var3);
            }
        } else if (var5 >= this.layout.x()
                && var5 < this.layout.x() + this.layout.w()
                && var6 >= this.layout.y() + 20
                && var6 < this.layout.top()) {
            var1.setTooltipForNextFrame(
                    this.font,
                    List.of(Component.literal(AttributePanelData110.t("budget_tip"))),
                    Optional.empty(),
                    var2,
                    var3);
        }
    }

    private void prepareFrame() {
        this.layout = AttributePanelLayout110.fit(this.width, this.height);
        this.uiScale =
                Math.max(
                        0.01F,
                        Math.min(
                                (this.width - 10.0F) / this.layout.w(),
                                (this.height - 10.0F) / this.layout.h()));
        this.uiLeft =
                (this.width - this.layout.w() * this.uiScale) / 2.0F
                        - this.layout.x() * this.uiScale;
        this.uiTop =
                (this.height - this.layout.h() * this.uiScale) / 2.0F
                        - this.layout.y() * this.uiScale;
        this.layout = AttributePanelLayout110.fit(800, 430);
        this.offset = 0;
        this.updateRows();
        this.syncWidgets();
    }

    @Override
    public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
        this.prepareFrame();
        this.prepared = true;
        GuiBackdrop1132.fill(var1, 0, 0, this.width, this.height, -922746880, this);
        this.pushView(var1);
        this.drawPanel(var1, this.localX(var2), this.localY(var3));
        var1.pose().popMatrix();
    }

    private void drawPanel(GuiGraphics var1, int var2, int var3) {
        int var4 = this.layout.x();
        int var5 = this.layout.y();
        int var6 = this.layout.w();
        int var7 = this.layout.h();
        GuiBackdrop1132.fill(var1, var4, var5, var4 + var6, var5 + var7, -15921906, this);
        border(var1, var4, var5, var6, var7, -12961219);
        this.text(
                var1,
                AttributePanelData110.t("title"),
                var4 + 10,
                var5 + 8,
                -855310,
                var6 - 20,
                1.75F);
        int var8 = (var6 - 24) / 3;
        int var9 = var5 + 34;
        OptionalInt var10 = AttributeCosts110.totalSpent();
        String var11 =
                StatUpgradeModel.hasData()
                        ? String.valueOf(this.points)
                        : AttributePanelData110.t("unknown");
        String var12 =
                var10.isPresent()
                        ? String.valueOf(var10.getAsInt())
                        : AttributePanelData110.t("unknown");
        String var13 =
                var10.isPresent()
                        ? String.valueOf((long) var10.getAsInt() + this.points)
                        : AttributePanelData110.t("unknown");
        this.budget(
                var1,
                AttributePanelData110.t("available"),
                var11,
                var4 + 10,
                var9,
                var8,
                numberColor(this.points, -6560085));
        this.budget(
                var1,
                AttributePanelData110.t("spent"),
                var12,
                var4 + 10 + var8,
                var9,
                var8,
                -986896);
        this.budget(
                var1,
                AttributePanelData110.t("budget"),
                var13,
                var4 + 10 + var8 * 2,
                var9,
                var8,
                var10.isPresent()
                        ? numberColor((long) var10.getAsInt() + this.points, -986896)
                        : -986896);
        this.drawTableHeader(var1);

        for (int var14 = 0;
                var14 < this.layout.visible() && var14 + this.offset < this.rows.size();
                var14++) {
            this.drawRow(
                    var1, this.rows.get(var14 + this.offset), this.layout.rowY(var14), var2, var3);
        }

        int var16 = this.layout.y() + this.layout.h() - 36;
        GuiBackdrop1132.fill(var1, var4 + 8, var16, var4 + var6 - 8, var16 + 1, -13421770, this);
        String var15 =
                !StatUpgradeModel.hasData()
                        ? AttributePanelData110.t("reading")
                        : (this.status.isEmpty()
                                ? AttributePanelData110.t("footer_hint")
                                : this.status);
        this.text(var1, var15, var4 + 10, var16 + 13, -4732998, var6 - 110, 1.1F);
    }

    private void budget(
            GuiGraphics var1, String var2, String var3, int var4, int var5, int var6, int var7) {
        this.text(var1, var2, var4, var5, -5526094, var6 - 8, 1.25F);
        this.text(var1, var3, var4, var5 + 16, var7, var6 - 8, 1.65F);
    }

    private void drawTableHeader(GuiGraphics var1) {
        int var2 = this.layout.top() - 28;
        int var3 = this.layout.x() + 8;
        int var4 = this.layout.w() - 16;
        GuiBackdrop1132.fill(var1, var3, var2, var3 + var4, var2 + 28, -15263975, this);
        GuiBackdrop1132.fill(var1, var3, var2, var3 + var4, var2 + 1, -12961217, this);
        GuiBackdrop1132.fill(var1, var3, var2 + 27, var3 + var4, var2 + 28, -10263701, this);
        String[] var5 =
                new String[] {
                    AttributePanelData110.t("column.attribute"),
                    AttributePanelData110.t("level"),
                    AttributePanelData110.t("total"),
                    AttributePanelData110.t("current"),
                    AttributePanelData110.t("next"),
                    AttributePanelData110.t("gain"),
                    AttributePanelData110.t("column.cost"),
                    AttributePanelData110.t("column.action")
                };
        int[] var6 =
                new int[] {
                    this.layout.nameX(),
                    this.layout.levelX(),
                    this.layout.totalX(),
                    this.layout.effectX(),
                    this.layout.nextX(),
                    this.layout.gainX(),
                    this.layout.costX(),
                    this.layout.buttonX()
                };
        int[] var7 =
                new int[] {
                    this.layout.nameW(),
                    this.layout.levelW(),
                    this.layout.totalW(),
                    this.layout.effectW(),
                    this.layout.effectW(),
                    this.layout.effectW(),
                    this.layout.costW(),
                    this.layout.buttonW()
                };

        for (int var8 = 0; var8 < var5.length; var8++) {
            this.text(var1, var5[var8], var6[var8], var2 + 8, -5262920, var7[var8] - 4, 1.3F);
        }
    }

    private void drawRow(
            GuiGraphics var1, AttributePanelData110.Row var2, int var3, int var4, int var5) {
        int var6 = this.layout.x() + 8;
        int var7 = this.layout.w() - 16;
        if (var4 >= var6
                && var4 < var6 + var7
                && var5 >= var3
                && var5 < var3 + this.layout.rowH()) {
            GuiBackdrop1132.fill(
                    var1, var6, var3, var6 + var7, var3 + this.layout.rowH(), -15198181, this);
        }

        GuiBackdrop1132.fill(
                var1,
                var6,
                var3 + this.layout.rowH() - 1,
                var6 + var7,
                var3 + this.layout.rowH(),
                -13487563,
                this);
        int var8 = this.layout.nameX();
        int var9 = var3 + 8;
        int var10 = var3 + 11;
        if (var2.key().equals("DAR")) {
            var1.pose().pushMatrix();
            var1.pose().translate(var8, var9);
            var1.pose().scale(1.25F, 1.25F);
            var1.renderItem(Items.BOOK.getDefaultInstance(), 0, 0);
            var1.pose().popMatrix();
        } else {
            Identifier var11 = ModTextures.STAT_ICONS.get(var2.key());
            if (var11 != null) {
                var1.blit(
                        RenderPipelines.GUI_TEXTURED,
                        var11,
                        var8,
                        var9,
                        0.0F,
                        0.0F,
                        20,
                        20,
                        16,
                        16,
                        16,
                        16);
            }
        }

        this.text(
                var1,
                ModLanguageManager.getString("stat.label." + var2.key().toLowerCase(Locale.ROOT)),
                var8 + 26,
                var10,
                -986896,
                this.layout.nameW() - 30);
        String var13 =
                var2.base().isPresent()
                        ? String.valueOf(var2.base().getAsInt())
                        : AttributePanelData110.t("unknown");
        this.text(
                var1,
                var13 + " / " + var2.cap(),
                this.layout.levelX(),
                var10,
                var2.base().isPresent() ? numberColor(var2.base().getAsInt(), -3354923) : -3354923,
                this.layout.levelW() - 4);
        this.text(
                var1,
                var2.total().isPresent()
                        ? String.valueOf(var2.total().getAsInt())
                        : AttributePanelData110.t("unknown"),
                this.layout.totalX(),
                var10,
                var2.total().isPresent()
                        ? numberColor(var2.total().getAsInt(), -1250066)
                        : -1250066,
                this.layout.totalW() - 4);
        this.text(
                var1,
                AttributePanelData110.format(var2.current(), false),
                this.layout.effectX(),
                var10,
                valueColor(var2.current(), -921103),
                this.layout.effectW() - 4);
        this.text(
                var1,
                var2.maxed()
                        ? AttributePanelData110.t("maxed")
                        : AttributePanelData110.format(var2.next(), false),
                this.layout.nextX(),
                var10,
                var2.maxed() ? -5328713 : valueColor(var2.next(), -1776152),
                this.layout.effectW() - 4);
        this.text(
                var1,
                var2.maxed() ? "—" : AttributePanelData110.format(var2.delta(), true),
                this.layout.gainX(),
                var10,
                var2.maxed() ? -5328713 : valueColor(var2.delta(), -6560085),
                this.layout.effectW() - 4);
        String var12 =
                var2.maxed()
                        ? "—"
                        : (var2.cost().isPresent()
                                ? String.valueOf(var2.cost().getAsInt())
                                : AttributePanelData110.t("unknown"));
        this.text(
                var1,
                var12,
                this.layout.costX(),
                var10,
                !var2.maxed() && var2.cost().isPresent() && var2.cost().getAsInt() < 0
                        ? -171
                        : (var2.cost().isPresent() && var2.cost().getAsInt() > this.points
                                ? -1919324
                                : (var2.maxed() ? -5328713 : -1118480)),
                this.layout.costW() - 4);
    }

    private static int numberColor(double var0, int var2) {
        return var0 < 0.0 ? -171 : var2;
    }

    private static int valueColor(AttributeEffects110.Value var0, int var1) {
        return var0.known() ? numberColor(var0.amount(), var1) : var1;
    }

    private void text(GuiGraphics var1, String var2, int var3, int var4, int var5, int var6) {
        this.text(var1, var2, var3, var4, var5, var6, 1.5F);
    }

    private void text(
            GuiGraphics var1, String var2, int var3, int var4, int var5, int var6, float var7) {
        if (var6 > 0 && var2 != null && !var2.isEmpty()) {
            int var8 = this.font.width(var2);
            float var9 = Math.min(var7, (float) var6 / Math.max(1, var8));
            var1.pose().pushMatrix();
            var1.pose().translate(var3, var4);
            var1.pose().scale(var9, var9);
            var1.drawString(this.font, var2, 0, 0, var5, false);
            var1.pose().popMatrix();
        }
    }

    private static void border(GuiGraphics var0, int var1, int var2, int var3, int var4, int var5) {
        var0.fill(var1, var2, var1 + var3, var2 + 1, var5);
        var0.fill(var1, var2 + var4 - 1, var1 + var3, var2 + var4, var5);
        var0.fill(var1, var2, var1 + 1, var2 + var4, var5);
        var0.fill(var1 + var3 - 1, var2, var1 + var3, var2 + var4, var5);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
        return super.mouseClicked(this.local(var1), var2);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
        return super.mouseDragged(this.local(var1), var2 / this.uiScale, var4 / this.uiScale);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent var1) {
        return super.mouseReleased(this.local(var1));
    }

    @Override
    public void mouseMoved(double var1, double var3) {
        super.mouseMoved((var1 - this.uiLeft) / this.uiScale, (var3 - this.uiTop) / this.uiScale);
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent var1) {
        if (var1.key() == 256) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(var1);
        }
    }

    private static final class FlatButton extends Button {
        FlatButton(int var1, int var2, int var3, int var4, String var5, OnPress var6) {
            super(var1, var2, var3, var4, Component.literal(var5), var6, DEFAULT_NARRATION);
        }

        @Override
        protected void renderContents(GuiGraphics var1, int var2, int var3, float var4) {
            int var5 = this.getX();
            int var6 = this.getY();
            int var7 = this.width;
            int var8 = this.height;
            boolean var9 = this.active && (this.isHoveredOrFocused() || this.isFocused());
            var1.fill(
                    var5,
                    var6,
                    var5 + var7,
                    var6 + var8,
                    this.active ? (var9 ? -12237496 : -13619149) : -14671837);
            StatUpgradeScreen.border(
                    var1,
                    var5,
                    var6,
                    var7,
                    var8,
                    this.active ? (var9 ? -5921366 : -10329497) : -13027011);
            Font var10 = Minecraft.getInstance().font;
            String var11 = this.getMessage().getString();
            int var12 = var10.width(var11);
            float var13 = Math.min(1.4F, (float) (var7 - 8) / Math.max(1, var12));
            var1.pose().pushMatrix();
            var1.pose()
                    .translate(
                            var5 + (var7 - var12 * var13) / 2.0F,
                            var6 + (var8 - 9.0F * var13) / 2.0F);
            var1.pose().scale(var13, var13);
            var1.drawString(var10, var11, 0, 0, this.active ? -1118481 : -6184281, false);
            var1.pose().popMatrix();
        }
    }
}
