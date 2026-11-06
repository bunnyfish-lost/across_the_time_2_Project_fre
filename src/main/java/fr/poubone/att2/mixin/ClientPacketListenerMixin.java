package fr.poubone.att2.mixin;

import fr.poubone.att2.client.data.SpellXpRefresh;
import fr.poubone.att2.client.gambling.GamblingModel;
import fr.poubone.att2.client.hud.CityToast;
import fr.poubone.att2.client.teleport.TeleportCommandSender;
import fr.poubone.att2.client.teleport.WaypointTeleportDetector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleOpenBook", at = @At("HEAD"), cancellable = true)
    private void att2$keepSpellRadialOpen(ClientboundOpenBookPacket var1, CallbackInfo var2) {
        if (Minecraft.getInstance().isSameThread() && SpellXpRefresh.suppressBookScreen()) {
            var2.cancel();
        }
    }

    @Inject(method = "sendCommand", at = @At("HEAD"))
    private void att2$observeCommand(String var1, CallbackInfo var2) {
        if (!TeleportCommandSender.isBypassing()) {
            WaypointTeleportDetector.onOutgoingCommand(var1);
        }
    }

    @Inject(method = "sendUnattendedCommand", at = @At("HEAD"), require = 0)
    private void att2$observeUnattendedCommand(String var1, Screen var2, CallbackInfo var3) {
        if (!TeleportCommandSender.isBypassing()) {
            WaypointTeleportDetector.onOutgoingCommand(var1);
        }
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    private void att2$observeSound(ClientboundSoundPacket var1, CallbackInfo var2) {
        if (Minecraft.getInstance().isSameThread()) {
            WaypointTeleportDetector.onSoundPacket(
                    var1.getSound().value().location(),
                    var1.getSource(),
                    var1.getX(),
                    var1.getY(),
                    var1.getZ());
            GamblingModel.get().onSound(var1.getSound().value().location(), var1.getSource());
        }
    }

    @Inject(method = "setTitleText", at = @At("HEAD"), cancellable = true)
    private void att2$observeTitle(ClientboundSetTitleTextPacket var1, CallbackInfo var2) {
        if (Minecraft.getInstance().isSameThread()) {
            GamblingModel.get().onTitle(var1.text());
            if (CityToast.consumeTitle(var1.text())) {
                var2.cancel();
            }
        }
    }

    @Inject(method = "setSubtitleText", at = @At("HEAD"), cancellable = true)
    private void att2$observeSubtitle(ClientboundSetSubtitleTextPacket var1, CallbackInfo var2) {
        if (Minecraft.getInstance().isSameThread() && CityToast.consumeSubtitle(var1.text())) {
            var2.cancel();
        }
    }

    @Inject(method = "handleTitlesClear", at = @At("HEAD"))
    private void att2$observeTitleClear(ClientboundClearTitlesPacket var1, CallbackInfo var2) {
        if (Minecraft.getInstance().isSameThread()) {
            CityToast.clearTitles(var1.shouldResetTimes());
        }
    }

    @Inject(method = "setTitlesAnimation", at = @At("HEAD"))
    private void att2$observeTitleTimes(
            ClientboundSetTitlesAnimationPacket var1, CallbackInfo var2) {
        if (Minecraft.getInstance().isSameThread()) {
            CityToast.titleTimes(var1.getFadeIn(), var1.getStay(), var1.getFadeOut());
        }
    }
}
