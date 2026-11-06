package fr.poubone.att2.client.screen;

import fr.poubone.att2.client.data.SpellLauncherTracker;
import fr.poubone.att2.client.spell.SpellWheelCatalog;
import fr.poubone.att2.client.spell.SpellWheelInput;
import fr.poubone.att2.client.spell.SpellWheelLayout;
import fr.poubone.att2.client.spell.SpellWheelLevelColors1111;
import fr.poubone.att2.client.spell.SpellWheelMovement;
import fr.poubone.att2.client.spell.SpellWheelOrder;
import fr.poubone.att2.client.spell.SpellWheelSwap;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Map;

public final class SpellWheelScreen extends Screen {
    private final Minecraft mc;
    private final SpellWheelMovement movement;
    private static final FontDescription TEXT_FONT =
            new Resource(Identifier.fromNamespaceAndPath("att2", "journal_search"));
    private final SpellWheelOrder order;
    private final int openedHotbar;
    private final int openedSpellId;
    private final int openedHandSlot;
    private List<Integer> positions;
    private Map<Integer, SpellWheelCatalog.Owned> owned = Map.of();
    private SpellWheelLayout layout;
    private float uiScale = 1.0F;
    private int virtualWidth;
    private int virtualHeight;
    private double mouseX;
    private double mouseY;
    private int dragSource = -1;
    private boolean edited;
    private boolean finished;
    private boolean saveFailed;

    public SpellWheelScreen(Minecraft var1) {
        super(ModLanguageManager.get("spell_wheel.title"));
        this.mc = var1;
        this.movement = new SpellWheelMovement(var1);
        this.order =
                new SpellWheelOrder(
                        FabricLoader.getInstance().getConfigDir().resolve("att2_spell_wheel.json"));
        this.positions = this.order.ids();
        this.openedHotbar = var1.player.getInventory().getSelectedSlot();
        this.openedHandSlot = SpellWheelSwap.sourceSlot(var1);
        SpellLauncherTracker.LauncherState var2 = SpellWheelSwap.sourceSpell(var1);
        this.openedSpellId = var2 == null ? 0 : var2.spellId;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.uiScale =
                Math.max(0.1F, Math.min(1.0F, Math.min(this.width / 320.0F, this.height / 240.0F)));
        this.virtualWidth = Math.round(this.width / this.uiScale);
        this.virtualHeight = Math.round(this.height / this.uiScale);
        this.layout =
                new SpellWheelLayout(this.virtualWidth, this.virtualHeight, this.positions.size());
        this.mouseX = this.layout.cx;
        this.mouseY = this.layout.cy;
        this.dragSource = -1;
        this.refresh();
        this.movement.start();
    }

    private void refresh() {
        if (this.mc.player != null) {
            this.owned =
                    SpellWheelCatalog.scan(
                            this.mc.player.getInventory(),
                            this.mc.player.getOffhandItem(),
                            this.openedHandSlot);
        } else {
            this.owned = Map.of();
        }
    }

    @Override
    public void tick() {
        if (!this.finished) {
            if (SpellWheelSwap.canOpen(this.mc)
                    && this.mc.player.getInventory().getSelectedSlot() == this.openedHotbar) {
                SpellLauncherTracker.LauncherState var1 = SpellWheelSwap.sourceSpell(this.mc);
                if (var1 != null
                        && var1.spellId == this.openedSpellId
                        && SpellWheelSwap.sourceSlot(this.mc) == this.openedHandSlot) {
                    this.movement.update();
                    this.refresh();
                    if (!SpellWheelInput.held(this.mc)) {
                        this.finish();
                    }
                } else {
                    this.onClose();
                }
            } else {
                this.onClose();
            }
        }
    }

