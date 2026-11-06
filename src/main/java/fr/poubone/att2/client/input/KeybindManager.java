package fr.poubone.att2.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.data.Att2Triggers;
import fr.poubone.att2.client.data.SpellLauncherTracker;
import fr.poubone.att2.client.data.SpellSelectTriggers;
import fr.poubone.att2.client.hud.HUDConfig;
import fr.poubone.att2.client.quest.QuestBookScreen;
import fr.poubone.att2.client.quest.QuestTracking;
import fr.poubone.att2.client.rune.RecipeTracking119;
import fr.poubone.att2.client.screen.HUDConfigScreen;
import fr.poubone.att2.client.screen.RepairMenuScreen;
import fr.poubone.att2.client.screen.SpellLevelRadialScreen;
import fr.poubone.att2.client.screen.StatUpgradeScreen;
import fr.poubone.att2.client.spell.SpellWheelInput;
import fr.poubone.att2.client.sync.PartySync;
import fr.poubone.att2.client.util.BroadcastScanner;
import fr.poubone.att2.client.util.ModLanguageManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;

import java.util.List;

public class KeybindManager {
    public static boolean showCustomHUD = true;
    private static final Category CATEGORY =
            Category.register(Identifier.fromNamespaceAndPath("att2", "att2"));
    private static KeyMapping toggleHUDKey;
    private static KeyMapping statUpgradeMenuKey;
    private static KeyMapping playerGlow;
    private static KeyMapping collectItems;
    private static KeyMapping questBook;
    private static KeyMapping trackQuestKey;
    private static KeyMapping repairItemMenuKey;
    private static boolean repairReleased = true;
    private static KeyMapping whistle;
    private static KeyMapping openHUDConfig;
    private static KeyMapping broadcastKey;
    private static KeyMapping pingKey;
    private static KeyMapping compareShopKey;
    private static KeyMapping weaponSkillKey;
    private static KeyMapping spellLevelMenuKey;
    private static KeyMapping spellWheelKey;
    private static boolean spellLevelReleased = true;
    private static boolean languageLoaded = false;
    private static long lastUsedTime = 0L;
    private static final long COOLDOWN_MS = 5000L;
    private static long lastPingTime = 0L;
    private static final long PING_COOLDOWN_MS = 250L;

    private static KeyMapping key(String var0, int var1) {
        return KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.att2." + var0, Type.KEYSYM, var1, CATEGORY));
    }

    private static void triggerWeaponSkill(Minecraft var0) {
        if (var0.player.isBlocking()) {
            InputSequencer.run(
                    List.of(
                            InputSequencer.Step.forceSneak(8),
                            InputSequencer.Step.wait(2),
                            InputSequencer.Step.holdUse(4)));
        } else if (var0.player.getMainHandItem().is(ItemTags.AXES)) {
            InputSequencer.run(List.of(InputSequencer.Step.clickSwap()));
        } else {
            InputSequencer.run(List.of(InputSequencer.Step.forceSneak(6)));
        }
    }

    public static KeyMapping getSpellWheelKey() {
        return spellWheelKey;
    }

    public static KeyMapping getRepairItemMenuKey() {
        return repairItemMenuKey;
    }

    public static void blockRepairUntilRelease() {
        repairReleased = false;
    }

    public static KeyMapping getSpellLevelMenuKey() {
        return spellLevelMenuKey;
    }

    public static int compareKeyCode() {
        if (compareShopKey == null) {
            return 340;
        }

        int var0 = KeyBindingHelper.getBoundKeyOf(compareShopKey).getValue();
        return var0 == InputConstants.UNKNOWN.getValue() ? 340 : var0;
    }

    public static Component compareKeyLabel() {
        return compareShopKey == null
                ? Component.translatable("key.keyboard.left.shift")
                : compareShopKey.getTranslatedKeyMessage();
    }

    public static void blockSpellLevelUntilRelease() {
        spellLevelReleased = false;
    }

    private static void handleBroadcastKey(Minecraft var0) {
        if (!FlashbackCompat.isInReplay() && var0.player != null) {
            if (!PartySync.isEnabled()) {
                var0.player.displayClientMessage(
                        Component.literal(
                                "§c"
                                        + ModLanguageManager.getString(
                                                HUDConfig.get().partySyncEnabled
                                                        ? "keybind.broadcast.no_api"
                                                        : "party_sync.disabled")),
                        true);
            } else {
                long var1 = System.currentTimeMillis();
                if (var1 - lastUsedTime < 5000L) {
                    long var3 = (5000L - (var1 - lastUsedTime)) / 1000L;
                    String var5 =
                            ModLanguageManager.format("keybind.broadcast.cooldown", "s", var3);
                    var0.player.displayClientMessage(Component.literal("§c" + var5), true);
                } else {
                    lastUsedTime = var1;
                    PartySync.shareHeldItem(var0);
                }
            }
        }
    }

