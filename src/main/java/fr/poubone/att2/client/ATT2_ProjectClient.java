package fr.poubone.att2.client;

import com.lootbeams.LootBeams;

import fr.poubone.att2.client.compat.FlashbackCompat;
import fr.poubone.att2.client.compat.MenuDispatch206;
import fr.poubone.att2.client.data.Att2Triggers;
import fr.poubone.att2.client.data.ChronotonPickupTracker;
import fr.poubone.att2.client.data.CurrencyModel;
import fr.poubone.att2.client.data.GrimoireXp;
import fr.poubone.att2.client.data.LootShimmerTracker;
import fr.poubone.att2.client.data.MapStatBar;
import fr.poubone.att2.client.data.MapStatDisplayEnabler;
import fr.poubone.att2.client.data.ScoreCache;
import fr.poubone.att2.client.data.SpellLauncherTracker;
import fr.poubone.att2.client.data.SpellLevelSelector;
import fr.poubone.att2.client.data.SpellXpRefresh;
import fr.poubone.att2.client.data.StatUpgradeModel;
import fr.poubone.att2.client.discord.DiscordPresence;
import fr.poubone.att2.client.gambling.GamblingModel;
import fr.poubone.att2.client.hud.ChronotonDisplay;
import fr.poubone.att2.client.hud.CityToast;
import fr.poubone.att2.client.hud.GlobeShader;
import fr.poubone.att2.client.hud.HudFx;
import fr.poubone.att2.client.hud.HudRenderer;
import fr.poubone.att2.client.hud.ManaOrbDisplay;
import fr.poubone.att2.client.hud.ModToast;
import fr.poubone.att2.client.hud.StatIconsDisplay;
import fr.poubone.att2.client.hud.XPDisplay;
import fr.poubone.att2.client.input.InputSequencer;
import fr.poubone.att2.client.input.KeybindManager;
import fr.poubone.att2.client.input.SneakCompareInput117;
import fr.poubone.att2.client.input.SneakMenuBypass;
import fr.poubone.att2.client.miner.MinerShopModel;
import fr.poubone.att2.client.quest.QuestModel;
import fr.poubone.att2.client.renderer.ItemParticleBudget;
import fr.poubone.att2.client.rune.RuneCodexModel;
import fr.poubone.att2.client.rune.WorkshopHopperCraft;
import fr.poubone.att2.client.shop.ShopModel;
import fr.poubone.att2.client.shop.ShopSeller;
import fr.poubone.att2.client.shop.ShopSellers;
import fr.poubone.att2.client.sync.PartySync;
import fr.poubone.att2.client.sync.PingMarkers;
import fr.poubone.att2.client.teleport.TeleportAnimationConfig;
import fr.poubone.att2.client.teleport.TeleportTransitionController;
import fr.poubone.att2.client.teleport.WaypointTeleportDetector;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.AllowGame;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

