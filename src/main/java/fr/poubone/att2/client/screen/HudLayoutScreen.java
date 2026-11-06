package fr.poubone.att2.client.screen;

import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.hud.HudLayout;
import fr.poubone.att2.client.hud.HudRenderer;
import fr.poubone.att2.client.hud.HudSlot;
import fr.poubone.att2.client.hud.ModToast;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Locale;

public class HudLayoutScreen extends Screen {
    private static final int HANDLE = 8;
    private static final int MIN_W = 32;
    private static final int MIN_H = 16;
    private static final int SNAP = 2;
    private final Screen parent;
    private String selected = "xp";
    private String dragging;
    private boolean resizing;
    private int grabDx;
    private int grabDy;
    private int startW;
    private int startH;
    private Button statsLayoutButton;

    public HudLayoutScreen(Screen var1) {
        super(ModLanguageManager.get("screen.hud_layout.title"));
        this.parent = var1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        byte var1 = 110;
        this.addRenderableWidget(
                Button.builder(
                                ModLanguageManager.get("screen.hud_layout.reset"),
                                var0 -> {
                                    HudLayout.resetAll();
                                    HUDConfig.save();
                                })
                        .bounds(8, this.height - 28, var1, 20)
                        .build());
        this.addRenderableWidget(
                Button.builder(
                                ModLanguageManager.get("screen.hud_layout.done"),
                                var1x -> this.done())
                        .bounds(this.width - var1 - 8, this.height - 28, var1, 20)
                        .build());
        this.statsLayoutButton =
                Button.builder(
                                this.statsLayoutLabel(),
                                var1x -> {
                                    HUDConfig var2 = HUDConfig.get();
                                    var2.statsLayoutMode = (var2.statsLayoutMode + 1) % 3;
                                    var1x.setMessage(this.statsLayoutLabel());
                                    HUDConfig.save();
                                })
                        .bounds(
                                Math.max(8, this.width / 2 - 88),
                                this.height - 52,
                                Math.min(176, Math.max(20, this.width - 16)),
                                20)
                        .build();
        this.addRenderableWidget(this.statsLayoutButton);
        this.updateStatsLayoutButton();
    }

    private void done() {
        HUDConfig.save();
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
        GuiBackdrop1132.fill(var1, 0, 0, this.width, 22, -1442840576, this);
        GuiBackdrop1132.fill(var1, 0, this.height - 32, this.width, this.height, -1442840576, this);
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        HudRenderer.renderWidgets(var1, true);
        ModToast.renderCityPreview(var1);

        for (String var8 : HudLayout.IDS) {
            this.drawBox(var1, var8, var8.equals(this.selected));
        }

        super.render(var1, var2, var3, var4);
        var1.drawCenteredString(
                this.font,
                ModLanguageManager.get("screen.hud_layout.hint"),
                this.width / 2,
                7,
                -1518160);
        HudLayout.Box var10 = HudLayout.box(this.selected);
        HudSlot var11 = HudLayout.slot(this.selected);
        float var12 = var11.scale;
        if (var12 <= 0.0F) {
            var12 = 1.0F;
        }

        String var13 =
                var10.w() + " x " + var10.h() + " · ×" + String.format(Locale.ROOT, "%.1f", var12);
        if ("stats".equals(this.selected)) {
            float var9 = var11.gap <= 0.0F ? 1.0F : var11.gap;
            var13 =
                    var13
                            + " · "
                            + ModLanguageManager.getString("screen.hud_layout.gap")
                            + " ×"
                            + String.format(Locale.ROOT, "%.1f", var9);
        }

        var1.drawCenteredString(this.font, var13, this.width / 2, this.height - 22, -3622752);
    }

    private void drawBox(GuiGraphics var1, String var2, boolean var3) {
        HudLayout.Box var4 = HudLayout.box(var2);
        int var5 = var3 ? -1521558 : -2000111456;
        int var6 = var3 ? 870893674 : 570425344;
        GuiBackdrop1132.fill(
                var1, var4.x(), var4.y(), var4.x() + var4.w(), var4.y() + var4.h(), var6, this);
        drawBorder(var1, var4.x(), var4.y(), var4.w(), var4.h(), var5);
        GuiBackdrop1132.fill(
                var1,
                var4.x() + var4.w() - 8,
                var4.y() + var4.h() - 8,
                var4.x() + var4.w(),
                var4.y() + var4.h(),
                var5,
                this);
        String var7 = ModLanguageManager.getString(HudLayout.labelKey(var2));
        int var8 = this.font.width(var7);
        int var9 = var4.x() + Math.max(2, (var4.w() - var8) / 2);
        int var10 = Math.max(var4.y() - 10, 24);
        var1.drawString(this.font, var7, var9, var10, var5, false);
    }