    private static void handlePingKey(Minecraft var0) {
        if (!FlashbackCompat.isInReplay() && var0.player != null) {
            if (!PartySync.isEnabled()) {
                var0.player.displayClientMessage(
                        Component.literal(
                                "§c"
                                        + ModLanguageManager.getString(
                                                HUDConfig.get().partySyncEnabled
                                                        ? "keybind.ping.no_api"
                                                        : "party_sync.disabled")),
                        true);
            } else {
                long var1 = System.currentTimeMillis();
                if (var1 - lastPingTime >= 250L) {
                    lastPingTime = var1;
                    PartySync.pingLookedAt(var0);
                }
            }
        }
    }

    public static void register() {
        toggleHUDKey = key("toggle_hud", 72);
        statUpgradeMenuKey = key("stat_upgrade_menu", -1);
        playerGlow = key("player_glow", -1);
        collectItems = key("collect_items", -1);
        questBook = key("quest_main", 66);
        trackQuestKey = key("track_quest", -1);
        repairItemMenuKey = key("repair_menu", -1);
        whistle = key("whistle", -1);
        openHUDConfig = key("hud_config", 75);
        broadcastKey = key("broadcast", -1);
        pingKey =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping("key.att2.ping", Type.KEYSYM, -1, CATEGORY));
        compareShopKey = key("compare_shop", 340);
        weaponSkillKey = key("weapon_skill", InputConstants.UNKNOWN.getValue());
        spellLevelMenuKey = key("spell_level_menu", InputConstants.UNKNOWN.getValue());
        spellWheelKey = key("spell_wheel", -1);
        ClientTickEvents.END_CLIENT_TICK.register(
                (EndTick)
                        var0 -> {
                            if (var0.player != null) {
                                if (!languageLoaded && var0.getResourceManager() != null) {
                                    languageLoaded = true;
                                    ModLanguageManager.loadLanguage(
                                            var0, HUDConfig.getModLanguage());
                                }

                                while (toggleHUDKey.consumeClick()) {
                                    showCustomHUD = !showCustomHUD;
                                    String var1 =
                                            showCustomHUD
                                                    ? "keybind.toggle_hud.on"
                                                    : "keybind.toggle_hud.off";
                                    var0.player.displayClientMessage(
                                            ModLanguageManager.get(var1), true);
                                }

                                while (statUpgradeMenuKey.consumeClick()) {
                                    var0.setScreen(new StatUpgradeScreen());
                                }

                                while (playerGlow.consumeClick()) {
                                    Att2Triggers.send(1085);
                                }

                                while (collectItems.consumeClick()) {
                                    Att2Triggers.send(1072);
                                }

                                while (questBook.consumeClick()) {
                                    if (var0.screen == null) {
                                        QuestBookScreen.open();
                                    }
                                }

                                while (trackQuestKey.consumeClick()) {
                                    if (var0.screen == null) {
                                        QuestTracking.quickTrack();
                                    }
                                }

                                while (whistle.consumeClick()) {
                                    Att2Triggers.send(1070);
                                }

                                while (openHUDConfig.consumeClick()) {
                                    var0.setScreen(new HUDConfigScreen());
                                }

                                while (broadcastKey.consumeClick()) {
                                    handleBroadcastKey(var0);
                                }

                                while (pingKey.consumeClick()) {
                                    handlePingKey(var0);
                                }

                                while (weaponSkillKey.consumeClick()) {
                                    if (var0.player != null
                                            && var0.screen == null
                                            && !InputSequencer.isBusy()) {
                                        triggerWeaponSkill(var0);
                                    }
                                }

                                if (repairItemMenuKey.isDown()) {
                                    if (repairReleased && var0.screen == null) {
                                        var0.setScreen(new RepairMenuScreen());
                                        repairReleased = false;
                                    }
                                } else {
                                    repairReleased = true;
                                }

                                if (spellLevelMenuKey.isDown()) {
                                    if (spellLevelReleased && var0.screen == null) {
                                        SpellLauncherTracker.LauncherState var2 =
                                                SpellLauncherTracker.parse(
                                                        var0.player.getMainHandItem());
                                        if (var2 != null
                                                && SpellSelectTriggers.isSupported(var2.spellId)) {
                                            var0.setScreen(new SpellLevelRadialScreen(var2));
                                            spellLevelReleased = false;
                                        }
                                    }
                                } else {
                                    spellLevelReleased = true;
                                }

                                SpellWheelInput.tick(var0);
                                BroadcastScanner.tick(var0);
                            }
                        });
        RecipeTracking119.initialize(CATEGORY);
    }
}