@Environment(EnvType.CLIENT)
public class ATT2_ProjectClient implements ClientModInitializer {
    public static final String MOD_ID = "att2";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("att2", path);
    }

    public void onInitializeClient() {
        TeleportAnimationConfig.load();
        new LootBeams().onInitializeClient();
        KeybindManager.register();
        GlobeShader.init();
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR, id("hud"), HudRenderer::render);
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT, id("pings"), PingMarkers::renderHud);
        UseEntityCallback.EVENT.register(
                (UseEntityCallback)
                        (player, world, hand, entity, hitResult) -> {
                            if (FlashbackCompat.isInReplay()) {
                                return InteractionResult.PASS;
                            }

                            if (!world.isClientSide()) {
                                return InteractionResult.PASS;
                            }

                            if (hand != InteractionHand.MAIN_HAND) {
                                return InteractionResult.PASS;
                            }

                            if (ShopSellers.isEldric(entity)) {
                                Minecraft.getInstance()
                                        .execute(() -> MinerShopModel.get().openFromNpc());
                                return InteractionResult.PASS;
                            }

                            ShopSeller seller = ShopSellers.matchEntity(entity);
                            if (seller == null) {
                                return InteractionResult.PASS;
                            }

                            ShopModel.get().openFromNpc(seller);
                            return InteractionResult.PASS;
                        });
        ClientTickEvents.START_CLIENT_TICK.register(
                (StartTick)
                        client -> {
                            SneakMenuBypass.tick(client);
                            if (!FlashbackCompat.isInReplay()) {
                                MinerShopModel.get().pollNpcUse(client);
                            }
                        });
        ClientTickEvents.END_CLIENT_TICK.register(
                (EndTick)
                        client -> {
                            Att2Triggers.tick(client);
                            MapStatDisplayEnabler.tick(client);
                            InputSequencer.tick(client);
                            WaypointTeleportDetector.tick(client);
                            TeleportTransitionController.tick(client);
                            QuestModel.get().tick(client);
                            GamblingModel.get().tick(client);
                            RuneCodexModel.get().tick();
                            WorkshopHopperCraft.tick(client);
                            MinerShopModel.get().tick();
                            ShopModel.get().tick(client);
                            CityToast.tick();
                            ModToast.tick();
                            HudFx.tick();
                            ChronotonPickupTracker.tick(client);
                            LootShimmerTracker.tick(client);
                            ItemParticleBudget.tick(client);
                            SneakCompareInput117.tick(client);
                            SpellLauncherTracker.tick(client);
                            SpellLevelSelector.tick();
                            SpellXpRefresh.tick(client);
                            GrimoireXp.captureResolvedBooks(client.player);
                            DiscordPresence.tick();
                            PartySync.tick(client);
                        });
        ClientReceiveMessageEvents.ALLOW_GAME.register(
                (AllowGame) (message, overlay) -> MenuDispatch206.allow(message, overlay));
        ClientPlayConnectionEvents.JOIN.register(
                (Join)
                        (handler, sender, client) -> {
                            ScoreCache.clear();
                            MapStatBar.reset();
                            CurrencyModel.reset();
                            QuestModel.get().reset();
                            StatUpgradeModel.reset();
                            GamblingModel.get().reset();
                            RuneCodexModel.get().reset();
                            MinerShopModel.get().reset();
                            ShopModel.get().reset();
                            WaypointTeleportDetector.reset();
                            Att2Triggers.reset();
                            MapStatDisplayEnabler.reset();
                            CityToast.reset();
                            ModToast.reset();
                            HudFx.reset();
                            ManaOrbDisplay.reset();
                            XPDisplay.reset();
                            ChronotonDisplay.reset();
                            StatIconsDisplay.reset();
                            ChronotonPickupTracker.reset();
                            LootShimmerTracker.reset();
                            SpellLauncherTracker.reset();
                            SpellLevelSelector.reset();
                            SpellXpRefresh.reset();
                            GrimoireXp.reset();
                            DiscordPresence.onJoin();
                            PartySync.onJoin();
                        });
        ClientPlayConnectionEvents.DISCONNECT.register(
                (Disconnect)
                        (handler, client) -> {
                            ScoreCache.clear();
                            MapStatBar.reset();
                            CurrencyModel.reset();
                            QuestModel.get().reset();
                            StatUpgradeModel.reset();
                            GamblingModel.get().reset();
                            RuneCodexModel.get().reset();
                            MinerShopModel.get().reset();
                            ShopModel.get().reset();
                            WaypointTeleportDetector.reset();
                            Att2Triggers.reset();
                            MapStatDisplayEnabler.reset();
                            CityToast.reset();
                            ModToast.reset();
                            HudFx.reset();
                            ManaOrbDisplay.reset();
                            XPDisplay.reset();
                            ChronotonDisplay.reset();
                            StatIconsDisplay.reset();
                            ChronotonPickupTracker.reset();
                            LootShimmerTracker.reset();
                            SpellLauncherTracker.reset();
                            SpellLevelSelector.reset();
                            SpellXpRefresh.reset();
                            GrimoireXp.reset();
                            DiscordPresence.onDisconnect();
                            PartySync.onDisconnect();
                        });
    }
}