    private static void drawBorder(
            GuiGraphics var0, int var1, int var2, int var3, int var4, int var5) {
        var0.fill(var1, var2, var1 + var3, var2 + 1, var5);
        var0.fill(var1, var2 + var4 - 1, var1 + var3, var2 + var4, var5);
        var0.fill(var1, var2, var1 + 1, var2 + var4, var5);
        var0.fill(var1 + var3 - 1, var2, var1 + var3, var2 + var4, var5);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
        if (super.mouseClicked(var1, var2)) {
            return true;
        }

        if (var1.button() != 0) {
            return false;
        }

        int var3 = (int) var1.x();
        int var4 = (int) var1.y();

        for (int var5 = HudLayout.IDS.length - 1; var5 >= 0; var5--) {
            String var6 = HudLayout.IDS[var5];
            HudLayout.Box var7 = HudLayout.box(var6);
            if (contains(var7, var3, var4)) {
                this.selected = var6;
                this.updateStatsLayoutButton();
                if (var3 >= var7.x() + var7.w() - 8 && var4 >= var7.y() + var7.h() - 8) {
                    this.resizing = true;
                    this.dragging = var6;
                    this.grabDx = var3;
                    this.grabDy = var4;
                    this.startW = var7.w();
                    this.startH = var7.h();
                } else {
                    this.resizing = false;
                    this.dragging = var6;
                    this.grabDx = var3 - var7.x();
                    this.grabDy = var4 - var7.y();
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
        if (this.dragging == null) {
            return super.mouseDragged(var1, var2, var4);
        }

        int var6 = (int) var1.x();
        int var7 = (int) var1.y();
        HudLayout.Box var8 = HudLayout.box(this.dragging);
        if (this.resizing) {
            int var9 =
                    snap(Mth.clamp(this.startW + (var6 - this.grabDx), 32, this.width - var8.x()));
            int var10 =
                    snap(Mth.clamp(this.startH + (var7 - this.grabDy), 16, this.height - var8.y()));
            HudLayout.writePixels(this.dragging, var8.x(), var8.y(), var9, var10);
        } else {
            int var13 = this.width - var8.w();
            int var14 = this.height - var8.h();
            int var11 = HudLayout.snapToEdge(Mth.clamp(var6 - this.grabDx, 0, var13), 0, var13, 2);
            int var12 = HudLayout.snapToEdge(Mth.clamp(var7 - this.grabDy, 0, var14), 0, var14, 2);
            HudLayout.writePixels(this.dragging, var11, var12, var8.w(), var8.h());
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        int var9 = (int) var1;
        int var10 = (int) var3;
        String var11 = this.selected;
        HudLayout.Box var12 = HudLayout.box(var11);
        if (!contains(var12, var9, var10)) {
            var11 = null;

            for (int var13 = HudLayout.IDS.length - 1; var13 >= 0; var13--) {
                String var14 = HudLayout.IDS[var13];
                if (contains(HudLayout.box(var14), var9, var10)) {
                    var11 = var14;
                    this.selected = var14;
                    break;
                }
            }

            if (var11 == null) {
                return super.mouseScrolled(var1, var3, var5, var7);
            }
        }

        if (var7 == 0.0) {
            return super.mouseScrolled(var1, var3, var5, var7);
        }

        if ("stats".equals(var11) && Minecraft.getInstance().hasShiftDown()) {
            float var16 = var7 > 0.0 ? 0.1F : -0.1F;
            HudLayout.adjustGap(var11, var16);
        } else {
            float var15 = var7 > 0.0 ? 0.1F : -0.1F;
            HudLayout.adjustScale(var11, var15);
        }

        HUDConfig.save();
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent var1) {
        if (this.dragging != null) {
            this.dragging = null;
            this.resizing = false;
            HUDConfig.save();
            return true;
        } else {
            return super.mouseReleased(var1);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent var1) {
        if (var1.key() == 256) {
            this.done();
            return true;
        }

        int var2 = var1.hasShiftDown() ? 8 : 2;
        HudLayout.Box var3 = HudLayout.box(this.selected);
        int var4 = var3.x();
        int var5 = var3.y();
        int var6 = var3.w();
        int var7 = var3.h();

        boolean var8 =
                switch (var1.key()) {
                    case 262 -> {
                        var4 = Mth.clamp(var4 + var2, 0, this.width - var6);
                        yield true;
                    }
                    case 263 -> {
                        var4 = Mth.clamp(var4 - var2, 0, this.width - var6);
                        yield true;
                    }
                    case 264 -> {
                        var5 = Mth.clamp(var5 + var2, 0, this.height - var7);
                        yield true;
                    }
                    case 265 -> {
                        var5 = Mth.clamp(var5 - var2, 0, this.height - var7);
                        yield true;
                    }
                    default -> false;
                };
        if (var8) {
            HudLayout.writePixels(this.selected, var4, var5, var6, var7);
            HUDConfig.save();
            return true;
        } else {
            return super.keyPressed(var1);
        }
    }

    private static int snap(int var0) {
        return var0 / 2 * 2;
    }

    private Component statsLayoutLabel() {
        return ModLanguageManager.get(
                "screen.hud_config.stats.layout."
                        + switch (HUDConfig.get().statsLayoutMode) {
                            case 1 -> "row";
                            case 2 -> "column";
                            default -> "auto";
                        });
    }

    private void updateStatsLayoutButton() {
        if (this.statsLayoutButton != null) {
            this.statsLayoutButton.visible = "stats".equals(this.selected);
            this.statsLayoutButton.active = this.statsLayoutButton.visible;
            this.statsLayoutButton.setMessage(this.statsLayoutLabel());
        }
    }

    private static boolean contains(HudLayout.Box var0, int var1, int var2) {
        return var1 >= var0.x()
                && var1 <= var0.x() + var0.w()
                && var2 >= var0.y()
                && var2 <= var0.y() + var0.h();
    }
}
