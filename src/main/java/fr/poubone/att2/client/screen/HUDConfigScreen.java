package fr.poubone.att2.client.screen;

import com.lootbeams.features.CustomLootBeamsConfig;

import fr.poubone.att2.client.data.StatManager;
import fr.poubone.att2.client.discord.DiscordPresence;
import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.renderer.ItemRarity;
import fr.poubone.att2.client.sync.PartySync;
import fr.poubone.att2.client.teleport.TeleportAnimationConfig;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Checkbox.Builder;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class HUDConfigScreen extends Screen {
    private static final List<String> RARITY_KEYS =
            List.of(
                    "com",
                    "unc",
                    "rar",
                    "epi_esc",
                    "epi",
                    "epi_set",
                    "leg",
                    "leg_armset",
                    "ult",
                    "myt",
                    "spe",
                    "que",
                    "misc",
                    "cur",
                    "unk",
                    "esc");
    private HUDConfigScreen.Category category = HUDConfigScreen.Category.HUD;
    private final Screen returnScreen;
    private final SettingsSaveState saveState = new SettingsSaveState();
    private final List<HUDConfigScreen.Row> rows = new ArrayList<>();
    private int scroll;
    private int maxScroll;
    private int contentX;
    private int contentRight;
    private int contentWidth;
    private boolean dragging;

    public HUDConfigScreen() {
        this(null);
    }

    public HUDConfigScreen(Screen var1) {
        super(ModLanguageManager.get("screen.hud_config.title"));
        this.returnScreen = var1;
    }

    public void exitToParent() {
        this.minecraft.setScreen(this.returnScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return this.returnScreen != null && this.returnScreen.isPauseScreen();
    }

    private int bottom() {
        return Math.max(40, this.height - 38);
    }

    private <T extends AbstractWidget> T content(T var1) {
        this.addRenderableWidget(var1);
        this.rows.add(new HUDConfigScreen.Row(var1, var1.getY()));
        return (T) var1;
    }

    private void changed() {
        CustomLootBeamsConfig.clearConfigCache();
    }

    private int toggle(
            int var1, int var2, int var3, String var4, boolean var5, Consumer<Boolean> var6) {
        return this.toggleText(var1, var2, var3, ModLanguageManager.get(var4), var5, var6);
    }

    private int toggleText(
            int var1, int var2, int var3, Component var4, boolean var5, Consumer<Boolean> var6) {
        Builder var7 =
                Checkbox.builder(var4, this.font)
                        .pos(var1, var2)
                        .maxWidth(Math.max(1, var3 - Checkbox.getBoxSize(this.font) - 4))
                        .selected(var5)
                        .onValueChange(
                                (var2x, var3x) -> {
                                    var6.accept(var3x);
                                    this.changed();
                                });
        Checkbox var8 = var7.build();
        var8.setWidth(var3);
        this.content(var8);
        return var2 + Math.max(22, var8.getHeight() + 2);
    }

    private int limits(int var1, String var2, int[] var3, IntSupplier var4, IntConsumer var5) {
        Button var6 =
                Button.builder(
                                Component.empty(),
                                var4x -> {
                                    int var5x = 0;

                                    for (int var6x = 0; var6x < var3.length; var6x++) {
                                        if (var3[var6x] == var4.getAsInt()) {
                                            var5x = var6x;
                                        }
                                    }

                                    var5.accept(var3[(var5x + 1) % var3.length]);
                                    this.changed();
                                    this.init();
                                })
                        .bounds(this.contentX, var1, this.contentWidth, 20)
                        .build();
        var6.setMessage(
                Component.literal(
                        ModLanguageManager.getString(var2)
                                + "："
                                + (var4.getAsInt() == 0
                                        ? ModLanguageManager.getString(
                                                "screen.hud_config.limit_unlimited")
                                        : var4.getAsInt())));
        this.content(var6);
        return var1 + 24;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.rows.clear();
        this.dragging = false;
        HUDConfig var1 = HUDConfig.get();
        boolean var2 = ModLanguageManager.isRightToLeft();
        int var3 = var2 ? this.width - 124 : 12;
        int var4 = HUDConfigLayout.tabTop(this.height, HUDConfigScreen.Category.values().length);

        for (HUDConfigScreen.Category var8 : HUDConfigScreen.Category.values()) {
            MutableComponent var9 =
                    ModLanguageManager.get(
                            "screen.hud_config.cat." + var8.name().toLowerCase(Locale.ROOT));
            if (var8 == this.category) {
                var9 = Component.literal("▸ ").append(var9);
            }

            HUDConfigScreen.Category var10 = var8;
            this.addRenderableWidget(
                    Button.builder(
                                    var9,
                                    var2x -> {
                                        this.category = var10;
                                        this.scroll = 0;
                                        this.init();
                                    })
                            .bounds(
                                    var3,
                                    var4,
                                    112,
                                    HUDConfigLayout.tabHeight(
                                            this.height, HUDConfigScreen.Category.values().length))
                            .build());
            var4 += HUDConfigLayout.tabStep(this.height, HUDConfigScreen.Category.values().length);
        }

        this.contentX = var2 ? 16 : 140;
        this.contentRight = var2 ? this.width - 140 : this.width - 12;
        this.contentWidth = Math.max(24, Math.min(360, this.contentRight - this.contentX - 8));
        int var14 = 40;
        switch (this.category) {
            case HUD:
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.show_chronoton",
                                var1.showChronoton,
                                var1x -> var1.showChronoton = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.show_xp",
                                var1.showXP,
                                var1x -> var1.showXP = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.show_mana",
                                var1.showMana,
                                var1x -> var1.showMana = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.show_stats",
                                var1.showStats,
                                var1x -> var1.showStats = var1x);
                var14 = this.statsControls(var14, var1);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.show_arrows",
                                var1.showArrows,
                                var1x -> var1.showArrows = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.show_armor_durability",
                                var1.showArmorDurability,
                                var1x -> var1.showArmorDurability = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.show_spell_bar",
                                var1.showSpellBar,
                                var1x -> var1.showSpellBar = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.show_temperature",
                                var1.showTemperature,
                                var1x -> var1.showTemperature = var1x);
                this.content(
                        Button.builder(
                                        ModLanguageManager.get("screen.hud_config.arrange"),
                                        var1x ->
                                                this.minecraft.setScreen(new HudLayoutScreen(this)))
                                .bounds(
                                        this.contentX,
                                        var14 + 8,
                                        Math.min(200, this.contentWidth),
                                        20)
                                .build());
                break;
            case MENUS:
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.shop_follow_gui_scale",
                                var1.shopFollowGuiScale,
                                var1x -> var1.shopFollowGuiScale = var1x);
                this.rows
                        .get(this.rows.size() - 1)
                        .widget()
                        .setTooltip(
                                Tooltip.create(
                                        ModLanguageManager.get(
                                                "screen.hud_config.shop_follow_gui_scale.tooltip")));
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.sneak_skips_menus",
                                var1.sneakSkipsMenus,
                                var1x -> var1.sneakSkipsMenus = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.hide_owned_spells",
                                var1.shopHideOwnedSpells,
                                var1x -> var1.shopHideOwnedSpells = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.quest_menu",
                                var1.questMenuEnabled,
                                var1x -> var1.questMenuEnabled = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.rune_menu",
                                var1.runeMenuEnabled,
                                var1x -> var1.runeMenuEnabled = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.miner_menu",
                                var1.minerMenuEnabled,
                                var1x -> var1.minerMenuEnabled = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.charles_auto_open",
                                var1.charlesAutoOpen,
                                var1x -> var1.charlesAutoOpen = var1x);
                int var54 = Math.max(1, Math.min(2, (this.contentRight - this.contentX + 2) / 180));
                int var57 =
                        Math.max(
                                24,
                                Math.min(
                                        170,
                                        (this.contentRight - this.contentX - 8 - (var54 - 1) * 10)
                                                / var54));
                int var60 = 0;

                while (var60 < HUDConfig.SHOP_MENU_IDS.size()) {
                    int var63 = var14;

                    for (int var66 = 0;
                            var66 < var54 && var60 < HUDConfig.SHOP_MENU_IDS.size();
                            var60++) {
                        String var67 = HUDConfig.SHOP_MENU_IDS.get(var60);
                        var63 =
                                Math.max(
                                        var63,
                                        this.toggle(
                                                this.contentX + var66 * (var57 + 10),
                                                var14,
                                                var57,
                                                "shop.type." + var67,
                                                var1.isShopMenuEnabled(var67),
                                                var2x -> var1.setShopMenuEnabled(var67, var2x)));
                        var66++;
                    }

                    var14 = var63;
                }
                break;
            case QUESTS:
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.quest_show_main",
                                var1.questBookShowMain,
                                var1x -> var1.questBookShowMain = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.quest_show_side",
                                var1.questBookShowSide,
                                var1x -> var1.questBookShowSide = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.quest_show_completed",
                                var1.questBookShowCompleted,
                                var1x -> var1.questBookShowCompleted = var1x);
                this.toggle(
                        this.contentX,
                        var14,
                        this.contentWidth,
                        "screen.hud_config.quest_show_daily",
                        var1.questBookShowDaily,
                        var1x -> var1.questBookShowDaily = var1x);
                break;
            case LOOT:
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.render_allItems",
                                var1.allItems,
                                var1x -> var1.allItems = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.render_nametags",
                                var1.renderNametags,
                                var1x -> var1.renderNametags = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.render_stackcount",
                                var1.renderStackcount,
                                var1x -> var1.renderStackcount = var1x);
                var14 += 6;

                for (String var56 : RARITY_KEYS) {
                    ItemRarity var59 = ItemRarity.fromId(var56);
                    int var62 = var59 == null ? ItemRarity.EPI_ESC.rgb : var59.rgb;
                    MutableComponent var65 =
                            ModLanguageManager.get("screen.hud_config.render_rarity." + var56)
                                    .withColor(var62);
                    var14 =
                            this.toggleText(
                                    this.contentX,
                                    var14,
                                    this.contentWidth,
                                    var65,
                                    var1.renderRarities.contains(var56),
                                    var2x -> {
                                        if (var2x) {
                                            if (!var1.renderRarities.contains(var56)) {
                                                var1.renderRarities.add(var56);
                                            }
                                        } else {
                                            var1.renderRarities.remove(var56);
                                        }
                                    });
                }
                break;
            case PERFORMANCE:
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.orb_gpu",
                                var1.orbGpuRendering,
                                var2x -> {
                                    var1.orbGpuRendering = var2x;
                                    this.init();
                                });
                this.addOrbFpsSlider(this.contentX, var14, this.contentWidth, var1);
                var14 += 28;
                String[] var52 =
                        new String[] {
                            "batchLootBeams",
                            "cullLootBeams",
                            "cacheLootBeamConfig",
                            "pruneBeamTimersPerFrame",
                            "dustWithoutCollisions",
                            "cullHiddenItemFaces",
                            "cacheItemBounds",
                            "directItemOutlines",
                            "cacheNameTags",
                            "lightItemChecks",
                            "lootPiles",
                            "solidItemEntities"
                        };

                for (String var64 : var52) {
                    String var11 = var64;

                    try {
                        Field var12 = HUDConfig.class.getField(var11);
                        var14 =
                                this.toggle(
                                        this.contentX,
                                        var14,
                                        this.contentWidth,
                                        "screen.hud_config.perf." + var11,
                                        var12.getBoolean(var1),
                                        var2x -> {
                                            try {
                                                var12.setBoolean(var1, var2x);
                                            } catch (IllegalAccessException var4x) {
                                                throw new IllegalStateException(var4x);
                                            }
                                        });
                    } catch (ReflectiveOperationException var13) {
                        throw new IllegalStateException(var13);
                    }
                }

                var14 =
                        this.limits(
                                var14,
                                "screen.hud_config.loot_beam_limit",
                                new int[] {16, 32, 64, 128, 0},
                                () -> var1.lootBeamLimit,
                                var1x -> var1.lootBeamLimit = var1x);
                this.limits(
                        var14,
                        "screen.hud_config.map_particle_limit",
                        new int[] {8, 16, 32, 64, 0},
                        () -> var1.mapItemParticleLimit,
                        var1x -> var1.mapItemParticleLimit = var1x);
                break;
            case GENERAL:
                this.content(
                        Button.builder(
                                        Component.literal(
                                                ModLanguageManager.getString(
                                                                "screen.hud_config.language_button")
                                                        + " : "
                                                        + ModLanguageManager.nativeName(
                                                                var1.modLanguage)),
                                        var1x -> this.cycleLanguage())
                                .bounds(this.contentX, var14, Math.min(200, this.contentWidth), 20)
                                .build());
                var14 += 28;
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.teleport_effect",
                                TeleportAnimationConfig.isEffectEnabled(),
                                TeleportAnimationConfig::setEffectEnabled);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.currency_pickup_animation",
                                var1.currencyPickupAnimation,
                                var1x -> var1.currencyPickupAnimation = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.disable_gray_background",
                                var1.disableGrayBackground,
                                var1x -> var1.disableGrayBackground = var1x);
                this.rows
                        .get(this.rows.size() - 1)
                        .widget()
                        .setTooltip(
                                Tooltip.create(
                                        ModLanguageManager.get(
                                                "screen.hud_config.disable_gray_background.tooltip")));
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.drop_lock",
                                var1.dropLockEnabled,
                                var1x -> var1.dropLockEnabled = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.party_sync",
                                var1.partySyncEnabled,
                                var1x -> {
                                    var1.partySyncEnabled = var1x;
                                    PartySync.onConfigChanged();
                                });
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.discord_rpc",
                                var1.discordRichPresence,
                                var1x -> {
                                    var1.discordRichPresence = var1x;
                                    DiscordPresence.onConfigChanged();
                                });
                this.toggle(
                        this.contentX,
                        var14,
                        this.contentWidth,
                        "screen.hud_config.discord_show_quest",
                        var1.discordShowQuest,
                        var1x -> {
                            var1.discordShowQuest = var1x;
                            DiscordPresence.onConfigChanged();
                        });
                break;
            case CITY:
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.takeover_city_titles",
                                var1.takeoverCityTitles,
                                var1x -> var1.takeoverCityTitles = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.takeover_level_titles",
                                var1.takeoverLevelTitles,
                                var1x -> var1.takeoverLevelTitles = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.takeover_other_titles",
                                var1.takeoverOtherTitles,
                                var1x -> var1.takeoverOtherTitles = var1x);
                var14 =
                        this.toggle(
                                this.contentX,
                                var14,
                                this.contentWidth,
                                "screen.hud_config.city_transparent_background",
                                var1.cityTransparentBackground,
                                var1x -> var1.cityTransparentBackground = var1x);
                this.toggle(
                        this.contentX,
                        var14,
                        this.contentWidth,
                        "screen.hud_config.city_map_colors",
                        var1.cityUseOriginalTextColors,
                        var1x -> var1.cityUseOriginalTextColors = var1x);
        }

        this.addRenderableWidget(
                Button.builder(
                                ModLanguageManager.get("screen.hud_config.save_and_close"),
                                var1x -> {
                                    if (this.saveState.save()) {
                                        DiscordPresence.onConfigChanged();
                                        this.exitToParent();
                                    } else {
                                        this.minecraft.setScreen(
                                                new UnsavedSettingsScreen(
                                                        this, this.saveState, true));
                                    }
                                })
                        .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
                        .build());
        this.place();
    }

    private void place() {
        int var1 = 40;

        for (HUDConfigScreen.Row var3 : this.rows) {
            var1 = Math.max(var1, var3.baseY() + var3.widget().getHeight());
        }

        this.maxScroll = Math.max(0, var1 + 2 - this.bottom());
        this.scroll = Math.max(0, Math.min(this.scroll, this.maxScroll));

        for (HUDConfigScreen.Row var8 : this.rows) {
            AbstractWidget var4 = var8.widget();
            int var5 = var8.baseY() - this.scroll;
            var4.setY(var5);
            boolean var6 = var5 >= 40 && var5 + var4.getHeight() <= this.bottom();
            var4.visible = var6;
            var4.active = var6;
            if (!var6) {
                var4.setFocused(false);
                if (this.getFocused() == var4) {
                    this.setFocused(null);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        if (var7 != 0.0
                && var1 >= this.contentX
                && var1 < this.contentRight
                && var3 >= 40.0
                && var3 <= this.bottom()
                && this.maxScroll > 0) {
            this.scroll = (int) (this.scroll - Math.round(var7 * 22.0));
            this.place();
            return true;
        } else {
            return super.mouseScrolled(var1, var3, var5, var7);
        }
    }

    private int barX() {
        return this.contentRight - 4;
    }

    private int thumbHeight() {
        int var1 = this.bottom() - 40;
        return Math.max(10, var1 * var1 / Math.max(1, var1 + this.maxScroll));
    }

    private int thumbY() {
        return 40
                + (this.bottom() - 40 - this.thumbHeight())
                        * this.scroll
                        / Math.max(1, this.maxScroll);
    }

    private void drag(double var1) {
        int var3 = this.bottom() - 40 - this.thumbHeight();
        this.scroll =
                var3 <= 0
                        ? 0
                        : (int)
                                Math.round(
                                        Math.max(
                                                        0.0,
                                                        Math.min(
                                                                1.0,
                                                                (var1
                                                                                - 40.0
                                                                                - this.thumbHeight()
                                                                                        / 2.0)
                                                                        / var3))
                                                * this.maxScroll);
        this.place();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
        if (var1.button() == 0
                && this.maxScroll > 0
                && var1.x() >= this.barX() - 2
                && var1.x() < this.barX() + 6
                && var1.y() >= 40.0
                && var1.y() <= this.bottom()) {
            this.dragging = true;
            this.drag(var1.y());
            return true;
        } else {
            return super.mouseClicked(var1, var2);
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
        if (this.dragging && var1.button() == 0) {
            this.drag(var1.y());
            return true;
        } else {
            return super.mouseDragged(var1, var2, var4);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent var1) {
        if (this.dragging) {
            this.dragging = false;
            return true;
        } else {
            return super.mouseReleased(var1);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent var1) {
        int var2 = this.scroll;
        switch (var1.key()) {
            case 266:
                this.scroll = this.scroll - Math.max(22, this.bottom() - 40);
                break;
            case 267:
                this.scroll = this.scroll + Math.max(22, this.bottom() - 40);
                break;
            case 268:
                this.scroll = 0;
                break;
            case 269:
                this.scroll = this.maxScroll;
                break;
            default:
                return super.keyPressed(var1);
        }

        this.place();
        return var2 != this.scroll;
    }

    private void addOrbFpsSlider(int var1, int var2, int var3, final HUDConfig var4) {
        final int[] var5 = HUDConfig.ORB_FPS_STEPS;
        int var6 = 0;

        for (int var7 = 0; var7 < var5.length; var7++) {
            if (var5[var7] == var4.effectiveOrbFps()) {
                var6 = var7;
            }
        }

        AbstractSliderButton var8 =
                new AbstractSliderButton(
                        var1,
                        var2,
                        var3,
                        20,
                        Component.empty(),
                        (double) var6 / (var5.length - 1)) {
                    {
                        this.updateMessage();
                    }

                    private int fps() {
                        return var5[(int) Math.round(this.value * (var5.length - 1))];
                    }

                    @Override
                    protected void updateMessage() {
                        int var1x = this.fps();
                        this.setMessage(
                                Component.literal(
                                        var1x == 0
                                                ? ModLanguageManager.getString(
                                                        "screen.hud_config.orb_fps.every_frame")
                                                : ModLanguageManager.format(
                                                        "screen.hud_config.orb_fps",
                                                        "fps",
                                                        var1x)));
                    }

                    @Override
                    protected void applyValue() {
                        var4.orbFps = this.fps();
                    }
                };
        var8.setTooltip(
                Tooltip.create(ModLanguageManager.get("screen.hud_config.orb_fps.tooltip")));
        this.content(var8);
    }

    private int statsControls(int var1, HUDConfig var2) {
        this.content(
                Button.builder(
                                ModLanguageManager.get(
                                        "screen.hud_config.stats.layout."
                                                + switch (var2.statsLayoutMode) {
                                                    case 1 -> "row";
                                                    case 2 -> "column";
                                                    default -> "auto";
                                                }),
                                var2x -> {
                                    var2.statsLayoutMode = (var2.statsLayoutMode + 1) % 3;
                                    this.init();
                                })
                        .bounds(this.contentX, var1, this.contentWidth, 20)
                        .build());
        var1 += 24;
        List var3 = StatManager.STAT_KEYS;
        int var4 = this.contentWidth >= 240 ? 2 : 1;
        int var5 = (this.contentWidth - (var4 - 1) * 8) / var4;
        int var6 = 0;

        while (var6 < var3.size()) {
            int var7 = var1;

            for (int var8 = 0; var8 < var4 && var6 < var3.size(); var6++) {
                String var9 = (String) var3.get(var6);
                var7 =
                        Math.max(
                                var7,
                                this.toggle(
                                        this.contentX + var8 * (var5 + 8),
                                        var1,
                                        var5,
                                        "stat.label." + var9.toLowerCase(Locale.ROOT),
                                        var2.isStatVisible(var9),
                                        var2x -> var2.setStatVisible(var9, var2x)));
                var8++;
            }

            var1 = var7;
        }

        return var1 + 4;
    }

    private void cycleLanguage() {
        HUDConfig var1 = HUDConfig.get();
        int var2 =
                (ModLanguageManager.CODES.indexOf(var1.modLanguage) + 1)
                        % ModLanguageManager.CODES.size();
        String var3 = ModLanguageManager.CODES.get(var2);
        HUDConfig.setModLanguage(var3);
        ModLanguageManager.loadLanguage(Minecraft.getInstance(), var3);
        this.init();
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        boolean var5 = ModLanguageManager.isRightToLeft();
        GuiBackdrop1132.fill(var1, 0, 0, this.width, this.height, -1726935540, this);
        GuiBackdrop1132.fill(
                var1,
                var5 ? this.width - 128 : 0,
                0,
                var5 ? this.width : 128,
                this.height,
                -870047717,
                this);
        var1.drawString(
                this.font,
                ModLanguageManager.getString("screen.hud_config.title"),
                var5 ? this.width - 124 : 12,
                12,
                -1521558,
                false);
        var1.drawString(
                this.font,
                ModLanguageManager.getString(
                        "screen.hud_config.cat." + this.category.name().toLowerCase(Locale.ROOT)),
                this.contentX,
                14,
                -3094352,
                false);
        super.render(var1, var2, var3, var4);
        if (this.maxScroll > 0) {
            GuiBackdrop1132.fill(
                    var1, this.barX(), 40, this.barX() + 3, this.bottom(), 1090519039, this);
            GuiBackdrop1132.fill(
                    var1,
                    this.barX(),
                    this.thumbY(),
                    this.barX() + 3,
                    this.thumbY() + this.thumbHeight(),
                    -1521558,
                    this);
        }
    }

    @Override
    public void onClose() {
        if (this.saveState.dirty()) {
            this.minecraft.setScreen(new UnsavedSettingsScreen(this, this.saveState));
        } else {
            this.exitToParent();
        }
    }

    private enum Category {
        HUD,
        MENUS,
        QUESTS,
        LOOT,
        PERFORMANCE,
        GENERAL,
        CITY;
    }

    private record Row(AbstractWidget widget, int baseY) {}
}
