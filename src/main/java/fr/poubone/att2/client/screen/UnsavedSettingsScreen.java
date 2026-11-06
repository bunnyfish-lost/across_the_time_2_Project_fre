package fr.poubone.att2.client.screen;

import fr.poubone.att2.client.discord.DiscordPresence;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;

public final class UnsavedSettingsScreen extends Screen {
    private final HUDConfigScreen parent;
    private final SettingsSaveState state;
    private boolean failed;

    public UnsavedSettingsScreen(HUDConfigScreen var1, SettingsSaveState var2) {
        this(var1, var2, false);
    }

    public UnsavedSettingsScreen(HUDConfigScreen var1, SettingsSaveState var2, boolean var3) {
        super(ModLanguageManager.get("settings.unsaved.title"));
        this.parent = var1;
        this.state = var2;
        this.failed = var3;
    }

    @Override
    public boolean isPauseScreen() {
        return this.parent.isPauseScreen();
    }

    @Override
    protected void init() {
        int var1 = Math.min(220, this.width - 40);
        int var2 = (this.width - var1) / 2;
        int var3 = this.height / 2 + 8;
        this.addRenderableWidget(
                Button.builder(
                                ModLanguageManager.get("settings.unsaved.save"),
                                var1x -> {
                                    if (this.state.save()) {
                                        DiscordPresence.onConfigChanged();
                                        this.parent.exitToParent();
                                    } else {
                                        this.failed = true;
                                    }
                                })
                        .bounds(var2, var3, var1, 20)
                        .build());
        this.addRenderableWidget(
                Button.builder(
                                ModLanguageManager.get("settings.unsaved.discard"),
                                var1x -> this.parent.exitToParent())
                        .bounds(var2, var3 + 25, var1, 20)
                        .build());
        this.addRenderableWidget(
                Button.builder(
                                ModLanguageManager.get("settings.unsaved.back"),
                                var1x -> this.onClose())
                        .bounds(var2, var3 + 50, var1, 20)
                        .build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void renderBackground(GuiGraphics var1, int var2, int var3, float var4) {
        GuiBackdrop1132.fill(var1, 0, 0, this.width, this.height, -585690352, this);
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        super.render(var1, var2, var3, var4);
        int var5 = Math.min(360, this.width - 40);
        int var6 = Math.max(20, this.height / 2 - 65);
        var1.drawCenteredString(this.font, this.title, this.width / 2, var6, -926294);
        int var7 = var6 + 20;

        for (FormattedCharSequence var9 :
                this.font.split(
                        ModLanguageManager.get(
                                this.failed
                                        ? "settings.unsaved.failed"
                                        : "settings.unsaved.message"),
                        var5)) {
            var1.drawString(
                    this.font,
                    var9,
                    (this.width - var5) / 2,
                    var7,
                    this.failed ? -34953 : -2042690,
                    false);
            var7 += 12;
        }
    }
}