    private void finish() {
        int var1 = this.layout == null ? -1 : this.layout.hover(this.mouseX, this.mouseY);
        boolean var2 =
                !this.edited
                        && this.dragSource < 0
                        && var1 >= 0
                        && this.owned.containsKey(this.positions.get(var1));
        int var3 = var2 ? this.positions.get(var1) : -1;
        this.finished = true;
        this.dragSource = -1;
        this.movement.close(this.mc.isWindowActive());
        this.mc.setScreen(null);
        if (var2) {
            SpellWheelSwap.Result var4 =
                    SpellWheelSwap.choose(
                            this.mc,
                            var3,
                            this.openedHotbar,
                            this.openedSpellId,
                            this.openedHandSlot);
            if (var4 == SpellWheelSwap.Result.UNAVAILABLE
                    || var4 == SpellWheelSwap.Result.CONTEXT_CHANGED) {
                this.mc.player.displayClientMessage(
                        ModLanguageManager.get("spell_wheel.changed"), true);
            }
        }
    }

    @Override
    public void onClose() {
        this.finished = true;
        this.dragSource = -1;
        this.movement.close(this.mc.isWindowActive());
        this.mc.setScreen(null);
    }

    @Override
    public void removed() {
        this.finished = true;
        this.dragSource = -1;
        this.movement.close(false);
        super.removed();
    }

