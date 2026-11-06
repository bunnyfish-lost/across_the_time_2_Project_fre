package fr.poubone.att2.mixin.teleport;

import fr.poubone.att2.client.rune.RuneWordCraft119;
import fr.poubone.att2.client.teleport.TeleportStepEffectRenderer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void gtalikeTeleport$renderEffectAfterHud(
            GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        TeleportStepEffectRenderer.render(graphics, deltaTracker);
    }

    @Inject(
            method = "setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = true)
    private void att2$autoWordConfirm1131(Component var1, boolean var2, CallbackInfo var3) {
        if (!RuneWordCraft119.onMessage(var1, true)) {
            var3.cancel();
        }
    }
}