    @Override
    public boolean isMouseOver(double var1, double var3) {
        this.mouseX = var1 / this.uiScale;
        this.mouseY = var3 / this.uiScale;
        return super.isMouseOver(var1, var3);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
        this.mouseX = var1.x() / this.uiScale;
        this.mouseY = var1.y() / this.uiScale;
        if (var1.button() == 1 && this.layout != null) {
            int var3 = this.layout.exactHit(this.mouseX, this.mouseY);
            if (var3 >= 0) {
                this.dragSource = var3;
                this.edited = true;
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
        this.mouseX = var1.x() / this.uiScale;
        this.mouseY = var1.y() / this.uiScale;
        return this.dragSource >= 0 || super.mouseDragged(var1, var2, var4);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent var1) {
        this.mouseX = var1.x() / this.uiScale;
        this.mouseY = var1.y() / this.uiScale;
        if (var1.button() == 1 && this.dragSource >= 0) {
            int var2 = this.layout.exactHit(this.mouseX, this.mouseY);
            SpellWheelOrder.Result var3 = this.order.swap(this.dragSource, var2);
            this.saveFailed = var3 == SpellWheelOrder.Result.FAILED;
            this.positions = this.order.ids();
            this.dragSource = -1;
            return true;
        } else {
            return true;
        }
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
        GuiBackdrop1132.fill(var1, 0, 0, this.width, this.height, -1524951277, this);
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        if (this.layout == null) {
            this.init();
        }

        this.renderBackground(var1, var2, var3, var4);
        this.mouseX = var2 / this.uiScale;
        this.mouseY = var3 / this.uiScale;
        var2 = (int) Math.round(this.mouseX);
        var3 = (int) Math.round(this.mouseY);
        var1.pose().pushMatrix();
        var1.pose().scale(this.uiScale, this.uiScale);
        this.ring(var1, this.layout.innerRadius, -1720886473);
        this.ring(var1, this.layout.outerRadius, -1432715184);
        int var5 =
                this.dragSource >= 0
                        ? this.layout.exactHit(var2, var3)
                        : this.layout.hover(var2, var3);
        int var6 = this.layout.nodeSize;

        for (int var7 = 0; var7 < this.positions.size(); var7++) {
            int var8 = this.positions.get(var7);
            SpellWheelLayout.Point var9 = this.layout.point(var7);
            boolean var10 = this.owned.containsKey(var8);
            boolean var11 = var7 == var5;
            int var12 = SpellWheelLevelColors1111.borderColor(this.owned.get(var8), var11);
            plate(
                    var1,
                    var9.x() - var6 / 2,
                    var9.y() - var6 / 2,
                    var6,
                    var6,
                    var11 ? -379236300 : -803004133,
                    var12);
            this.icon(var1, var8, var9.x(), var9.y(), var6 - 4);
            String var13 =
                    var10
                            ? (this.owned.get(var8).state().level > 0
                                    ? Integer.toString(this.owned.get(var8).state().level)
                                    : "—")
                            : "—";
            float var14 = Math.min(0.85F, (var6 - 2.0F) / Math.max(1, this.font.width(var13) + 2));
            int var15 = Math.round(this.font.width(var13) * var14);
            int var16 = var9.x() + var6 / 2 - var15;
            int var17 = var9.y() + var6 / 2 - 8;
            GuiBackdrop1132.fill(
                    var1,
                    var16 - 1,
                    var17 - 1,
                    var9.x() + var6 / 2 + 1,
                    var17 + 8,
                    -854256881,
                    this);
            var1.pose().pushMatrix();
            var1.pose().translate(var16, var17);
            var1.pose().scale(var14, var14);
            var1.drawString(this.font, var13, 0, 0, var10 ? -9341 : -7698305, true);
            var1.pose().popMatrix();
            if (var7 == this.dragSource) {
                GuiBackdrop1132.fill(
                        var1,
                        var9.x() - var6 / 2,
                        var9.y() + var6 / 2 - 2,
                        var9.x() + var6 / 2,
                        var9.y() + var6 / 2,
                        -3495324,
                        this);
            }
        }

        int var20 = this.layout.centerWidth();
        plate(
                var1,
                this.layout.cx - var20 / 2,
                this.layout.cy - 27,
                var20,
                54,
                -220279385,
                -4153238);
        if (var5 >= 0) {
            int var21 = this.positions.get(var5);
            SpellWheelCatalog.Owned var24 = this.owned.get(var21);
            int var25 = this.layout.cy - 21;
            Component var26 = plain(SpellWheelCatalog.name(var21));
            float var27 = 1.0F;

            while (var27 > 0.6F && this.font.split(var26, (int) ((var20 - 8) / var27)).size() > 2) {
                var27 -= 0.05F;
            }

            for (FormattedCharSequence var30 :
                    this.font.split(var26, (int) ((var20 - 8) / var27)).stream()
                            .limit(2L)
                            .toList()) {
                var1.pose().pushMatrix();
                var1.pose().translate(this.layout.cx, var25);
                var1.pose().scale(var27, var27);
                var1.drawString(this.font, var30, -this.font.width(var30) / 2, 0, -13621477, false);
                var1.pose().popMatrix();
                var25 += Math.round(10.0F * var27);
            }

            String var29 =
                    var24 == null
                            ? "—"
                            : (var24.state().level > 0
                                    ? Integer.toString(var24.state().level)
                                    : "—");
            this.centerText(
                    var1,
                    Component.literal(ModLanguageManager.format("spell_wheel.level", "n", var29)),
                    this.layout.cx,
                    this.layout.cy + 2,
                    var20 - 8,
                    -11189979);
            String var31 =
                    var24 == null
                            ? "spell_wheel.missing"
                            : (var24.state().ready()
                                    ? "spell_wheel.owned"
                                    : "spell_wheel.cooldown");
            this.centerText(
                    var1,
                    ModLanguageManager.get(var31),
                    this.layout.cx,
                    this.layout.cy + 15,
                    var20 - 8,
                    var24 == null ? -9409696 : -12625609);
        } else {
            this.centerText(
                    var1,
                    ModLanguageManager.get("spell_wheel.title"),
                    this.layout.cx,
                    this.layout.cy - 19,
                    var20 - 8,
                    -11978465);
            this.centerText(
                    var1,
                    ModLanguageManager.get("spell_wheel.point"),
                    this.layout.cx,
                    this.layout.cy - 2,
                    var20 - 8,
                    -10069955);
            this.centerText(
                    var1,
                    ModLanguageManager.get("spell_wheel.cancel"),
                    this.layout.cx,
                    this.layout.cy + 14,
                    var20 - 8,
                    -8556459);
        }

        if (this.dragSource >= 0) {
            int var22 = this.positions.get(this.dragSource);
            plate(
                    var1,
                    var2 - 13,
                    var3 - 13,
                    26,
                    26,
                    -400547815,
                    SpellWheelLevelColors1111.borderColor(this.owned.get(var22), true));
            this.icon(var1, var22, var2, var3, 22);
        }

        String var23 =
                this.saveFailed
                        ? "spell_wheel.save_failed"
                        : (this.edited ? "spell_wheel.edit_done" : "spell_wheel.hint");
        this.centerText(
                var1,
                ModLanguageManager.get(var23),
                this.virtualWidth / 2,
                this.virtualHeight - 23,
                this.virtualWidth - 20,
                this.saveFailed ? -34953 : -2043997);
        this.centerText(
                var1,
                ModLanguageManager.get("spell_wheel.drag_hint"),
                this.virtualWidth / 2,
                this.virtualHeight - 11,
                this.virtualWidth - 20,
                -5661826);
        var1.pose().popMatrix();
    }

    private void icon(GuiGraphics var1, int var2, int var3, int var4, int var5) {
        int var6 = var3 - var5 / 2;
        int var7 = var4 - var5 / 2;
        SpellWheelCatalog.Owned var8 = this.owned.get(var2);
        if (var8 == null) {
            Identifier var9 =
                    Identifier.fromNamespaceAndPath(
                            "att2", "textures/gui/spell_wheel/gray/" + var2 + ".png");
            var1.pose().pushMatrix();
            var1.pose().translate(var6, var7);
            var1.pose().scale(var5 / 16.0F, var5 / 16.0F);
            var1.blit(RenderPipelines.GUI_TEXTURED, var9, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
            var1.pose().popMatrix();
        } else {
            var1.pose().pushMatrix();
            var1.pose().translate(var6, var7);
            var1.pose().scale(var5 / 16.0F, var5 / 16.0F);
            var1.renderItem(var8.state().stack, 0, 0);
            var1.pose().popMatrix();
            if (!var8.state().ready()) {
                int var10 = var5 * var8.state().cooldownTenths / 10;
                GuiBackdrop1132.fill(
                        var1,
                        var6,
                        var7 + var5 - var10,
                        var6 + var5,
                        var7 + var5,
                        -1509949440,
                        this);
            }
        }
    }

    private static Component plain(Component var0) {
        return var0.copy().withStyle(var0x -> var0x.withFont(TEXT_FONT));
    }

    private void centerText(
            GuiGraphics var1, Component var2, int var3, int var4, int var5, int var6) {
        var2 = plain(var2);
        float var7 = Math.min(1.0F, (float) var5 / Math.max(1, this.font.width(var2)));
        var1.pose().pushMatrix();
        var1.pose().translate(var3, var4);
        var1.pose().scale(var7, var7);
        var1.drawString(this.font, var2, -this.font.width(var2) / 2, 0, var6, false);
        var1.pose().popMatrix();
    }

    private void ring(GuiGraphics var1, int var2, int var3) {
        for (int var4 = 0; var4 < 120; var4++) {
            double var5 = (Math.PI * 2) * var4 / 120.0;
            int var7 = this.layout.cx + (int) Math.round(Math.cos(var5) * var2);
            int var8 = this.layout.cy + (int) Math.round(Math.sin(var5) * var2);
            GuiBackdrop1132.fill(var1, var7, var8, var7 + 1, var8 + 1, var3, this);
        }
    }

    private static void plate(
            GuiGraphics var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        var0.fill(var1, var2, var1 + var3, var2 + var4, var5);
        var0.fill(var1 + 2, var2, var1 + var3 - 2, var2 + 1, var6);
        var0.fill(var1 + 2, var2 + var4 - 1, var1 + var3 - 2, var2 + var4, var6);
        var0.fill(var1, var2 + 2, var1 + 1, var2 + var4 - 2, var6);
        var0.fill(var1 + var3 - 1, var2 + 2, var1 + var3, var2 + var4 - 2, var6);
    }
}
